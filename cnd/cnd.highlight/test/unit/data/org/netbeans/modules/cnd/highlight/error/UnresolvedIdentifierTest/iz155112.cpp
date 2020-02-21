struct A {
    int i;
};
template<class T> struct B {
    typedef T type;
};
template<class T> struct C {
    typedef typename B<T>::type TT;
    TT a;
    void foo() {
        T b;
        b.i++;
        a.i++; // i is highlighted as error
    }
};
int main() {
    C<A> c;
    c.foo();
    return 0;
}