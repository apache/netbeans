template<class T> class Cls {
};

template class Cls<int>;

template<class T> T min(T x, T y) {
return x < y ? x : y;
}

template int min(int,int);
