namespace bug270231 {
    struct AAA270231 {
        AAA270231();
        AAA270231(int var);
        AAA270231 operator+(const AAA270231 &other);
        void foo();
    };

    void check___typeof__() { 
        AAA270231 a;
        __typeof__(a) c; 
        __typeof__(AAA270231) d;
        __typeof__(AAA270231{}) e; 
        __typeof__(d + e) g;  
        c.foo();
        d.foo();  
        e.foo();  
        g.foo();
    }  

    void check___typeof() { 
        AAA270231 a;
        __typeof(a) c; 
        __typeof(AAA270231) d;
        __typeof(AAA270231{}) e; 
        __typeof(d + e) g;  
        c.foo();
        d.foo();  
        e.foo();  
        g.foo();
    }

    void check_typeof() { 
        AAA270231 a;
        typeof(a) c; 
        typeof(AAA270231) d;
        typeof(AAA270231{}) e; 
        typeof(d + e) g;  
        c.foo();
        d.foo();  
        e.foo();  
        g.foo();
    }
}