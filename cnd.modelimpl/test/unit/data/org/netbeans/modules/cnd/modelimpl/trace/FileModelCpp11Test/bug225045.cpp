namespace bug225045 {
    struct Base225045 {
        virtual void final();
    }; 

    class ReproduceBug225045 : Base225045 {
    public:
        void final() final;
    };

    void ReproduceBug225045::final() {
    }   
}