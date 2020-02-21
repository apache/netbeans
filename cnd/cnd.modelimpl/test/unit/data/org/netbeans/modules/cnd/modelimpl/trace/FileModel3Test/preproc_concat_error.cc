// 24.6: In function-like macro (right).
#define FUNC(a)  # b
//E t_6_048.cpp(23): error: ill formed preprocessing operator: stringize ('#')
int FUNC(1);
