import java.lang.String

class TypeOccurrencesTester extends String {

    protected String fieldString
    private String[] fieldArrayString
    private List<String> fieldList
    String propertyString
    String[] propertyArrayString
    List<String> propertyList = new ArrayList<String>();


    TypeOccurrencesTester(String constructorParam) {
    }

    TypeOccurrencesTester(String[] constructorParam) {
    }

    TypeOccurrencesTester(List<String> constructorParam) {
    }

    TypeOccurrencesTester(Number wrongConstructorParam) {
    }

    TypeOccurrencesTester(Number[] wrongConstructorParam) {
    }

    TypeOccurrencesTester(List<Number> wrongConstructorParam) {
    }

    public String returnType() {}
    public String[] arrayReturnType() {}
    public List<String> listReturnType() {}
    public void parameterType(String parameterType, Number test) {}
    public void arrayParameterType(String[] parameterType, Number test) {}
    public void listParameterType(List<String> parameterType, Number test) {}
    public Number wrongReturnType() {}
    public Number[] wrongArrayReturnType() {}
    public List<Number> wrongListReturnType() {}

    public void stringUsedInDifferentSituations() {
        String string
        String stringInit = new String()
        String[] stringArray
        String[] stringArrayInit = new String[1]
        List<String> stringList
        List<String> stringListInit = new ArrayList<String>()

        String.CASE_INSENSITIVE_ORDER

        def val = ""
        if (val instanceof String) {
            val.toUpperCase()
        } else if (val instanceof Number) {
            val.intValue() * 2
        }

        for (String sss : somearray) {
            String innerString = sss.concat("");
        }
        for (Number nnn : numbers) {
            Number n = nnn;
        }
    }
}
