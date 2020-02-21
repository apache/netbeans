namespace bug248749 {
    template <typename T>
    void foo248749(T param) {
        T::iterator iter;
        (*iter)->boo(); // boo should be warning
    }  
}