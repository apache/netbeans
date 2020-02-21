namespace N {
    struct A {
        A() {
        }
        A(int i) {
        }
    };
}

struct String {};

void foo () {
    int i;
    N::A a1(i), a2;
    int i1(i), i2;
    int i3(1), i4;

    /* The names of functions that actually do the manipulation. */
    static int com_nopager(String *str, char*), com_pager(String *str, char*),
               com_edit(String *str,char*), com_shell(String *str, char *);
}