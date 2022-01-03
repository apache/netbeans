class A {
public:
    static int var;
};

int A::var=10;

int main(int argv) {
    A a;
    a.var=4;
    return 1;
}

