namespace bug243508 {
  namespace AN_243508 {
    class a243508 {};
  }

  namespace BN_243508 {
    typedef class {} b243508;
  }

  namespace DN_243508 {
    using typename AN_243508::a243508;
    using typename BN_243508::b243508;
  }
  
  template <class T> struct A243508 {
    typedef int type243508; // expected-note {{target of using declaration}}
    union { double union_member243508; }; // expected-note {{target of using declaration}}
    enum tagname243508 { enumerator243508 }; // expected-note 2 {{target of using declaration}}
  };

  template <class T> struct C243508 : A243508<T> {
    using typename A243508<T>::type243508;
    using typename A243508<T>::union_member243508; // expected-error {{'typename' keyword used on a non-type}}
    using typename A243508<T>::enumerator243508; // expected-error {{'typename' keyword used on a non-type}}
  };  
}