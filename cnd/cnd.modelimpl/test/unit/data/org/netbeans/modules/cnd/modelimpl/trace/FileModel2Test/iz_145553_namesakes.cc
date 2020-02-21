class Class {
public:
    struct Inner {
        void foo_inner_1();
    };
    struct Inner1 {
    };
    Class();
    Class(const Class& orig);
    virtual ~Class();
    void foo_1();
};

class Derived : public Class { // Class should hyperlink to ::Class
    Class other; // Class should hyperlink to ::Class
public:
    Derived() {
        other.foo_1(); // should be resolved
        Class::Inner inner; // Inner should hyperlink to ::Class::Inner
        inner.foo_inner_1(); // should be resolved
    }
    Derived(const Derived& orig);
    virtual ~Derived() {
    }
};

namespace AAA {
    class Class {
    public:
        struct Inner {
            void foo_inner_2();
        };
        struct Inner2 {
        };
        Class();
        Class(const Class& orig);
        virtual ~Class();
        void foo_2();
    };

    class Derived : public Class { // Class should hyperlink to AAA::Class
        Class other; // Class should hyperlink to AAA::Class
    public:
        Derived() {
            other.foo_2(); // should be resolved
            Class::Inner inner; // Inner should hyperlink to AAA::Class::Inner
            inner.foo_inner_2(); // should be resolved
        }
        Derived(const Derived& orig);
        virtual ~Derived() {
        }
    };
}
