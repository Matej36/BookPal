package com.example.bookpal.models

class ModelQuote {

    
    var id: String = ""
    var bookId: String = ""
    var uid: String = ""
    var quote: String = ""
    var pageNumber: String = ""
    var timestamp: Long = 0
    var isInMyFavorite: Boolean = false


    constructor()

    
    constructor(
        id: String,
        bookId: String,
        uid: String,
        quote: String,
        pageNumber: String,
        timestamp: Long,
        isInMyFavorite: Boolean
    ) {
        this.id = id
        this.bookId = bookId
        this.uid = uid
        this.quote = quote
        this.pageNumber = pageNumber
        this.timestamp = timestamp
        this.isInMyFavorite = isInMyFavorite
    }

}
