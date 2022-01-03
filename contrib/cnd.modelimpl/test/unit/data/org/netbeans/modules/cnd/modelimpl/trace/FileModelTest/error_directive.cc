int a;
#ifndef A
int b;
#	ifndef B
int c;
#		ifndef C
int d;
#			error "qwe"	
int e;
#		else
int f;
#		endif
int g;
#	else
int h;
#	endif
int i;
#else
int j;
#endif
