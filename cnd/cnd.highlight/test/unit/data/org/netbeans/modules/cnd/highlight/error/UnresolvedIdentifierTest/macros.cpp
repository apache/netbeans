#define MMM(x) x
#define CONCAT(a, b, c) a##b##c
namespace std {}

int main() {
    MMM(std)::cout << "Hello";
    CONCAT(s, t, d)::cout << endl;

#define DEBUG(x)
    DEBUG({
        int i;
        string msg = "hello world";
        debug_func_call(&i, msg);
    })
}

int func MMM((int arg)) {}
