namespace entity_resolver_test {
  template <typename A>
  struct AAA {
    int foo() {
      return 0;
    }
    
    A var_1;
  };

  template <>
  struct AAA<int> {
    int foo() {
      return 1;
    }
    
    int var_2;
  };

  int x;

  namespace yyy {
    int y;
  }

  int foo(int param);
  int foo(int *param);
  int foo(int param, double x);

  namespace test {
    int foo(int param);
  }

  struct BBB {
    int foo(int param);
    
    template <class T>
    struct CCC {
      T var_3;
    };
  };

  template <typename T>
  T boo(T param);


  template <>
  int boo<int>(int param);
}