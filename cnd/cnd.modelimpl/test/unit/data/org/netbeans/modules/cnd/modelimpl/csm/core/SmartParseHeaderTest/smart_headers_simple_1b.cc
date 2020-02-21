// the 1-st one includes the 2-nd one, the most simple variant

// A B C D E
// + + - - -
// - + - - -
//

#define A
#define B
#include "smart_headers_simple_1.h"

#undef A
#include "smart_headers_simple_1.h"
