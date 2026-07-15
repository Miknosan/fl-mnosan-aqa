package io.testautomation.hygraph.features.moviecatalog;

import io.testautomation.hygraph.framework.metadata.HygraphFeature;
import io.testautomation.hygraph.framework.metadata.HygraphTags;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@HygraphFeature("Movie Catalog")
@Tag(HygraphTags.MOVIE_CATALOG)
@ExtendWith(MovieCatalogExtension.class)
public @interface MovieCatalogFeature {
}
