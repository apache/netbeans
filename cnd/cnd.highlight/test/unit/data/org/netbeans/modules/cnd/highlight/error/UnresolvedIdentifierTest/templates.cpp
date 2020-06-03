template <class T> class A {
public:
    typedef T* pointer;
    typedef pointer p;
    T foo(T t) {
        t.sayHi();
        p tt = new T();
        tt->sayHi();
        T::sayHiStatic();
    }
    void bar() {
        A a;
        foo(a).doSomething();
        T::n::m;
    }
    typename T::template Rebind<T> rebind;
};

template <template<typename> class T> class C {
    typedef int myint;
    T<myint>::foo var;
};
