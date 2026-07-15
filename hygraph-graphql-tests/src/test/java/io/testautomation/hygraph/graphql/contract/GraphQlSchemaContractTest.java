package io.testautomation.hygraph.graphql.contract;

import com.fasterxml.jackson.core.type.TypeReference;
import graphql.introspection.IntrospectionQuery;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.Document;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.hygraph.classification.GraphQlPlatformFeature;
import io.testautomation.hygraph.classification.Hygraph;
import io.testautomation.hygraph.graphql.client.GraphQlClient;
import io.testautomation.hygraph.graphql.model.GraphQlRequest;
import io.testautomation.hygraph.graphql.model.GraphQlResponse;
import io.testautomation.hygraph.graphql.model.GraphQlResult;
import io.testautomation.hygraph.reporting.ReportGroup;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.testautomation.hygraph.assertions.GraphQlAssertions.assertSuccessful;
import static org.assertj.core.api.Assertions.assertThat;

@Hygraph
@GraphQlPlatformFeature
@ReportGroup("Schema contract")
class GraphQlSchemaContractTest {
    private static final String INTROSPECTION_QUERY = IntrospectionQuery.INTROSPECTION_QUERY
            .replace("  isOneOf\n", "")
            .replace("args(includeDeprecated: true)", "args")
            .replace("inputFields(includeDeprecated: true)", "inputFields")
            .replace("  isDeprecated\n  deprecationReason\n", "");
    private static final TypeReference<GraphQlResponse<Map<String, Object>>> INTROSPECTION_RESPONSE =
            new TypeReference<>() {
            };

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldValidateAllExecutableDocumentsAgainstPublishedSchema(GraphQlClient client) throws IOException {
        GraphQlResult<Map<String, Object>> introspection = client.execute(
                new GraphQlRequest(INTROSPECTION_QUERY, Map.of(), "IntrospectionQuery"),
                INTROSPECTION_RESPONSE);
        assertSuccessful(introspection);
        assertThat(introspection.body().data()).isNotNull().containsKey("__schema");

        Document schemaDocument = new IntrospectionResultToSchema()
                .createSchemaDefinition(introspection.body().data());
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(
                new SchemaParser().buildRegistry(schemaDocument));
        Map<Path, List<ValidationError>> failures = validateDocuments(schema);

        assertThat(failures).as("GraphQL documents incompatible with the published Hygraph schema").isEmpty();
    }

    private static Map<Path, List<ValidationError>> validateDocuments(GraphQLSchema schema) throws IOException {
        Path resources = moduleDirectory().resolve("src/main/resources/graphql");
        Map<Path, List<ValidationError>> failures = new LinkedHashMap<>();
        try (var paths = Files.walk(resources)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".graphql"))
                    .filter(path -> !path.toString().contains("/validation/"))
                    .sorted()
                    .forEach(path -> validateDocument(schema, resources, path, failures));
        }
        return failures;
    }

    private static void validateDocument(
            GraphQLSchema schema,
            Path resources,
            Path documentPath,
            Map<Path, List<ValidationError>> failures) {
        try {
            Document document = Parser.parse(Files.readString(documentPath));
            List<ValidationError> errors = new Validator().validateDocument(schema, document, Locale.ENGLISH);
            if (!errors.isEmpty()) {
                failures.put(resources.relativize(documentPath), errors);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read GraphQL document: " + documentPath, exception);
        }
    }

    private static Path moduleDirectory() {
        String directory = System.getProperty("hygraph.module.directory");
        if (directory == null || directory.isBlank()) {
            throw new IllegalStateException("Missing hygraph.module.directory system property");
        }
        return Path.of(directory);
    }
}
