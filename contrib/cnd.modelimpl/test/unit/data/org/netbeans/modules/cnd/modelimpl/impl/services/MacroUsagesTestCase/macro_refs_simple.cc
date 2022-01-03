
#define MACRO

int VAL = 100;
int foo() {
    return VAL;
}

#define VAL 1

#ifdef MACRO
int i = 10 + VAL;
#endif

int foo() {
    return VAL2;
}
