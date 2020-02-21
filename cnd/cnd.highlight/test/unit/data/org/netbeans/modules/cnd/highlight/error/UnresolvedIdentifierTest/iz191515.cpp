namespace BBB191515 {
    namespace AAA191515 {
        struct newClass191515 {
            int newClassField;

        };

    }

    struct Inner191515 {
        int field;
    };
}

typedef BBB191515::Inner191515 typeDef191515;

int mai191515n(int argc, char** argv) {
    typedef BBB191515::AAA191515::newClass191515 typeDef191515;

    typeDef191515 var;

    if (var.newClassField == 0) { // resolved
        return 2;
    }
    if (var.field == 1) {// Unresolved
        return 1;
    }
    return 0;
}

int foo191515() {
    typeDef191515 var;
    if (var.field == 1) { // resolved
        return 1;
    }
    if (var.newClassField == 0) { // Unresolved
        return 2;
    }
}
