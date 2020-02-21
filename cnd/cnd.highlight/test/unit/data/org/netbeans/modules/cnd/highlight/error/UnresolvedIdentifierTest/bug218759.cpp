struct ST {
    mutable unsigned int i1;
    unsigned mutable int i2;
    int unsigned mutable i3;
};

class CL {
public:
    int virtual unsigned i3() {
        return 0;
    }
    unsigned int virtual i4() {
        return 0;
    }
};