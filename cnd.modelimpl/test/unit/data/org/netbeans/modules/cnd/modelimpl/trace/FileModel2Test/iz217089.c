#define TYPE i##n##t
#define AAA D1 ## 0
#define BBB 1 ## 0

#if BBB == 10
TYPE AAA;
#endif
