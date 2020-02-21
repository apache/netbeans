void fooo(char* msg) {}

namespace ns1 {
    using namespace n2;
    using ::foo;
    typedef char* string;
}

class Bbb : public Aaa {
public:
    Bbb();
    virtual ~Bbb();
    friend ns1::foo;
};

typedef Ccc *CccPtr;

int main(char argc, char* argv[]) {
    foo();
    fooo((ns1::string) msg);
    result = 1;
    return result;
}
