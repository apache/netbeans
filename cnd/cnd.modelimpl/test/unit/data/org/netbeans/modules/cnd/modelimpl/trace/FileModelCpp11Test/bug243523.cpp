namespace bug243523 {
    struct X_243523 { };
    void test_243523(X_243523 *xp, int (X_243523::*pmf)(int), int (X_243523::*l_pmf)(int) &,  int (X_243523::*r_pmf)(int) &&);
}