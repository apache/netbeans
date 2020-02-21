struct Base {
    virtual void foo();
};

struct DescA : protected Base {
    void foo();
};

struct DescB : protected DescA {
    void foo();
};

struct DescC : protected DescB {
    void foo();
};
