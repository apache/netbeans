class iz151054_A {
public:
    int a;
};

class iz151054_C {
    iz151054_A r;
public:
    int c;
    iz151054_A* operator->() {
        return &r;
    }
};

int iz151054_main() {
    iz151054_C *pc;
    pc->c; // unresolved
    pc->a; // unmarked error
    return 0;
}
