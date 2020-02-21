namespace bug243524 {
    void test_exception_spec_243524() {
      auto tl1 = []() throw(int) {};
      auto tl2 = []() {};
      auto ntl1 = []() throw() {};
      auto ntl2 = []() noexcept(true) {};
      auto ntl3 = []() noexcept {};
    }
}