namespace bug251621 {
    struct AAA251621 {
        AAA251621 virtual foo251621();
        AAA251621 const virtual boo251621();
        AAA251621 virtual const roo251621();
        const AAA251621 virtual too251621();
        AAA251621 virtual const * const loo1_251621(int const *src);
        AAA251621 virtual __attribute((noreturn)) const * const loo2_251621(int const *src);
        AAA251621 virtual * zoo251621();
        int virtual const volatile noo251621();
        AAA251621 virtual inline const volatile * ptrfun251621();
        virtual void normal_function251621();
    };
}