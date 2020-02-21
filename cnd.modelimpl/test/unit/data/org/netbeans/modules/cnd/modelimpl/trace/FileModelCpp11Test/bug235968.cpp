namespace bug235968 {
    class AAA_235968 {
    public:
        AAA_235968() noexcept;
        virtual ~AAA_235968() noexcept = default;
    };

    class BBB_235968 : public AAA_235968 {
    public:
        virtual ~BBB_235968() noexcept override = default;
    };
}