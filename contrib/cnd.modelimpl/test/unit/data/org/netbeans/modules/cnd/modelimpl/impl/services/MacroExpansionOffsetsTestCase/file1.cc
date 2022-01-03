
#include "file1.h"

int main() {
    int i = CONSTANT;
}

#define EMPTY

int foo() {
    // ...
    EMPTY
    // ...
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