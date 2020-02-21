#ifdef A
    int a[8 / 4];
#endif
//typedef long int fm;
class fm {};
#define __NFDBITS       (8 * sizeof (fm))
#define __FD_SETSIZE 16
fm fb1[__FD_SETSIZE / __NFDBITS];

typedef struct
  {
#ifdef __USE_XOPEN
    fm fb[__FD_SETSIZE / __NFDBITS];
# define __FDS_BITS(set) ((set)->fb)
#else
    fm __fb[__FD_SETSIZE / __NFDBITS];
# define __FDS_BITS(set) ((set)->__fb)
#endif
  } fd_set;

