
// IZ 138216 : IDE highlights typedef typename ct_imp2<T, ... line as wrong
template <typename T, bool small_>
struct ct_imp2
{
   typedef const T& param_type;
};

template <typename T, bool isp>
struct TT {
    typedef typename ct_imp2<T, sizeof (T) <= sizeof (void*) >::param_type param_type;
};

// IZ 139360 : parser fails on "a->f<A>()"
class A {
public:
    template<class T> void f();
};

int main(int argc, char** argv) {
    A* a = new A();
    a->f<A>(); // ERROR: unexpected token )(RPAREN)
    (*a).f<A>(); // no error
    delete a;
}

// IZ 139358 : parser fails on "static_cast<int* (*)(int)>"
int main2(int argc, char** argv) {
    void *a = 0;
    static_cast<int* (*)(int)>(a); // ERROR: expecting RPAREN, found 'int'
}

// IZ 139564 : regression in reference resolving on CLucene
int sort_getScores (){
    CLHashMap<TCHAR*,float_t,Compare::TChar,Deletor::tcArray, Deletor::DummyFloat>* scoreMap = CLHashMap<TCHAR*,float_t,Compare::TChar,Deletor::tcArray, Deletor::DummyFloat>(true,false);
}

// IZ 139701 : Missed typename keyword in casts and templates
template <typename E1, class T, class A, class S>
const typename flex_string<E1, T, A, S>::size_type
flex_string<E1, T, A, S>::npos = static_cast<typename flex_string<E1, T, A, S>::size_type>(-1);

int foo() {
    A<T, typename Private::Deleter<T>::Type>(pDynObject, longevity, d);
}

// IZ 140117 : parser fails on conditional expression in template arguments
typedef typename boost::mpl::if_c<
        rank<T0>::value < rank<T1>::value,
        T1, T0>::type type;