namespace ns216084 {
    class base {
    public:
        virtual ~base() { }
        virtual operator int() const = 0;
        virtual operator char() const = 0;
    };

    class AA {
    public:
        AA() {
            ;
        }
        virtual ~AA() throw() = 0;
    };
}
