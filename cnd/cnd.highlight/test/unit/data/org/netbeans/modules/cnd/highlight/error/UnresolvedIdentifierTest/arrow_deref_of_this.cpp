struct A1234 {
    int fieldA;
};

struct B1234 {
    A1234* operator->() {
        return (A1234*)0;
    }

    int fieldB;

    void foo();
};

void B1234::foo() {
    this->fieldB;
    this->fieldA;
}
