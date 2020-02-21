template<class T> class TypedefTemplateClassPar {
    typedef T traits_type;
    typedef typename traits_type::char_type value_type; // error
    value_type v;
    T t;
    void foo() {
        v.inexistent(); // error
        t.inexistent(); // warning - OK
    }
};
