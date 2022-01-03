class D
{
public:
    typedef enum
    {
        MyEnum_ValOne, // <--- Unable to resolve identifier
        MyEnum_ValTwo  // <--- Unable to resolve identifier
    } MyEnum;

    typedef enum _MyEnum12 //<--- unable to resolve identifier
    {
        MyEnum_ValOne12, //<--- Unable to resolve identifier
        MyEnum_ValTwo12  //<--- Unable to resolve identifier
    } MyEnum12;
};

typedef enum _MyEnum22 //<--- unable to resolve identifier
{
    MyEnum_ValOne22, //<--- Unable to resolve identifier
    MyEnum_ValTwo22 //<--- Unable to resolve identifier
} MyEnum22;

int main() {
    D::MyEnum k = D::MyEnum_ValOne;
    return 0;
}
