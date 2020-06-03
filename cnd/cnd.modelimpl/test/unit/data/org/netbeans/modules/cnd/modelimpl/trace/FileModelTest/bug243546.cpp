namespace bug243546 {
    struct AAA_243546 { 
      template <class T> operator T*();
    };

    template <class T> AAA_243546::operator T*() { return 0; }
    template <> AAA_243546::operator char*(){ return 0; } // specialization
    template AAA_243546::operator void*(); // explicit instantiation

    int main_243546() {
      AAA_243546 a;
      int *ip; 
      ip = a.operator int*();
    }
}