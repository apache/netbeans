class A {
public:
    virtual void b() throw(int) = 0;
    virtual void c() = 0;
    virtual void d() throw(int);
};
