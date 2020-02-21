namespace bug243515 {
    struct X_243515 {
      void f0_243515() &;
      void f1_243515() &&;
    };

    typedef void func_type_lvalue_243515() &;
    typedef void func_type_rvalue_243515() &&;

    void (X_243515::*mpf1)() & = &X_243515::f0_243515;
    void (X_243515::*mpf2)() && = &X_243515::f1_243515;

    class Y_243515 { 
      void h_243515() &; 
      void h_243515() const &; 
      void h_243515() &&; 
      void i_243515() &; // expected-note{{previous declaration}}
      void i_243515() const; // expected-error{{cannot overload a member function without a ref-qualifier with a member function with ref-qualifier '&'}}

      template<typename T> void f_243515(T*) &;
      template<typename T> void f_243515(T*) &&;

      template<typename T> void g_243515(T*) &; // expected-note{{previous declaration}}
      template<typename T> void g_243515(T*); // expected-error{{cannot overload a member function without a ref-qualifier with a member function with ref-qualifier '&'}}

      void k_243515(); // expected-note{{previous declaration}}
      void k_243515() &&; // expected-error{{cannot overload a member function with ref-qualifier '&&' with a member function without a ref-qualifier}}
    };
}