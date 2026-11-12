package cl.ipp.recordatorios.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.ipp.recordatorios.ui.create.CreateReminderScreen
import cl.ipp.recordatorios.ui.create.CreateReminderViewModel
import cl.ipp.recordatorios.ui.detail.ReminderDetailScreen
import cl.ipp.recordatorios.ui.detail.ReminderDetailViewModel
import cl.ipp.recordatorios.ui.list.ReminderListScreen
import cl.ipp.recordatorios.ui.list.ReminderListViewModel

object Routes {
    const val LIST = "lista"
    const val CREATE = "crear"
    const val ARG_ID = "id"
    const val DETAIL = "detalle/{$ARG_ID}"

    fun detail(id: Int) = "detalle/$id"
}

@Composable
fun RecordatoriosNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            ReminderListScreen(
                viewModel = viewModel(factory = ReminderListViewModel.Factory),
                onCrearRecordatorio = { navController.navigate(Routes.CREATE) { launchSingleTop = true } },
                onRecordatorioClick = { id -> navController.navigate(Routes.detail(id)) }
            )
        }
        composable(Routes.CREATE) {
            CreateReminderScreen(
                viewModel = viewModel(factory = CreateReminderViewModel.Factory),
                onGuardar = { navController.popBackStack() },
                onCancelar = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.IntType })
        ) {
            ReminderDetailScreen(
                viewModel = viewModel(factory = ReminderDetailViewModel.Factory),
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
