template<typename K, typename V = K> class pair {
    K first;
    V second;
};

// main template declaration
template<class T> const char* func(T p);

// partial specialization declaration #1
template<class T> const char* func(T* p);

// partial specialization declaration #2
template<class T> const char* func(pair<T,T> p);

// full specialization declaration #1
template<> const char* func(char p);

// full specialization declaration #2
template<> const char* func(pair<char, char>);

int main(int argc, char** argv) {
    printf("%s\n", func(argc)); // prints "base"
    printf("%s\n", func(argv)); // prints "pointer"
    printf("%s\n", func('c'));  // prints "char"
    pair<char, char> pc;
    printf("%s\n", func(pc));   // prints "pair<char, char>"
    pair<int, int> pi;
    printf("%s\n", func(pi));   // prints "pair"
    return 0;
}

// main template definition
template<class T> const char* func(T p) {
    return "base";
};

// partial specialization definition #1
template<class T> const char* func(T* p) {
    return "pointer";
};

// partial specialization definition #2
template<class T> const char* func(pair<T,T> p) {
    return "pair";
};

// full specialization definition #1
template<> const char* func(char p) {
    return "char";
};
 
// full specialization definition #3
template<> const char* func(pair<char,char> p) {
    return "pair<char, char>";
};
