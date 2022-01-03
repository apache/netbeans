#ifndef _second_hello1_H
#define	_second_hello1_H

#if defined(SECOND_EMPTY_MACRO_1) && defined(SECOND_EMPTY_MACRO_2) && defined(SECOND_EMPTY_MACRO)
#define EMPTY_1
#endif

#if defined(SECOND_EMPTY_MACRO_1)
#define EMPTY_1
#define EMPTY_2
#endif


#if defined(SECOND_EMPTY_MACRO_2)
#define EMPTY_2
#define EMPTY_3
#endif

extern EMPTY_1 char *secondHello1();
extern EMPTY_2 char *secondHello2();
extern EMPTY_3 char *secondHello3();

#endif	/* _second_hello1_H */
