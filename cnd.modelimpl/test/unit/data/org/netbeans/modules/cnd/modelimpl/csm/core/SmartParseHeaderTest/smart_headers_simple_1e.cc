// the last one is a subset of the union of all the others

// A B C D E
// + + - - -
// - - + + -
// - - - - +
// + + + - +  is included

#define A
#define B
#include "smart_headers_simple_1.h"

#undef A
#undef B
#define C
#define D
#include "smart_headers_simple_1.h"

#undef C
#undef D
#define E
#include "smart_headers_simple_1.h"

// A, B, C, E - the largest, but less than the union of others
#define A
#define B
#define C
#include "smart_headers_simple_1.h"

