class A {
};
#error "just a test"
class B {
    // the folding should survive #error directive
};
