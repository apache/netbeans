class bug204497_Expr1 {
public:
    bug204497_Expr1() {
    }
    void bug204497_foo() {
    }
};

class bug204497_ExprIterator {    
public:
    friend bug204497_Expr1::bug204497_Expr1();
    friend void bug204497_Expr1::bug204497_foo();
};
