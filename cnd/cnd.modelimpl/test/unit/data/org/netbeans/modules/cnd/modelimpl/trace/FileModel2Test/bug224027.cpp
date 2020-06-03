namespace {
    
  struct A {};

  struct B {
      friend A::A();
      friend A::~A();
      friend A &A::operator=(const A&);
  };
    
}
