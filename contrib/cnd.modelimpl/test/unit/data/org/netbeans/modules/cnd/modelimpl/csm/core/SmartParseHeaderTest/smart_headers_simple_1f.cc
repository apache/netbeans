// the last one equalis to the subset of the union of all the others

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

// A, B, C, D, E - the same as union of all the others => should replace them
#define A
#define B
#define C
#define D
#define E
#include "smart_headers_simple_1.h"
