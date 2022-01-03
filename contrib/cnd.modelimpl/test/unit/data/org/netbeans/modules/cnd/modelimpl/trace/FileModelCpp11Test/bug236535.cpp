namespace bug236535 {
  
  struct AAA_236535 {
    operator bool () = delete;
    operator int () const = delete;    
  };
  
}