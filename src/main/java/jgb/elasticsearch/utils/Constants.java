package jgb.elasticsearch.utils;

/**
 * @author txoppe
 * @since 04/07/2017 | 19:16
 */
public class Constants {
    public static class Elastic {
        public static final String INDEX_CATALOG = "catalog";
        public static final String TYPE_BOOKS = "books";
        public static final String TYPE_AUTHORS = "authors";
        public static final String FIELD_TITLE = "title";
        public static final String FIELD_CATEGORIES = "categories";
        public static final String FIELD_PUBLISHER = "publisher";
        public static final String FIELD_DESCRIPTION = "description";
        public static final String FIELD_PUBLISHED_DATE = "published_date";
        public static final String FIELD_ISBN = "isbn";
        public static final String FIELD_RATING = "rating";
        public static final String FIELD_NAME = "name";
        public static final String FIELD_FIRST_NAME = "first_name";
        public static final String FIELD_LAST_NAME = "last_name";
        public static final String FIELD_CATEGORIES_NAME = "categories.name";
    }
}
