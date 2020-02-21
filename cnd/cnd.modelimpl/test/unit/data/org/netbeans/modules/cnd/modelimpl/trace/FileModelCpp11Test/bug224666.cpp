namespace {
    
    struct A
    {
        virtual void foo();
    };

    struct B : A
    {
        virtual void foo() final override; //Error: "unexpected token override"
    };
    
}