namespace bug243514 {
    template<typename... Args>
    auto ff() -> int(&)[sizeof...(Args)] {}
    
    auto f1() -> auto (*)() -> void;
    
    int f2(auto (*)(int a) -> int);
    
    auto f3(int z) -> int(&)[];
    
    auto f4(int z1) -> auto (*)(int) -> int;
    
    auto f5(int z2) -> auto (*)(int(*)(int)) -> int;
}