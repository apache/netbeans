// the last one includes all the rest

// A B C D E
// - - + - -
// + + - - -
// - + - + -
// + + + + +   the best one

#define C
#include "smart_headers_simple_1.h"
#undef C

#define A
#define B
#include "smart_headers_simple_1.h"

#undef A
#define D
#include "smart_headers_simple_1.h"

#define A
#define B
#define C
#define D
#define E
#include "smart_headers_simple_1.h"
