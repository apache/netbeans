namespace iz146560 {

    class A {};
    struct B {};
    enum C {};
    union D {};

    void test() {
        class A* pa = new class A[1];
        struct B* pb = new struct B[2];
        enum C* pc = new enum C[3];
        union D* pd = new union D[4];
    }

}
