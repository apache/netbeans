int bug218192_main() {
    void foo();
    class C1 {
        friend void foo();
    };
    return 0;
}
