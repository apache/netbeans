struct A1 : public A1 {
};

template <class T, int t>
struct A2 : public A2<T, t+1> {
};

template <int t>
struct A3 : public A3<t+1> {
};


int main() {
    A1 a1;
    a1.a;
    A2<int, 1> a2;
    a2.a;
    A3<1> a3;
    a3.a;
    return 0;
}