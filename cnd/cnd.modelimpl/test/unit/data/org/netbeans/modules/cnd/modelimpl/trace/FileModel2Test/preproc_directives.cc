#define A
# define B
# /*xx*/ define C 8

%:define D
%: define E
%: /*xx*/ define F 7

%:/*aa*/if C == 8
    int a = 5;
#/*s*/endif

#/*aa*/if F < 7
    int b = 5;
#/*s*/endif
