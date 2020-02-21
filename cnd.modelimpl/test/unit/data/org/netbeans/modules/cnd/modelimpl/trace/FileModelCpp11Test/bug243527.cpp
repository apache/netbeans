namespace bug243527 {
    struct X0_243527 {
      typedef int &(*func_int_ref243527)();
      typedef float &(*func_float_ref243527)();    

      operator func_int_ref243527() &;
      operator func_float_ref243527() &&;

      int &operator+(const X0_243527&) &;
      float &operator+(const X0_243527&) &&;

      template<typename T> int &operator+(const T&) &;
      template<typename T> float &operator+(const T&) &&;  

      template<typename T> int &ft243527(T) &;
      template<typename T> float &ft243527(T) &&;  
    };
}