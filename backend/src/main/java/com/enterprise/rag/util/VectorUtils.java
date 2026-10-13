package com.enterprise.rag.util;

import com.pgvector.PGvector;

public final class VectorUtils {

    private VectorUtils() {
        // Utility class
    }

    public static PGvector floatArrayToPGvector(float[] array) {
        if (array == null) {
            return null;
        }
        return new PGvector(array);
    }

    public static float[] pgVectorToFloatArray(PGvector vector) {
        if (vector == null) {
            return new float[0];
        }
        return vector.toArray();
    }

    public static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must have the same non-null dimensions");
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
