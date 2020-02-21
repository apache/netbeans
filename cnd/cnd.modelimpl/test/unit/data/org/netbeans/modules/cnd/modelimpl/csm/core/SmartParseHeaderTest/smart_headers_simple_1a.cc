// Just 3 different parses to be merged:

// A B C D E
// + - - - -
// - + - - -
// - - + - -

#define A
#include "smart_headers_simple_1.h"

#undef A
#define B
#include "smart_headers_simple_1.h"

#undef B
#define C
#include "smart_headers_simple_1.h"
