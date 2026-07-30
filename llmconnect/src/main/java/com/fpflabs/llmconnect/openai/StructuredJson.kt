package com.fpflabs.llmconnect.openai

import kotlinx.serialization.descriptors.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

//  JSON Schema helpers
fun <T> KSerializer<T>.toJsonSchema(): JsonElement =
    descriptor.toSchema()

private fun SerialDescriptor.toSchema(): JsonElement =
    buildJsonObject {
        put("type", "object")

        put(
            "properties",
            buildJsonObject {
                for (i in 0 until elementsCount) {
                    put(
                        getElementName(i),
                        getElementDescriptor(i).toSchemaProperty()
                    )
                }
            }
        )

        putJsonArray("required") {
            for (i in 0 until elementsCount) {
                val descriptor = getElementDescriptor(i)
                if (!descriptor.isNullable && !isElementOptional(i)) {
                    add(getElementName(i))
                }
            }
        }

        put("additionalProperties", false)
    }

private fun SerialDescriptor.toSchemaProperty(): JsonElement {
    val schema = when (kind) {
        PrimitiveKind.STRING ->
            buildJsonObject { put("type", "string") }

        PrimitiveKind.BOOLEAN ->
            buildJsonObject { put("type", "boolean") }

        PrimitiveKind.INT,
        PrimitiveKind.LONG ->
            buildJsonObject { put("type", "integer") }

        PrimitiveKind.FLOAT,
        PrimitiveKind.DOUBLE ->
            buildJsonObject { put("type", "number") }

        StructureKind.LIST ->
            buildJsonObject {
                put("type", "array")
                put("items", getElementDescriptor(0).toSchemaProperty())
            }

        StructureKind.CLASS,
        StructureKind.OBJECT ->
            toSchema()

        else ->
            buildJsonObject { put("type", "string") }
    }

    if (!isNullable) {
        return schema
    }

    val obj = schema.jsonObject

    return buildJsonObject {
        for ((key, value) in obj) {
            if (key != "type") {
                put(key, value)
            }
        }

        putJsonArray("type") {
            add(obj["type"]!!)
            add(JsonPrimitive("null"))
        }
    }
}