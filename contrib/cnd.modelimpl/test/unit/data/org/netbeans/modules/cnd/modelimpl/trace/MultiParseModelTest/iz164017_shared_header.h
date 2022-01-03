#if !defined(CC_VISIBILITY)
#define CC_VISIBILITY protected
#endif

class BB164017 {
};

class CC164017 {
};

class AA164017 : public BB164017, CC_VISIBILITY CC164017 {
        int common;
#if defined(BRANCH1)
        int branch1; 
#else
        int branch2; 
#endif
};
