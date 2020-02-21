namespace bug251214 {
    struct AAA251214 {
        AAA251214(int a, int b);
    };

    struct BBB251214 {
        BBB251214(int a, AAA251214 b);
    };

    struct CCC251214 {
        CCC251214(int x, BBB251214 y, int z);
    };
    
    struct DDD251214 : CCC251214 {
        int xx;
        DDD251214() : CCC251214(1, {1, {1, 2}}, 2), xx{4} {
            xx = 5;
        }
    };

    void funA251214(BBB251214 x, int y);

    void funB251214(int x, BBB251214 y);

    BBB251214 testvar251214(1, {2, 3});
    BBB251214 *ptestvar251214 = new BBB251214(1, {2, 3});

    int foo() {
        BBB251214 testvar1(1, {2, 3});
        BBB251214 *ptestvar1 = new BBB251214(1, {2, 3});
        CCC251214 testvar2(1, {1, {1, 2}}, 2);
        CCC251214 *ptestvar2 = new CCC251214(1, {1, {1, 2}}, 2);
        funA251214({1, {2, 3}}, 1);
        funB251214(1, {1, {2, 3}});
    }
}