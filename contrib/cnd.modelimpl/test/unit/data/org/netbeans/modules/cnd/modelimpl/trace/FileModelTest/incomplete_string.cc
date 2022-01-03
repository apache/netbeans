#ifdef DEBUG
char* v = "S  ;
#endif

int foo_incomplete_string();


#if 0
 it's a dead block with incomplete char literal

#endif

int foo_incomplete_char() {
 char c = 'asdfasdf
 c++;
}

int foo_incomplete_char_ok() {

}

#if 0
this is usage of ` GRAVE_ACCENT
#endif
