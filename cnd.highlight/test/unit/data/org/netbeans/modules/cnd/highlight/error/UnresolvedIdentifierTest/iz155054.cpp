template<class T> struct B {
};
template<> struct B<int> {
    typedef int type;
};
template<class T> struct A {
    typename B<T>::type t; // type is highlighted as error
};
int main() {
    A<int> a;
    a.t++;
    return 0;
}