#define __glibcxx_function_requires1(...)

#define __glibcxx_function_requires2(...)                                 \
            __gnu_cxx::__function_requires< __gnu_cxx::__VA_ARGS__ >();

int i  = __glibcxx_function_requires2(A::B::C)

__glibcxx_function_requires1(1,3,2,11)
 
// Tests empty __VA_ARGS__ expansion

//O --variadics

#define MACRO1(x, ...)  x -> __VA_ARGS__
#define MACRO2(...)     __VA_ARGS__
#define STR(...)        #__VA_ARGS__

//R #line 19 "t_1_034.cpp"
MACRO1(1,)    //R 1 -> 
MACRO2(1, 2)  //R 1,2 
STR()         //R "" 

// Tests the stringize operator in conjunction with varidic macros

//O --variadics

#define STR(...) #__VA_ARGS__

//R #line 17 "t_1_033.cpp"
STR(1, 2, 3)            //R "1, 2, 3" 
STR(1,2,3)              //R "1,2,3" 
STR(1 , 2 , 3)          //R "1 , 2 , 3" 
STR( 1  ,   2  ,   3 )  //R "1 , 2 , 3" 

