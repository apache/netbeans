namespace bug243518 {
  // Function with spec returning function with spec and taking pointer to function with spec.
  // The actual function throws int, the return type double, the argument float.
  void (*i1_243518() throw(int))(void (*)() throw(float)) throw(double);
  void (*i2_243518() noexcept(false))(void (*)() noexcept(true)) noexcept(false);
  void (*i3_243518(int xx) noexcept((false)))(int yy) const && throw(double);  
}