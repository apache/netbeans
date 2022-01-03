// All the three are the same => should be parsed only once

// A B C D E
// + + - - -
// + + - - -
// + + - - -

#define A
#define B
#include "smart_headers_simple_1.h"

#include "smart_headers_simple_1.h"

#include "smart_headers_simple_1.h"
