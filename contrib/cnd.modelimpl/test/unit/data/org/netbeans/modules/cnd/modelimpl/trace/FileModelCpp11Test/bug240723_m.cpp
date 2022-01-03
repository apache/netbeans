namespace bug240723 {
    class MyClass240723_1 {
        
        MyClass240723_1() {}
        
        MyClass240723_1(int x) {}
        
        
        template<typename K> 
        auto myFunc_1() const noexcept -> decltype(K()) { 
                return 42;
        }                   

        int boo() {
            myFunc_1<MyClass240723_1>().boo();
        }
    };   
    
    class MyClass240723_2 {  
        
        template<typename K> 
        auto myFunc_2() const noexcept -> decltype(K()) { 
                return K();
        }                   

        int boo() {
            myFunc_2<MyClass240723_2>().boo();
        }
    };       
}
