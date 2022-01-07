import a.Annotation

@Annotation class AnnotationOccurrencesTester {

    @Annotation protected String field

    @Annotation String property

    @Annotation AnnotationOccurrencesTester() {}

    @Annotation public String method() {}
}
