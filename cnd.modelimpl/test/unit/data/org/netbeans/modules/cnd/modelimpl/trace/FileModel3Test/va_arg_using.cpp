typedef __builtin_va_list va_list;
#define va_start(v,l)	__builtin_va_start(v,l)
#define va_end(v)	__builtin_va_end(v)
#define va_arg(v,l)	__builtin_va_arg(v,l)

void myprintf(const wchar_t * format, ...){
    va_list ap;
    va_start(ap, format);
    int fieldlen = va_arg(ap, int);
    const wchar_t *wstr = va_arg(ap,const wchar_t *);
    va_end(ap); 
}

namespace A {
	struct C {
	};
}

class B {
};

void cf(const A::C* cc, ...){
    va_list ap;
    va_list *p_va = &ap;
    va_start(ap, cc);
    int fieldlen = va_arg(ap, int);
    (void) va_arg(*p_va, char *);
    (void) va_arg(*p_va, short *);
    (void) va_arg(*p_va, unsigned short *);
    (void) va_arg(*p_va, int *);
    (void) va_arg(*p_va, long *);
    (void) va_arg(*p_va, float *);
    (void) va_arg(*p_va, double *);
    (void) va_arg(*p_va, char *);
    (void) va_arg(*p_va, char **);
    B  b1 = va_arg(ap, B);
    const A::C *c = va_arg(ap,const A::C*);
    A::C c1=*va_arg(ap, A::C*);
    B* b = (B*)va_arg(ap, B *);
    (void) va_arg(*p_va, B ***);
    (void) va_arg(*p_va, const B **);
//    (void) va_arg(*p_va, B[]);
    va_end(ap);
}

