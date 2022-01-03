namespace tpl_sr_test {
  template <typename A>
  struct AAA_sr_test {
      A x;

      static A T_var_1;

      template <typename B>
      struct BBB__sr_test {
          B y;
          static B T_var_2;
      };
  };

  template <typename T>
  extern T foo(T param) {
      return 0;
  }
  template int foo<int>(int param);
  template double foo<double>(double param);

  AAA_sr_test<int> boo(AAA_sr_test<int> param) {
    return AAA_sr_test<int>();
  }  

  AAA_sr_test<int> boo(const AAA_sr_test<int> &param) {
      return AAA_sr_test<int>();
  }  

  struct ZZZ_sr_test {
      static AAA_sr_test<int> var1;
      static AAA_sr_test<int>::BBB__sr_test<float> var2;

      int roo(AAA_sr_test<int>::BBB__sr_test<int> var);

      template <typename T>
      int zoo(AAA_sr_test<int>::BBB__sr_test<T> var);
  };

  int ZZZ_sr_test::roo(AAA_sr_test<int>::BBB__sr_test<int> var) {
      return 0;
  }

  template <typename T>
  int ZZZ_sr_test::zoo(AAA_sr_test<int>::BBB__sr_test<T> var) {
      return 0;
  }

  AAA_sr_test<int> ZZZ_sr_test::var1 = AAA_sr_test<int>();
  AAA_sr_test<int>::BBB__sr_test<float> ZZZ_sr_test::var2 = AAA_sr_test<int>::BBB__sr_test<float>();

  template <>
  int AAA_sr_test<int>::T_var_1 = 0;

  template int ZZZ_sr_test::zoo<int>(AAA_sr_test<int>::BBB__sr_test<int> var);
}