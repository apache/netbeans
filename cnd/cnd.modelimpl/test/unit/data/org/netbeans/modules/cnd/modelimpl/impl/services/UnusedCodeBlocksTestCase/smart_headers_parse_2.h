//
// should be included twice:
// 1. A defined, B undefined
// 2. A undefined, B defined
//

#if defined(A) && defined(B)
    int a_or_b;         // passive
#endif

#if !defined(A)
    int a1;             // active
#   if !defined (B)
        int a_and_b;    // passive
#   endif
    int a2;             // active
#endif

#if !defined(B)
    int b1;             // active
#   if !defined (A)
        int b_and_a;    // passive
#   endif
    int b2;             // active
#endif
