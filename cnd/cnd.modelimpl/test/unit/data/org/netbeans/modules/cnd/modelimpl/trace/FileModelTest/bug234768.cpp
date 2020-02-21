namespace {
  using A = struct AA {
      int x;
  };

  using B = enum BB {
      Y
  };

  using C = struct CC {
      int foo() {
          return 0;
      }
  };
  
  struct AAA {    
    using AAA_B = struct BBB {
      int foo();
    };
    
    int boo() {
      return AAA_B().foo() + BBB().foo();
    }
  };


  int foo() {
      A a;    
      a.x = Y;

      B b;
      b = Y;

      return C().foo() + CC().foo();
  }  
}