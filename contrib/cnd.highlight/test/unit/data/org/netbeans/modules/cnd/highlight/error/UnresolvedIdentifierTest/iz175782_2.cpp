struct S
{
    friend void r(int&);
};
void r(int&);
struct B {
    friend void c(int&);
};
void c(int&) {
}

struct string {};
struct subString {};
struct A {
    friend inline void cat(const string&, const char*, string&);
};
inline void cat(const string&, const char*, string&) {
}