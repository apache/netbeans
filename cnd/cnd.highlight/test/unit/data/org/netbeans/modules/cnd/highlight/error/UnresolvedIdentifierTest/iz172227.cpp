namespace iz172227 {
    template <class T> struct A {
        int i;
    };
    template <> struct A<int> {
        int j;
    };

    template <class T, class T2 = int> struct B {
        A<T2> a;
    };

    void foo() {
        B<char, char> b;
        b.a.i;
        B<char> b2;
        b2.a.j;
    }
}

namespace iz143977_3 {
    class NullType {};
    template <class T1, class T2> struct FactoryImpl {};
    template<class T> struct FactoryImpl<T, NullType> {
        typedef NullType Parm1;
    };
    template<class T, class T2 = NullType> struct Factory {
        typedef FactoryImpl<T, NullType> Impl1;
        typedef FactoryImpl<T, T2> Impl2;
        typedef typename Impl1::Parm1 Parm1; // ok
        typedef typename Impl2::Parm1 Parm1; // unresolced
    };
}

namespace iz143977_2 {
    class NullType {};
    template <class T1, class T2> struct FactoryImpl {};
    template<class T> struct FactoryImpl<T, NullType> {
        typedef NullType Parm1;
    };
    template<class T1, class T2> struct Factory {
        typedef FactoryImpl<T1, NullType> Impl; // reosolved ok
        typedef typename Impl::Parm1 Parm1; // unresolved
    };
}

namespace iz143977_4 {
    typedef int MyInt;
    typedef MyInt NullType;
    template <class T1, class T2> struct FactoryImpl {};
    template<class T> struct FactoryImpl<T, NullType> {
        typedef NullType Parm_null;
    };
    template<class T> struct FactoryImpl<T, int> {
        typedef NullType Parm_int;
    };
    template<class T, class T2 = NullType, class T3 = int> struct Factory {
        typedef FactoryImpl<T, NullType> Impl1;
        typedef FactoryImpl<T, int> Impl2;
        typedef FactoryImpl<T, T2> Impl3;
        typedef FactoryImpl<T, T3> Impl4;
        typedef typename Impl1::Parm_null Parm1;    // Parm_null should be resolved
        typedef typename Impl2::Parm_int Parm2;     // Parm_int should be resolved
        typedef typename Impl3::Parm_null Parm3;    // Parm_null should be resolved
        typedef typename Impl4::Parm_int Parm4;     // Parm_int should be resolved
    };
}

//#include <stdio.h>
namespace iz143977_1 {
    class NullType {};
    class EmptyType {};
    template <class T1, class T2> class DefaultFactoryError {};
    template <class T, class U> struct Typelist {};
    struct FactoryImplBase {
        typedef EmptyType Parm1;
        typedef EmptyType Parm2;
    };
    template <typename AP, typename Id, typename TList>
    struct FactoryImpl {
    };
    template<typename AP, typename Id>
    struct FactoryImpl<AP, Id, NullType>
            : public FactoryImplBase
    {
    };
    template <typename AP, typename Id, typename P1 >
    struct FactoryImpl<AP,Id, Typelist<P1, NullType> >
                : public FactoryImplBase
    {
        virtual ~FactoryImpl() {}
        virtual AP* CreateObject(const Id& id,Parm1 ) = 0;
    };
    template
    <
        class AbstractProduct,
        typename IdentifierType,
        typename CreatorParmTList = NullType,
        template<typename, class> class FactoryErrorPolicy = DefaultFactoryError
    >
    class Factory : public FactoryErrorPolicy<IdentifierType, AbstractProduct>
    {
        typedef FactoryImpl< AbstractProduct, IdentifierType, CreatorParmTList > Impl;

        typedef typename Impl::Parm1 Parm1; // Parm1 should be resooved
        typedef typename Impl::Parm2 Parm2; // Parm2 should be resooved
    };
}