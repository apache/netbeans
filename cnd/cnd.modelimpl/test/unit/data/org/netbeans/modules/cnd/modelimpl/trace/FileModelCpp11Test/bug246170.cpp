namespace bug246170 {
    int foo246170(int ((*array)[2])[2] = new int[2][2][2] {{{0, 1}, {0, 1}},{{0, 1}, {0, 1}},{{0, 1}, {0, 1}}});

    void boo246170() {
        auto lambda = []{ int res = 1; return res; };
        foo246170(new int[2][2][2] {{{2, 3}, {2, 3}},{{2, 3}, {2, 3}},{{2, 3}, {2, 3}}}); // Unexpected token here
    }     
}