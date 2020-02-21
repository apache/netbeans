namespace rnud {
    namespace A {
        class B {
            public:
            class C {

            };
        };
    }
}

namespace rnud_2 {
    using rnud::A::B;
    namespace A {

        class AA : public B {
        };
    }
    namespace C {
        static const int c = 5;
        struct CC {

            class CCC : public B::C {
                static const int b = 5;
                public: int boo();
            };
            C::CC::CCC cc;
            int foo();
        };

    }

    using C::CC;
}


namespace rnud_2 {
    int CC::foo() {
        return c;
    }
}

using namespace ::rnud_2;

int CC::CCC::boo() {

    return b;
}

using rnud::A::B;

int main () {
    B b;
    B::C cc;
    using rnud_2::A::AA;
    B* bb = new AA();
    using rnud_2::C::c;
    return c;
}
