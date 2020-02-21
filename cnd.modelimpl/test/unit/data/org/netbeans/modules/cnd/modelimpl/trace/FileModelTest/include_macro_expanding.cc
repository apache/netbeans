#ifdef  linux
#undef  linux
#endif
#ifdef  i386
#undef  i386
#endif
#define _MULTIARCH_HEADER wx-2.8/wx/defs.h
#define _MULTIARCH_OS linux
#define _MULTIARCH_MAKE_HEADER(arch,header) <multiarch-arch-_MULTIARCH_OS/header>

#include _MULTIARCH_MAKE_HEADER(i386,_MULTIARCH_HEADER)

