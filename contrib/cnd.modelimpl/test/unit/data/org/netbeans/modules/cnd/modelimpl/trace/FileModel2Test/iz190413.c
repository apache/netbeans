#define HOST_CHARSET_UNKNOWN 0
#define HOST_CHARSET_ASCII   1
#define HOST_CHARSET_EBCDIC  2

#if  '\n' == 0x0A && ' ' == 0x20 && '0' == 0x30 \
   && 'A' == 0x41 && 'a' == 0x61 && '!' == 0x21
#  define HOST_CHARSET HOST_CHARSET_ASCII
#else
# if '\n' == 0x15 && ' ' == 0x40 && '0' == 0xF0 \
   && 'A' == 0xC1 && 'a' == 0x81 && '!' == 0x5A
#  define HOST_CHARSET HOST_CHARSET_EBCDIC
# else
#  define HOST_CHARSET HOST_CHARSET_UNKNOWN
# endif
#endif

#if HOST_CHARSET == HOST_CHARSET_ASCII
char* out190413 = "HOST_CHARSET_ASCII";
#else if HOST_CHARSET == HOST_CHARSET_EBCDIC
char* out190413 = "HOST_CHARSET_EBCDIC";
#else 
char* out190413 = "HOST_CHARSET_UNKNOWN";
#endif
