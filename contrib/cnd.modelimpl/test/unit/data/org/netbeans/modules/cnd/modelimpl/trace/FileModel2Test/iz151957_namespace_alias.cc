namespace A {
    int i;
}

struct definition {
    definition() {
        using namespace A;
        namespace B = A; // 'namespace' is highlighted as error
        B::i;
    }
};
