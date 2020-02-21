namespace bug268671 {
    typedef int type268671;
    int value268671 = 1;

    template <typename T>
    T var268671;  

    template <typename T>
    T t1_268671 = 1;  

    template <typename T, int N>
    T t2_268671 = N;  

    template <typename T>
    T t3_268671(value268671);  // variable

    template <typename T>
    T t4_268671(type268671);  // function

    class AAA268671 {      
        template <typename T, int V>  
        static T t5_268671 = V;    
    };      
}  