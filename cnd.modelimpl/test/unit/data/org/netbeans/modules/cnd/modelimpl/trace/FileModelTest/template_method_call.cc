class C {
public:
    template <int i> int foo(){
        return i;
    };
};

int main(){    
    C c;
    c.foo<1>();
    return 0;
}