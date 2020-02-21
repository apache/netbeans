#ifdef FOO
void class_body_included_foo();
#elif defined(BOO)
void class_body_included_boo();
#else
void class_body_included_no_name_fun();
#endif
