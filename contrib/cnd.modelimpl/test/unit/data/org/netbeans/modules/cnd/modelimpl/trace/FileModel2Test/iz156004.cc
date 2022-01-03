# define PRT_UNIT_NAMES {"pc", "in", "mm", "pt"}
int foo() {
    static char *(units[4]) = PRT_UNIT_NAMES;
    return 0;
}
