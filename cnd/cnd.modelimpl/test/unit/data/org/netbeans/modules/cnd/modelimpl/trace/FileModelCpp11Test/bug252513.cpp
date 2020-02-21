namespace bug252513 {
  namespace std252513 {
    typedef int string252513;

    int to_string252513(int var) {
      return var;
    }
  }


  int foo252513() {
    int x, y, z;

    auto lambda0 = [=](int a, int b)->std252513::string252513 {
      return std252513::to_string252513(a) + std252513::to_string252513(b) + std252513::to_string252513(x);
    };

    // this works fine
    auto lambda1 = [](int a, int b)->std252513::string252513 {
      return std252513::to_string252513(a) + std252513::to_string252513(b);
    };

    // also works
    auto lambda2 = [&](int a, int b)->std252513::string252513 {
      return std252513::to_string252513(a) + std252513::to_string252513(b);
    };

    // also works
    auto lambda3 = [x](int a, int b)->std252513::string252513 {
      int c = a + x;
      return std252513::to_string252513(a) + std252513::to_string252513(b);
    };

    // does not work
    // unable to resolve identifier a
    // unable to resolve identifier b
    // unable to resolve identifier std
    auto lambda4 = [x, y, z](int a, int b)->std252513::string252513 {
    // unable to resolve identifier c
      int c = a + x + y + z;
      return std252513::to_string252513(a) + std252513::to_string252513(b);
    };
    
    return 0;
  }
}