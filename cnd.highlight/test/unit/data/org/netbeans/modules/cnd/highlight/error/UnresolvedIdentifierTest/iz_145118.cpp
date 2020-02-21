namespace iz145118 {

    template<typename _Tp, typename _Alloc> class vector {
public:
    typedef _Tp value_type;
    //void push_back(const value_type& __x);
    value_type get(int);
};

template<class _T1, class _T2> struct pair {
    typedef _T1 first_type;
    typedef _T2 second_type;

    _T1 first;
    _T2 second;
};

int main(int argc, char**argv) {
    vector<pair<int, int> > v;
    v.push_back(pair<int, int>(1, 2));// warning, but may be error?
    return 0;
}

template<typename T> void foo() {
    vector<T> v;
    v.get(0).should_be_warning();
}

}
