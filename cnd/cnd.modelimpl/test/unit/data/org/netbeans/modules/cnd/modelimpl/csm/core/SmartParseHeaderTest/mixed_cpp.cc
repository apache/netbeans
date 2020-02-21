#define __cplusplus 1998 // in tests we have an issue with getting macros
#include "mixed_header.h"

MixedClass1* getMixedClass() {
    return new MixedClass1();
}
