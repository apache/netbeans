namespace bug225102
{
    struct cls225102
    {
        virtual operator double() const override {
       
        }

        virtual operator int() const override;

        virtual operator float() const final {
        }
    };
}