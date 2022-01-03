namespace bug243528 {
    template<typename T>
    struct X1_243528 {

      X1_243528<T>();
      X1_243528<T>(int);
      (X1_243528)(double);
      ((X1_243528)(char*));

      template<typename U> X1_243528(U);
    };

    template<typename T> X1_243528<T>::X1_243528() { }
    template<typename T> X1_243528<T>::X1_243528(int) { }
    template<typename T> (X1_243528<T>::X1_243528)(double) { }
    template<typename T> ((X1_243528<T>::X1_243528)(char*)) { }
}