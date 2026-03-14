package com.progweb.recipify.datamodels

class ItemsModel(
    // Esta es una solución temporal, todavía falta complementar.
    // En esta carpeta van todos los tipos de datos/interfaces que se tengan que heredar
    // directamente de alguna base de datos.
    var titulo: String ="" ,
    var descr:  String = "",
    var img: ArrayList<String> = ArrayList(),
    var savedtimes: Int=0,
    )