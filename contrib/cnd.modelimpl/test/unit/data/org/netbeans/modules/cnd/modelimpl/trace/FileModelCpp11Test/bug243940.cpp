namespace bug243940 {
    enum class E1_243940;
    enum class E2_243940 : int;
    enum class E3_243940 : short;
    enum E4_243940 : int;
    enum E5_243940 : short;
    namespace N243940 {
        enum class E6_243940;
    }    
    enum {
        XXX_243940
    };
    enum N243940::E6_243940 e6_1_243940; //ok
    enum ::bug243940::N243940::E6_243940 e6_2_243940; //ok    
}

