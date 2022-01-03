namespace rng {

    namespace A {
        struct AA {
        };
    }
    namespace C {
        static const int c = 5;
        struct CC {

            class CCC {
                int a;
                static const int b = 5;
                public: int boo();
            };
            CC::CCC cc;
            int foo();
        };

    }

    namespace B = A;    // namespace alias

    using namespace A;  // using directive
    using C::CC;        // using declaration

    class ZZ {
        AA a;           // a is of type A::AA
        B::AA b;        //  b is of type A::AA
        CC c;           // c is of type C::CC
    };

    ZZ z;

    using rng::C::CC;
}


namespace rng {
    int CC::foo() {
        int a = c;
        return a;
    }

    int CC::CCC::boo() {
        int a = c + b;
        return a;
    }
}
