namespace bug246534 {
    typedef char CharType246534;

    struct AAA246534 {
        int field;
    };

    int checkCpp11_246534() {
        int var1{1 + 3};
        int var2{var1 * 5};
        int var3{5};
        int array1[] = {var1, var2, 5, var3};
        const CharType246534 array2 [][2] = {{(char)var1, (char)var2}, {1, 3}};
        AAA246534 varAAA1 = {(CharType246534){42}};
        AAA246534 varAAA2 = {.field =  (CharType246534){(char)var1} + (int){var2} + var3};        
        return varAAA1.field + (CharType246534){varAAA2.field} + (int){var1 + 42};
    }
}