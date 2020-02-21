int fun1(int, int);
int (*fp_fun1)(int, int) = fun1;
int (**fpp_fun1)(int, int) = &fp_fun1;
int (***fppp_fun1)(int, int) = &fpp_fun1;

int* fun2(int, int);
int* (*fp_fun2)(int, int) = fun2;
int* (**fpp_fun2)(int, int) = &fp_fun2;
int* (***fppp_fun2)(int, int) = &fpp_fun2;
