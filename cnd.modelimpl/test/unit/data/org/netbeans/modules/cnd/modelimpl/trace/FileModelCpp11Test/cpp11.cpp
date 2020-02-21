
//    Core language runtime performance enhancements
//        Rvalue references and move constructors

class A {
    A& operator=(A&& rhs) {
    }
};

//        Generalized constant expressions 

// GCC 4.6
constexpr int get_five() {return 5;}
int some_value[get_five() + 7];

constexpr double acceleration_due_to_gravity = 9.8;
constexpr double moon_gravity = acceleration_due_to_gravity / 6.0;

//        Modification to the definition of plain old data 


//    Core language build time performance enhancements
//        Extern template 

template<class T> class B {
};
extern template class B<int>;


//    Core language usability enhancements
//        Initializer lists

//#include <initializer_list>
//#include <vector>
//#include <string>
class SequenceClass {
public:
    SequenceClass(std::initializer_list<int> list) {
    }
};
SequenceClass some_var = {1, 4, 5, 6};
void function_name(std::initializer_list<float> list) {    
}
void foo() {
    function_name({1.0f, -3.45f, -0.4f});
    
    std::vector<std::string> v = { "xyzzy", "plugh", "abracadabra" };
    std::vector<std::string> v2{ "xyzzy", "plugh", "abracadabra" };
}

//        Uniform initialization

struct BasicStruct {
    int x;
    double y;
};
 
struct AltStruct {
    AltStruct(int x, double y) : x_{x}, y_{y} {}
 
private:
    int x_;
    double y_;
};
 
BasicStruct var1{5, 3.2};
AltStruct var2{2, 4.3};

//#include <string>

struct IdString {
    std::string name;
    int identifier;
};
 
IdString get_string()
{
    return {"SomeName", 4}; //Note the lack of explicit type.
}

//        Type inference

int foo2() {
    return 1;
}
auto some_strange_callable_type = foo2();
auto other_variable = 5;

int some_int;
decltype(some_int) other_integer_variable = 5;

//#include <vector>
void foo3() {
    std::vector<int> myvec(1);
    for (auto itr = myvec.cbegin(); itr != myvec.cend(); ++itr) {
    }
}

//        Range-based for-loop

// GCC 4.6
void foo4() {
    int my_array[5] = {1, 2, 3, 4, 5};
    for (int &x : my_array) {
        x *= 2;
    }
}

//        Lambda functions and expressions

//#include <algorithm>
void foo5() {
    char s[] = "Hello World!";
    int Uppercase = 0; //modified by the lambda

    std::for_each(s, s + sizeof (s), [&Uppercase] (char c) {
        if (isupper(c))
                Uppercase++;
        });
}

//        Alternative function syntax

//template<class Lhs, class Rhs>
//  auto adding_func(const Lhs &lhs, const Rhs &rhs) -> decltype(lhs+rhs) {return lhs + rhs;}
//
//struct SomeStruct  {
//    auto func_name(int x, int y) -> int;
//};
// 
//auto SomeStruct::func_name(int x, int y) -> int {
//    return x + y;
//}

//        Object construction improvement

// NO
class SomeType  {
    int number;
 
public:
    SomeType(int new_number) : number(new_number) {}
    SomeType() : SomeType(42) {}
};

//        Explicit overrides and final

// GCC 4.7
struct Base {
    virtual void some_func(float);
}; 
struct Derived : Base {
    virtual void some_func(float) override;
};

struct Base1 final { };
 
struct Derived1 : Base1 { }; // ill-formed because the class Base1 has been marked final
 
struct Base2 {
    virtual void f() final;
};
 
struct Derived2 : Base2 {
    void f(); // ill-formed because the virtual function Base2::f has been marked final
};

//        Null pointer constant

// GCC 4.6
int* x = nullptr;

//        Strongly typed enumerations

enum class Enumeration {
    Val1,
    Val2,
    Val3 = 100,
    Val4 /* = 101 */
};

enum class Enum2 : unsigned int {Val1, Val2};

enum Enum3 : unsigned int {Val1, Val2};

unsigned int k = Enum3::Val1;

//        Right angle bracket

//#include <vector>
template<bool Test> class SomeType {    
};
std::vector<SomeType<(1>2)>> x1;

//        Explicit conversion operators

//        Template aliases

// ?
template <typename First, typename Second, int third>
class SomeType; 
template <typename Second> using TypedefName = SomeType<OtherType, Second, 5>;
using OtherType = void (*)(double);           // New introduced syntax


//        Unrestricted unions

// GCC 4.6
//#include <new>
 
struct Point  {
    Point() {}
    Point(int x, int y): x_(x), y_(y) {}
    int x_, y_;
};
union U {
    int z;
    double w;
    Point p;  // Illegal in C++; point has a non-trivial constructor.  However, this is legal in C++11.
    U() { new( &p ) Point(); } // No nontrivial member functions are implicitly defined for a union;
                               // if required they are instead deleted to force a manual definition.
};

//        Identifiers with special meaning 

// The identifiers override and final have a special meaning when used in a certain context, but can otherwise be used as normal identifiers.

//    Core language functionality improvements
//        Variadic templates

template<typename... Values> class tuple;

template <typename... BaseClasses> class ClassName : public BaseClasses... {
public:
    ClassName (BaseClasses&&... base_classes) : BaseClasses(base_classes)... {}
};
template<typename ...Args> struct SomeStruct {
    static const int size = sizeof...(Args);
};

//        New string literals

//void foo6() {
//    u8"This is a Unicode Character: \u2018.";
//    u"This is a bigger Unicode Character: \u2018.";
//    U"This is a Unicode Character: \u2018.";
//    R"(The String Data \ Stuff " )";
//    R"delimiter(The String Data \ Stuff " )delimiter";
//    u8R"XXX(I'm a "raw UTF-8" string.)XXX";
//    uR"*(This is a "raw UTF-16" string.)*";
//    UR"(This is a "raw UTF-32" string.)";
//}

//        User-defined literals

// NO
//int operator "" _suffix(const char *literal_string) {
//    return 1;
//}
//int some_variable = 1234_suffix;

//        Multitasking memory model

//        Thread-local storage

// A new thread-local storage duration (in addition to the existing static, dynamic and automatic) has been proposed for the next standard. 

//        Explicitly defaulted and deleted special member functions

struct NonCopyable {
    NonCopyable & operator=(const NonCopyable&) = delete;
    NonCopyable(const NonCopyable&) = delete;
    NonCopyable() = default;
};

struct NoInt {
    void f(double i);
    void f(int) = delete;
};

struct OnlyDouble {
    void f(double d);
    template<class T> void f(T) = delete;
};

//        Type long long int

long long int i;

//        Static assertions

#define GREEKPI 3.1415
static_assert ((GREEKPI > 3.14) && (GREEKPI < 3.15), "GREEKPI is inaccurate!");

template<class T>
struct Check  {
    static_assert (sizeof(int) <= sizeof(T), "T is not big enough!");
};

//        Allow sizeof to work on members of classes without an explicit object

struct SomeType { int member; };
int j = sizeof(SomeType::member);
