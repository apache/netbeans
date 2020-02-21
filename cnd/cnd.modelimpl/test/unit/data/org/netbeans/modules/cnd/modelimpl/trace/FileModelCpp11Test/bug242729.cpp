namespace bug242729 {
    
    template <typename T1, typename T2>
    void func242729(T1 t1, T2 t2) noexcept;
    
    namespace std242729 {
        template <typename T>
        T declval242729() noexcept {};
    }

    template<typename _Tp>
    struct array242729 // array is not resolved
    {
      void swap242729(array242729& __other)
      noexcept(noexcept(func242729(std242729::declval242729<_Tp&>(), std242729::declval242729<_Tp&>())))
      {}

      // Iterators.
      int
      begin242729() noexcept
      { return 0; }
    };
}