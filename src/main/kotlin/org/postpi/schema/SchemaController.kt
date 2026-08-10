package org.postpi.schema

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SchemaController (
    private val schemaIntrospector: SchemaIntrospector
){
    @GetMapping("/schema")
    fun getSchema(): List<TableSchema>{
        return schemaIntrospector.introspect()
    }
}