package com.github.rawsanj.aws.broker.aws.model

import javax.persistence.AttributeConverter

class ObjectToStringConverter : AttributeConverter<Any, String> {

    override fun convertToDatabaseColumn(attribute: Any): String {
        return attribute.toString()
    }

    override fun convertToEntityAttribute(dbData: String): Any {
        return dbData
    }
}