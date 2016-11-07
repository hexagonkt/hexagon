package co.there4.hexagon.rest

import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.MongoRepository
import co.there4.hexagon.web.Exchange
import java.net.InetAddress.getByName as address

import co.there4.hexagon.web.Server
import co.there4.hexagon.web.server

fun <T : Any, K : Any> Server.crud(repository: MongoIdRepository<T, K>, readOnly: Boolean = false) {
    RestCrud (repository, this, readOnly).install()
}

fun <T : Any, K : Any> crud(repository: MongoIdRepository<T, K>, readOnly: Boolean = false) {
    server.crud (repository, readOnly)
}

fun <T : Any> Server.crud(repository: MongoRepository<T>, readOnly: Boolean = false) {
    RestBaseCrud (repository, this, readOnly).install()
}

fun <T : Any> crud(repository: MongoRepository<T>, readOnly: Boolean = false) {
    server.crud (repository, readOnly)
}

fun Server.files(path: String = "/files", fileName: Exchange.() -> String) {
    get(path) { }
    put(path) { }
    post(path) { }
    delete(path) { }
}
