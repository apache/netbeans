
template <typename T> class C {
public:
    typedef typename T::iterator* mytype;
    typename T::iterator *iter;
    mytype iter2;
    T *i;
};

template <typename T>
void func ()
{
    typename T::iterator t;
}

class A 
{
public:
    class iterator {
        int i;
    };
};