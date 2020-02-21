
class ClassWithIncludedBody {

#define FOO
    #include "class_body_included.h"
#undef FOO
#define BOO
    #include "class_body_included.h"
#undef BOO
#include "class_body_included.h"

};
