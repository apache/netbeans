struct A {
    void foo() {}
};

struct B : A {
   // void foo();
};

void B::foo() {
}

int main() {
    B b;
    b.foo();
    return 0;
}
