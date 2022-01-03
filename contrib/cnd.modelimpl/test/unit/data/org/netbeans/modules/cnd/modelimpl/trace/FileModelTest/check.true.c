// simulate C mode
#undef __cplusplus 

#define TRUE true
#define FALSE false

#if (TRUE)
int TRUE_PARENS_visible;
#endif
#if TRUE
int TRUE_visible;
#endif

#if (2)
int _2_PARENS_visible;
#endif
#if 2
int _2_visible;
#endif

#if (1)
int _1_PARENS_visible;
#endif
#if 1
int _1_visible;
#endif

#if (0)
int _0_PARENS_invisible;
#endif
#if 0
int _0_invisible;
#endif

#if (FALSE)
int FALSE_PARENS_invisible;
#endif
#if FALSE
int FALSE_invisible;
#endif
