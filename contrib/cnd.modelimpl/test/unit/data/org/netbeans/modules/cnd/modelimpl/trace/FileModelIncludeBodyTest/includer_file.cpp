namespace OuterNSIncluder {
    namespace InnnerNSIncluder {
#include "innernsbody.h"
        struct OuterClassIncluder {
            enum OuterClassIncluderEnum {
                #define TOK(X) OuterClassIncluderEnum_##X,
                #include "enumbody.h"
                OuterClassIncluder_NUM_ELEMENTS
            };
#define PART1
#include "outerbody.h"
#undef PART1
            struct InnerClassIncluder {
#include "innerbody.h"

                void booIncluder() {
#include "methodbody.h"
                    localvarIncluder = 1;
                    enum booIncluderEnum {
                        #define TOK(X) booIncluderEnum_##X,
                        #include "enumbody.h"
                        booIncluder_NUM_ELEMENTS
                    };
                }

                enum InnerClassIncluderEnum {
                    #define TOK(X) InnerClassIncluderEnum_##X,
                    #include "enumbody.h"
                    InnerClassIncluder_NUM_ELEMENTS
                };
            };
#define PART2
#include "outerbody.h"
#undef PART2
        };
        enum InnnerNSIncluderEnum {
            #define TOK(X) InnnerNSIncluderEnum_##X,
            #include "enumbody.h"
            InnnerNSIncluder_NUM_ELEMENTS
        };
    }
#include "outernsbody.h"
    enum OuterNSIncluderEnum {
        #define TOK(X) OuterNSIncluderEnum_##X,
        #include "enumbody.h"
        OuterNSIncluderEnum_NUM_ELEMENTS
    };
}
 
enum globalEnum {
    #define TOK(X) GLOBAL_##X,
    #include "enumbody.h"
    GLOBAL_NUM_ELEMENTS
};
