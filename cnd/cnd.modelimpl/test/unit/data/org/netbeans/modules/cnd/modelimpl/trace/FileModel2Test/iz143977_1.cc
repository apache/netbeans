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
using namespace iz143977_1;
//int main(int argc, char** argv) {
//    Factory<int, int> f2;
//    return 0;
//}
