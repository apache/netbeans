
template <class T>
class A {
public:
    template <class TT> operator TT();
    operator int();
};

template <class T>
template <class TT>
inline A<T>::operator TT() {
    return 0;
}

template <class T>
inline A<T>::operator int() {
    return 0;
}

class B {
public:
    template <class TT> operator TT();
    operator int();
};

template <class TT>
inline B::operator TT() {
    return 0;
}

inline B::operator int() {
    return 0;
}
