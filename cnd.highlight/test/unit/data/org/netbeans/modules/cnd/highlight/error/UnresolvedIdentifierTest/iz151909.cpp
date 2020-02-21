template <class T> class iz151909_B {
};

class iz151909_A {
  template <class T> friend class iz151909_B;
  friend class iz151909_B;
  template <class T> class iz151909_D;
  template <class T> friend int iz151909_foo();
};