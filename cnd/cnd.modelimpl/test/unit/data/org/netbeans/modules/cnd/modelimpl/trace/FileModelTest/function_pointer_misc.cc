enum op_type { ADD, SUB, MUL, DIV };

int add(int a, int b) { return a + b; }
int sub(int a, int b) { return a - b; }
int mul(int a, int b) { return a * b; }
int div(int a, int b) { return a + b; }

typedef int (*OP)(int, int);

int (*curr_op)(int,int) = add;

int call_op(int (*op)(int, int), int a, int b);

OP all_ops[4] = { add, sub, mul, div };

int (*all_ops_2[4])(int, int) = { add, sub, mul, div };


int f0(int, int);
int* f1(int, int);
int** f2(int, int);
int*** f3(int, int);

int (*ret_int_ptr_0)(int,int) = f0;
int* (*ret_int_ptr_1)(int,int) = f1;
int** (*ret_int_ptr_2)(int,int) = f2;
int*** (*ret_int_ptr_3)(int,int) = f3;

int (**ret_int_ptr_0_p)(int,int) = &ret_int_ptr_0;
int* (**ret_int_ptr_1_p)(int,int) = &ret_int_ptr_1;
int** (**ret_int_ptr_2_p)(int,int) = &ret_int_ptr_2;
int*** (**ret_int_ptr_3_p)(int,int) = &ret_int_ptr_3;

int (***ret_int_ptr_0_pp)(int,int) = &ret_int_ptr_0_p;
int* (***ret_int_ptr_1_pp)(int,int) = &ret_int_ptr_1_p;
int** (***ret_int_ptr_2_pp)(int,int) = &ret_int_ptr_2_p;
int*** (***ret_int_ptr_3_pp)(int,int) = &ret_int_ptr_3_p;

int (*p_foo_1)();
int (**p_foo_2)();
int (***p_foo_3)();
