int moo2(int);
int moo(int){return 0;}
struct S2 { 
    int soo();
    int soo2(){return 0;} };
class A2{
    int foo(); };
int A2::foo(){ return 0; }
class B{
    typedef int xxx;
    friend class A2;
    int boo();
    friend int moo2(int) { return 0; };
    friend int moo(int);
    friend int S2::soo(){ return 0; }
    friend int S2::soo2();};
