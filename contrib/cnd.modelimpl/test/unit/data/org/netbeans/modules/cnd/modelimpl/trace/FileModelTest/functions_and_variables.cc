// #include <stdio.h>
// #include <stdlib.h>


//
// Variables
//

static int A;
int* pA(&A);
int** ppA(&pA);
int*** pppA(&ppA);

int f2(*pA);
int f3(**ppA);
int f4(***pppA);

int a(A);
int* pa(&A);
int foo_pa(*pa);

int foo_1(A);

int foo_2(optind);

namespace qwe {
    int q;
    namespace asd {
        int a;
        namespace zxc {
            int z;
            int qweqwe(q);
            int asdasd(a);
            int zxczxc(a);
        }
    }
}

using namespace qwe;
using namespace qwe::asd;
using namespace qwe::asd::zxc;

// IZ 136165 : Parameter type is not resolved in contained class
class NewClass {
public:
    static int AAA;
    static int BBB;
private:

};

int NewClass::AAA = 10;
int NewClass::BBB(AAA);



// IZ 139425 : Wrong rendering of declarations with const qualifier
class C1{
public:
    C1(int i){
        
    }
};

int t = 1;

C1 c1(t);
const C1 c2(t);
int foo() {
    C1 c3(t);
    const C1 c4(t);
}


// IZ 146030 : set of problems for declarations in Loki (usecase 4)
static void write_five() {
    global_int = 5;
}
function<void() > static_func2(write_five);

// IZ#151530 : Variable is resolved as function
class T {
public:
    int i;
    T(int) {}
};
template <class P> P foo() {
    return 0;
};
T v(foo<int>()); // resolved as function declaration
int main() {
    v.i = 0; // unresolved i
    return 0;
}


//
// Functions
//

class C {};

int f11(C);
int f23(C*);

int main(int argc, char** argv) {
    int foo;
    foo = qweqwe;
    foo = asdasd;
    foo = zxczxc;
    return foo;
}

//Some more variables
int pBa(*bbbb);
int ba(&bbbbb);
