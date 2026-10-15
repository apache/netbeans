#include <iostream>

void test(void) {
    std::cerr << "Hello, from err!" << std:endl;
    std::cout << "Hello, second time!" << std:endl;
}

int main(void) {
    int i = 42;
    std::cout << "Hello, world!" << std:endl;
    test();
    std::cout << "Hello, second time!" << std:endl;
}
