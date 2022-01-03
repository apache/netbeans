#define FUN_NAME(name, x, y) name PARAM(x,y) {

#ifdef MUL

#define PARAM(x, y) (int x, int y)
int FUN_NAME(mul, first, second)
    int result;
    result = first * second;
    return result;
}

#endif

#ifdef ADD

#define PARAM(x, y) (double x, double y)

int FUN_NAME(add, first, second)
    int result;
    result = first + second;
    return result;
}

#endif
