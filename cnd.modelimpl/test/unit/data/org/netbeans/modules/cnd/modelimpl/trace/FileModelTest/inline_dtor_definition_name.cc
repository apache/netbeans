class A {
public:
    A() : a(0) {}
    inline ~A();
private:
    int a;
};

inline A::~A() {
}

