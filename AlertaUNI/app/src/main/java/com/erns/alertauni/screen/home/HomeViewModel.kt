package com.erns.alertauni.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erns.alertauni.domain.manager.DataStoreHelper

data class MenuItemEntity(
    val title: String,
    val image: String,
)

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dataStoreHelper = DataStoreHelper(application)
    private val menuItems = HashMap<String, MenuItemEntity>()


    fun getMenu(): HashMap<String, MenuItemEntity> {

        menuItems.put("consultas", MenuItemEntity("Consultas", "marcar_tarjeta.jpg"))
        menuItems.put("asistencia", MenuItemEntity("Asistencia", "marcar_tarjeta.jpg"))
        menuItems.put("anuncios", MenuItemEntity("Anuncios", "marcar_tarjeta.jpg"))
        menuItems.put("participacion", MenuItemEntity("Participacion", "marcar_tarjeta.jpg"))
        menuItems.put("chat", MenuItemEntity("Chat", "marcar_tarjeta.jpg"))

        return menuItems
    }


}
