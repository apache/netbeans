namespace iz143977_3 {
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
