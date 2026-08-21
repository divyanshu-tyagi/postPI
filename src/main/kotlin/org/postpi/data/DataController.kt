package org.postpi.data

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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

    @PostMapping("/api/{table}")
    fun create(
        @PathVariable table: String,
        @RequestBody body: Map<String , Any?>
    ): ResponseEntity<Map<String, Any?>>{
        val created = dataService.insert(table, body)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @DeleteMapping("/api/{table}/{id}")
    fun delete(
        @PathVariable table: String,
        @PathVariable id: String,
    ): ResponseEntity<Void>{
        dataService.delete(table, id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/api/{table}/{id}")
    fun update(
        @PathVariable table: String,
        @PathVariable id: String,
        @RequestBody body: Map<String , String>
    ):Map<String, Any?>{
         return dataService.update(table, id, body)
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

    @ExceptionHandler(RowNotFoundException::class)
    fun handleRowNotFound(ex: RowNotFoundException): ResponseEntity<Map<String, String>>{
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to (ex.message ?: "Row Not Found")))
    }
}