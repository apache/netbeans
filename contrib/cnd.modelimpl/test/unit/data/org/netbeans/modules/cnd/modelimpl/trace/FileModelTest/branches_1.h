#if !defined(_SIZE_T) || __cplusplus >= 199711L
#define _SIZE_T
#if defined(_LP64) || defined(_I32LPx)
typedef unsigned long   size_t;         /* size of something in bytes */
#else
typedef unsigned int    size_t;         /* (historical version) */
#endif
#endif  /* !_SIZE_T */
