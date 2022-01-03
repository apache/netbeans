namespace bug252427 {
    struct AAA252427 {
        virtual int foo(const float *);
    };

    typedef float float_t252427;

    struct BBB252427 : AAA252427 { 
        int foo(float_t252427); 
        int foo(const float_t252427*);
    };                        

    typedef const float const_float_t252427;

    struct CCC252427 : BBB252427 { 
        int foo(float_t252427*); 
        int foo(const_float_t252427*);
    };                        

    typedef const float * const_float_t_ptr252427;

    struct DDD252427 : CCC252427 { 
        int foo(const_float_t252427); 
        int foo(const_float_t_ptr252427);
    };
}