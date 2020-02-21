void bug223298_foo() {
    
}
struct bug223298_A {
    int b;
    bug223298_A c;
    bug223298_A bar() {}
};
int bug223298_A::bar(void) {    
    int i;
    int j;
    bug223298_A a;
    bug223298_A z[1];
    if(a.b < i)
        a.b = i;
    if (bug223298_foo(i) || z[i].b < z[i].c/z[i].b)
        j = i;
    if (bug223298_foo(i)) {
        if (bug223298_foo(j) == 0.0)
            bug223298_foo("", bug223298_foo(i));
        else
        if (bug223298_foo(j) > -1. && bug223298_foo(i) < 1.)
            bug223298_foo("", bug223298_foo(i), bug223298_foo(j));
        else {
        }
    }
    if (z[i - 1].b > 0
        && z[j - 1].c < 32)
        z[i - 1].b = 32;    
    if (a.b < 0)
        z->b = 0;
    if (a.b < bug223298_foo (i))
        a.b = bug223298_foo (i);    
    if (z->c.b < sizeof (i))
        i = i & ((1LL << z->c.b * 8) - 1);
    if (i == j && a.b < j)
        a.b = j;
    if (z[i].c < i) i = j + 1;
    if (z->bar().c < this->b)
        this->b = z->bar().b;
    if (bug223298_foo(i) > 0.0 && bug223298_foo(i) < j)
        j = bug223298_foo(i);
    
}

