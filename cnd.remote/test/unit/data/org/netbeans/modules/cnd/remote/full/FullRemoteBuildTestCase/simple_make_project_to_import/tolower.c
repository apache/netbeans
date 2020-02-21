#include <stdio.h>

void to_lower(const char* src, char* dst, int len) {
    char c;
    while (len > 0) {
        *dst = tolower(*src);
        src++;
        dst++;
        len--;
    }
}
