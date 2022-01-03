class R {
public:
    int r_field;
};
template<class T> class B {
    T t;
public:
    T foo() {
        return t;
    }
};
template <class T> class A {
public:
    typedef T Type;
    typedef B<Type> Type2;
};
typedef A<R> MyA;
MyA::Type2 x;

void foo() {
    x.foo().r_field; // r should be resolved
}
