package com.ebc.crud_notes.viewMmodels

import com.ebc.crud_notes.states.ImagePathState

sealed class Event {
    data class SetText(val text: String) : Event()

    object OpenDialog: Event()

    object CloseDialog: Event()

    object Save: Event()

    data class Delete(val id: Int?) : Event()

    data class Load(val id: Int?) : Event()

    data class SetImagePath(val imagePath: String?) : Event()
}