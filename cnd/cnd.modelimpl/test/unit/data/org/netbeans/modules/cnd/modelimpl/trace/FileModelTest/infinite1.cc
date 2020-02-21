
class InfiniteB : InfiniteA {
    class D {
        
    };
};

namespace N {
    class InfiniteA : InfiniteB {

    };
}

class InfiniteA : InfiniteB {
    void foo();
};

void InfiniteA::foo() {
    C a;
}
