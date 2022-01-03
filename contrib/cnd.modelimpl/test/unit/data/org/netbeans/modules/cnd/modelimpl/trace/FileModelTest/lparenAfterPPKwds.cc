#if(defined(A))
int a;
#elseif(defined(B))
int b;
#elif(defined(C))
int c;
#else\
/*else*/
int d;
#endif/*end*/

#if+1>0
int i1;
#endif

#if-1<0
int iM1;
#endif

#if!1==0
int i0;
#endif
