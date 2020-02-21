class Fumble; // forward declaration
class Foo {
    private: Fumble* fumble; // no errors in this line
    private: class FooImpl* mImpl; // FooImpl is not underlined as error
    public:  void doBad() { fumble->doWork(); } // doWork() is marked as undefined
    public:  inline void doGood();
};
