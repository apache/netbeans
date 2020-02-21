namespace bug251329 {
    struct AAA251329 {
        typedef int final;
        virtual auto foo() -> final;
    };   

    struct BBB251329 : AAA251329 {
        auto foo() -> final override;
    };

    struct CCC251329 {
        typedef int override;
        virtual auto foo() -> override;
    };   

    struct DDD251329 : CCC251329 {
        auto foo() -> override final;
    };

    struct EEE251329 {
        virtual auto boo() -> int final;
        virtual auto coo() -> AAA251329::final final;
    }; 
}