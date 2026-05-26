package pl.wsei.pam.lab06

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            ListViewModel(
                repository = todoApplication().container.todoTaskRepository
            )
        }
        initializer {
            FormViewModel(
                repository = todoApplication().container.todoTaskRepository,
                dateProvider = todoApplication().container.currentDateProvider,
                notificationHandler = todoApplication().container.notificationHandler
            )
        }
    }
}

fun CreationExtras.todoApplication(): TodoApplication {
    return this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TodoApplication
}
