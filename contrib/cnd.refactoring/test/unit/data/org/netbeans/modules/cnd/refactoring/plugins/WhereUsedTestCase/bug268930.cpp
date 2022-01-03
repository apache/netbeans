namespace bug268930 {
    const char* operator "" _my_str(const char *str, unsigned long int length) {
        return str;
    }

    char operator "" _my_chr(char chr) {
        return chr;
    }

    long double operator "" E0l(long double v) {
        return v;
    }

    int usage268930() {
        "abc"_my_str;
        'a'_my_chr;
        .0E+0E0l;
        return 0;
    }
}