import json
import sys
import threading
import time
from datetime import datetime
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

DATA_FILE = Path(__file__).with_name("data.json")
PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 8080
MODES = {"ok", "error500", "badrequest", "timeout"}

SEED = [
    {"id": "6f1c2a10-0001-4c3b-9a11-000000000001", "title": "Revisar correo del cliente", "description": "Responder la consulta del pedido",
     "category": "WORK", "priority": "HIGH", "status": "PENDING", "createdAt": 1762900000000, "updatedAt": 1762900000000},
    {"id": "6f1c2a10-0002-4c3b-9a11-000000000002", "title": "Comprar fruta", "description": None,
     "category": "PERSONAL", "priority": "LOW", "status": "PENDING", "createdAt": 1762990000000, "updatedAt": 1762990000000},
    {"id": "6f1c2a10-0003-4c3b-9a11-000000000003", "title": "Control dental", "description": "Clinica del centro",
     "category": "HEALTH", "priority": "MEDIUM", "status": "DONE", "createdAt": 1763080000000, "updatedAt": 1763080000000},
]

lock = threading.Lock()
state = {"mode": "ok"}


def load():
    if not DATA_FILE.exists():
        save({item["id"]: item for item in SEED})
    return json.loads(DATA_FILE.read_text(encoding="utf-8"))


def save(items):
    DATA_FILE.write_text(json.dumps(items, ensure_ascii=False, indent=2), encoding="utf-8")


class Handler(BaseHTTPRequestHandler):

    def send_json(self, code, body=None):
        payload = b"" if body is None else json.dumps(body, ensure_ascii=False).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(payload)))
        self.end_headers()
        self.wfile.write(payload)

    def simulated_fault(self):
        mode = state["mode"]
        if mode == "timeout":
            time.sleep(15)
        if mode == "error500":
            self.send_json(500, {"error": "Error simulado del servidor"})
            return True
        if mode == "badrequest":
            self.send_json(400, {"error": "Solicitud invalida simulada"})
            return True
        return False

    def reminder_id(self):
        parts = self.path.strip("/").split("/")
        return parts[1] if len(parts) == 2 and parts[0] == "reminders" else None

    def do_GET(self):
        if self.path == "/_mode":
            return self.send_json(200, state)
        if self.simulated_fault():
            return
        with lock:
            items = load()
        if self.path == "/reminders":
            return self.send_json(200, sorted(items.values(), key=lambda r: r["createdAt"]))
        reminder_id = self.reminder_id()
        if reminder_id in items:
            return self.send_json(200, items[reminder_id])
        self.send_json(404, {"error": "No existe"})

    def do_PUT(self):
        if self.path.startswith("/_mode/"):
            mode = self.path.split("/")[-1]
            if mode not in MODES:
                return self.send_json(400, {"error": f"Modos validos: {sorted(MODES)}"})
            state["mode"] = mode
            return self.send_json(200, state)
        if self.simulated_fault():
            return
        reminder_id = self.reminder_id()
        length = int(self.headers.get("Content-Length", 0))
        body = json.loads(self.rfile.read(length) or b"{}")
        if not reminder_id or body.get("id") != reminder_id or len(str(body.get("title", "")).strip()) < 4:
            return self.send_json(400, {"error": "Recordatorio invalido"})
        with lock:
            items = load()
            created = reminder_id not in items
            items[reminder_id] = body
            save(items)
        self.send_json(201 if created else 200, body)

    def do_DELETE(self):
        if self.simulated_fault():
            return
        reminder_id = self.reminder_id()
        with lock:
            items = load()
            if reminder_id not in items:
                return self.send_json(404, {"error": "No existe"})
            del items[reminder_id]
            save(items)
        self.send_json(204)

    def log_message(self, fmt, *args):
        with lock:
            total = len(load())
        print(f"{datetime.now():%H:%M:%S} {self.command} {self.path} -> {args[1]} | modo={state['mode']} | registros={total}", flush=True)


if __name__ == "__main__":
    load()
    print(f"Servicio simulado de recordatorios en http://0.0.0.0:{PORT}/ (Ctrl+C para detener)", flush=True)
    ThreadingHTTPServer(("0.0.0.0", PORT), Handler).serve_forever()
