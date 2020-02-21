template<class T>
struct C : public T {
};

template<
typename T
>
struct B {
    typedef typename C<T>::type type;
    type i;
};

struct A {
    typedef int type;
};

int main() {
    B<A> b;
    b.i++;

    return 0;
}