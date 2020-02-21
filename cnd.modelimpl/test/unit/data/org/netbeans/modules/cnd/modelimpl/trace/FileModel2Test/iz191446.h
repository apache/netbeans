
#ifndef RECURSIVE_HEADER_H
#define	RECURSIVE_HEADER_H

namespace Outer191446 {
#define AAAAAA
#define INCL_FUN_NAME sin1234
#define TYPE int
#include "iz191446.h"

#undef  AAAAAA
#undef  INCL_FUN_NAME 
#undef  TYPE
#define INCL_FUN_NAME cos1234
#define TYPE double
#include "iz191446.h"
    
}

#else

using namespace Outer191446;
 
#ifdef AAAAAA
TYPE INCL_FUN_NAME(TYPE t, TYPE p);

#else
TYPE INCL_FUN_NAME(TYPE p, TYPE t) {
    return p + v;    
}

#endif

#endif	/* RECURSIVE_HEADER_H */
