
#include "inc210983_1.h"

int foo210983(struct Interp210983* i) {
    struct env210983 *e = i->env210983;
    int *a = i->env210983->savefd;  //unresolved savefd
    int *b = e->oenv->savefd;
    return *a+*b;
}
