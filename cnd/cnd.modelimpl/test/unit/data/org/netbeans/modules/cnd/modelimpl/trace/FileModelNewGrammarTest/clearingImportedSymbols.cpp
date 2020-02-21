struct ctype {
    int type;
};

class AAA {
public:    
    class _Impl {
        
    };
    
    static const int ctype = 1;
};

class BBB {
public:
    friend class AAA::_Impl;
      
    const ctype variable;
};  
