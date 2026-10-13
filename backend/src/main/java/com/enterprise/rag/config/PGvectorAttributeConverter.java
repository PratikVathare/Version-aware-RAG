package com.enterprise.rag.config;

import com.pgvector.PGvector;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

@Converter(autoApply = true)
public class PGvectorAttributeConverter implements AttributeConverter<PGvector, Object> {

    @Override
    public Object convertToDatabaseColumn(PGvector attribute) {
        return attribute;
    }

    @Override
    public PGvector convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        if (dbData instanceof PGvector pgVector) {
            return pgVector;
        }
        if (dbData instanceof PGobject pgObject) {
            try {
                return new PGvector(pgObject.getValue());
            } catch (SQLException e) {
                throw new IllegalArgumentException("Failed to convert PGobject to PGvector: " + pgObject.getValue(), e);
            }
        }
        if (dbData instanceof String str) {
            try {
                return new PGvector(str);
            } catch (SQLException e) {
                throw new IllegalArgumentException("Failed to convert String to PGvector: " + str, e);
            }
        }
        throw new IllegalArgumentException("Unsupported vector database type: " + dbData.getClass().getName());
    }
}
