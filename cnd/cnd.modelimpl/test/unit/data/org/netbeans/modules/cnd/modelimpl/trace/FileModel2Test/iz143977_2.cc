namespace iz143977_2 {
    class NullType {};
    template <class T1, class T2> struct FactoryImpl {};
    template<class T> struct FactoryImpl<T, NullType> {
        typedef NullType Parm1;
    };
    template<class T1, class T2> struct Factory {
        typedef FactoryImpl<T1, NullType> Impl;
        typedef typename Impl::Parm1 Parm1; // should be resolved
    };
}
//using namespace iz143977_2;
//void foo_iz143977_2() {
//    Factory<int,int> f2;
//}
