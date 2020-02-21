namespace bug243262 {    
    template <typename T>
    struct NodeOption243262 {
        int parse243262(int val);
        int value243262;
    };
    
    template <>
    int NodeOption243262<int>::parse243262(int val) {
        return value243262;
    }   
} 