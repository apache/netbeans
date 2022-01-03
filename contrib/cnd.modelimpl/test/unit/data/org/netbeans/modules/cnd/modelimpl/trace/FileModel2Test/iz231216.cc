class A {
public:
    int i;
    A(int a) {i = a;}
    int operator~ () {return 1 + i;}
    int operator! () {return 2 + i;}
};
