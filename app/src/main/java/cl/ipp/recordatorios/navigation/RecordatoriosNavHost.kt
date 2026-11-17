package cl.ipp.recordatorios.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.ipp.recordatorios.ui.detail.ReminderDetailScreen
import cl.ipp.recordatorios.ui.detail.ReminderDetailViewModel
import cl.ipp.recordatorios.ui.form.ReminderFormScreen
import cl.ipp.recordatorios.ui.form.ReminderFormViewModel
import cl.ipp.recordatorios.ui.list.ReminderListScreen
import cl.ipp.recordatorios.ui.list.ReminderListViewModel
import cl.ipp.recordatorios.ui.settings.SettingsScreen
import cl.ipp.recordatorios.ui.settings.SettingsViewModel

object Routes {
    const val LIST = "lista"
    const val CREATE = "crear"
    const val SETTINGS = "ajustes"
    const val ARG_ID = "id"
    const val EDIT = "editar/{$ARG_ID}"
    const val DETAIL = "detalle/{$ARG_ID}"

    fun edit(id: String) = "editar/$id"
    fun detail(id: String) = "detalle/$id"
}

@Composable
fun RecordatoriosNavHost(navController: NavHostController = rememberNavController()) {
    val idArgument = listOf(navArgument(Routes.ARG_ID) { type = NavType.StringType })
    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            ReminderListScreen(
                viewModel = viewModel(factory = ReminderListViewModel.Factory),
                onCrearRecordatorio = { navController.navigate(Routes.CREATE) { launchSingleTop = true } },
                onRecordatorioClick = { id -> navController.navigate(Routes.detail(id)) },
                onAjustes = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } }
            )
        }
        composable(Routes.CREATE) {
            ReminderFormScreen(
                viewModel = viewModel(factory = ReminderFormViewModel.Factory),
                onGuardar = { navController.popBackStack() },
                onCancelar = { navController.popBackStack() }
            )
        }
        composable(Routes.EDIT, arguments = idArgument) {
            ReminderFormScreen(
                viewModel = viewModel(factory = ReminderFormViewModel.Factory),
                onGuardar = { navController.popBackStack() },
                onCancelar = { navController.popBackStack() }
            )
        }
        composable(Routes.DETAIL, arguments = idArgument) {
            ReminderDetailScreen(
                viewModel = viewModel(factory = ReminderDetailViewModel.Factory),
                onEditar = { id -> navController.navigate(Routes.edit(id)) },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel(factory = SettingsViewModel.Factory),
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
