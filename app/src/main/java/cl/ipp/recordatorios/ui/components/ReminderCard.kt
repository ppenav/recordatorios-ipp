package cl.ipp.recordatorios.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.ipp.recordatorios.model.Category
import cl.ipp.recordatorios.model.Reminder
import cl.ipp.recordatorios.model.SyncStatus

val categoryColors: Map<Category, Color> = mapOf(
    Category.Work to Color(0xFF1565C0),
    Category.Personal to Color(0xFF6A1B9A),
    Category.Health to Color(0xFF2E7D32),
    Category.Study to Color(0xFFEF6C00)
)

@Composable
fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryDot(color = categoryColors[reminder.category] ?: Color.Gray)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${reminder.category.label} - Prioridad ${reminder.priority.label}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                SyncBadge(syncStatus = reminder.syncStatus)
            }
            Spacer(Modifier.width(8.dp))
            AssistChip(onClick = onClick, label = { Text(reminder.status.label) })
        }
    }
}

@Composable
fun SyncBadge(syncStatus: SyncStatus, modifier: Modifier = Modifier) {
    val icon = when (syncStatus) {
        SyncStatus.SYNCED -> Icons.Filled.CheckCircle
        SyncStatus.PENDING -> Icons.Filled.Refresh
        SyncStatus.FAILED -> Icons.Filled.Warning
    }
    val color = when (syncStatus) {
        SyncStatus.SYNCED -> MaterialTheme.colorScheme.onSurfaceVariant
        SyncStatus.PENDING -> MaterialTheme.colorScheme.tertiary
        SyncStatus.FAILED -> MaterialTheme.colorScheme.error
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text = syncStatus.label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun CategoryDot(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(color)
    )
}
