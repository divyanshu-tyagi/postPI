package org.postpi.data

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DataController (
    private val dataService: DataService
){
    @GetMapping("/api/{table}")
    fun getAll(
        @PathVariable table: String,
        @RequestParam params: Map<String, String>
        ): List<Map<String , Any?>>{
        return dataService.findAll(table, params)
    }

    @ExceptionHandler(TableNotFoundException::class)
    fun handleTableNotFound(ex: TableNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to (ex.message ?: "Table not found")))
    }

    @ExceptionHandler(InvalidColumnException ::class)
    fun handleInvalidColumn(ex: InvalidColumnException): ResponseEntity<Map<String, String>>{
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf("errror" to (ex.message ?: "Invalid column")))
    }
}