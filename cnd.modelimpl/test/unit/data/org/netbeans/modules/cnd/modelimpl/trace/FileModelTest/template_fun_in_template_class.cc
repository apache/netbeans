template<class T1> class Object {
public:
    template<class T2> Object<T1> useT2Declaration(T2 o);
    template<class T3> Object<T1> useT3Definition(T3 o) {
    }
};


