package io.testautomation.hygraph.graphql.client;

public final class GraphQlResponseDeserializationException extends IllegalStateException {
    private static final int MAX_RESPONSE_EXCERPT = 1000;
    private final String operationName;
    private final int statusCode;
    private final String responseBody;

    GraphQlResponseDeserializationException(
            String operationName,
            int statusCode,
            String responseBody,
            Throwable cause) {
        super(message(operationName, statusCode, responseBody), cause);
        this.operationName = operationName;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public String operationName() {
        return operationName;
    }

    public int statusCode() {
        return statusCode;
    }

    public String responseBody() {
        return responseBody;
    }

    private static String message(String operationName, int statusCode, String responseBody) {
        String normalized = responseBody == null ? "" : responseBody.replaceAll("\\s+", " ").trim();
        String excerpt = normalized.length() <= MAX_RESPONSE_EXCERPT
                ? normalized
                : normalized.substring(0, MAX_RESPONSE_EXCERPT);
        return "Cannot deserialize GraphQL response for operation " + operationName
                + " with HTTP status " + statusCode
                + "; response excerpt: " + excerpt;
    }
}
