namespace bug252425 {
  typedef int type252425;

  template <typename T>
  using alias252425 = T;

  struct AAA252425 {
    typedef char inner_type252425;

    enum class : alias252425<int> {
      Val1
    };

    enum struct : type252425 {
      Val2
    };

    enum class : AAA252425::inner_type252425 {
      Val3
    };

    enum struct : volatile typename ::bug252425::AAA252425::inner_type252425 const {
      Val4
    };

    enum struct NamedEnum252425 : volatile typename ::bug252425::AAA252425::inner_type252425 const {
      Val5
    };
  };
}