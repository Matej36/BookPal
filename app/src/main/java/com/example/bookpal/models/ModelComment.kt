package com.example.bookpal.models

class ModelComment {

    //variables, should be with same spellings and type as we added in firebase
    var id: String = ""
    var bookId: String = ""
    var timestamp: Long = 0
    var comment: String = ""
    var uid: String = ""

    //empty constructor, required by firebase, if not added the app will crash
    constructor()

    //all param constructor
    constructor(id: String, bookId: String, timestamp: Long, comment: String, uid: String) {
        this.id = id
        this.bookId = bookId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }


}