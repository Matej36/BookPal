package com.example.bookpal.models

class ModelBook {

    
    var id: String = ""
    var name: String = ""
    var genre: String = ""
    var author: String = ""
    var uid: String = ""
    var bookImage: String = ""
    var timestamp: Long = 0
    var isInMyFavorite: Boolean = false

    constructor()

  
    constructor(
        id: String,
        name: String,
        genre: String,
        author: String,
        uid: String,
        bookImage: String,
        timestamp: Long,
        isInMyFavorite: Boolean
    ) {
        this.id = id
        this.name = name
        this.genre = genre
        this.author = author
        this.uid = uid
        this.bookImage = bookImage
        this.timestamp = timestamp
        this.isInMyFavorite = isInMyFavorite
    }


}
