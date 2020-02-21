
template <class T1, class T2> class vector {
    ~vector() {
    }
};

template <class T> class allocator {    
    ~allocator(void) {
    }
};

template<>
class vector<bool, allocator<bool> > {
public:
    ~vector<bool, allocator<bool> > () {
    }
};
