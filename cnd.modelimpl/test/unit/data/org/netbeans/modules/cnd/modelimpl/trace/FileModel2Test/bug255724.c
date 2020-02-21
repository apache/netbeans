void test255724() {
    struct Outer255724 {
        int f1;
        union Inner255724 {
            int j1;
            char j2;
        } u;
    };
    
    struct Outer255724 str;
    str.u.j2 = 4;
    
    union Inner255724 un;
    un.j2 = 4;
}