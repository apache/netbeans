namespace bug243513 {
    struct S_243513 {
      virtual ~S_243513();
      void g_243513() throw (auto(*)()->int);
    };
    
    void g_243513(auto (*f1_243513)() -> int) {
      try { }
      catch (auto (&f2_243513)() -> int) { }
      catch (auto (*const f3_243513[10])() -> int) { }
    } 
}