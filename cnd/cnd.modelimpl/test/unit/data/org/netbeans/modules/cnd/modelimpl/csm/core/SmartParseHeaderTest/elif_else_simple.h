namespace elif_else_simple {
#if        0 < mode && mode <     100
    class Tiny {};
#elif    100 < mode && mode <    1000
    class Small {};
#elif   1000 < mode && mode <   10000
    class Medium {};
#elif  10000 < mode && mode <  100000
    class Large {};
#else
    class Huge {};
#endif
}
