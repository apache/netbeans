class AAA {
public:
    class inner;
};

class AAA::inner {
    virtual void foo() = 0;
};

class BBB : public AAA::inner {
    void foo();
};