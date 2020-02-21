__extension__ typedef struct
{
long long int quot;		/* Quotient.  */
long long int rem;		/* Remainder.  */
} lldiv_t;

#define __USER_LABEL_PREFIX__
#define __ASMNAME(cname)  __ASMNAME2 (__USER_LABEL_PREFIX__, cname)
#define __ASMNAME2(prefix, cname) __STRING (prefix) cname
#define __REDIRECT(name, proto, alias) name proto __asm__ (__ASMNAME (#alias))
#define __nonnull(params) __attribute__ ((__nonnull__ params))
#define __wur /* Ignore */
extern lldiv_t __REDIRECT (mkstemp, (char *__template), mkstemp64) __nonnull ((1)) __wur;
