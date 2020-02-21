#include <stdio.h>
#include "change_case.h"
   
int main() {
    char *text = "Hi, I'm a simple program\n"; 
    printf(text);
    char buf1[256];
    char buf2[256];
    to_upper(text, buf1, sizeof buf1);
    printf(buf1);
    to_lower(text, buf2, sizeof buf2);
    printf(buf2);
    return 0;
}
