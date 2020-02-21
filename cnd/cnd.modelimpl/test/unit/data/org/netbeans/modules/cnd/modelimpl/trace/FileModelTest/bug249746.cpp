namespace bug249746 {
    struct AAA249746 {
        AAA249746(int, int);
    };

    int foo249746();

    int boo249746() {
        AAA249746 a((foo249746()), foo249746());
    }
}