// NB! This code is not compilable!
namespace bug250324 {
    template <typename A>
    struct AAA250324 {
        template <typename B>
        void f250324();
    };

    // This specialization is erroneous - template-id do not match the declaration
    template <typename B> 
    void AAA250324<short>::f250324<B>() {
        return;
    } 
}