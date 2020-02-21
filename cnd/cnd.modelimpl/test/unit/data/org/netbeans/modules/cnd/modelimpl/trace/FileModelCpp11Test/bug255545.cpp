namespace bug255545 {
    template <int T1, int T2 = decltype(T1){1}>
    struct A255545 {
        int foo255545();
    };  
    int roo255545(int param = decltype(5){1});
}