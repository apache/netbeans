#include <stdio.h> 

void to_upper(const char* src, char* dst, int len) { 
    char c;
    while (len > 0) {
	    *dst = toupper(*src);
	    src++;
	    dst++;
	    len--;
    }
}

