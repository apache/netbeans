
#include "file1.h"

#define B(x) x + x
#define A(x) B(x) + x

int main() {
    int i = CONSTANT;
}

#define EMPTY

int foo() {
    // ...
    EMPTY
    // ...

    A(1);
}

// ...

#ifdef CONSTANT
// ...
int var;
#endif

#ifdef ZZZ
// ...
int var2;
#endif

#define ONE 1
#define TWO 2
#define min(x, y) ((x) < (y)) ? (x) : (y)

int a = min(ONE, TWO);