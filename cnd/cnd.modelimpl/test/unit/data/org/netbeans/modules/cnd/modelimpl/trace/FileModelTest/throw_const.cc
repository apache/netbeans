class Exception {};

namespace ns {
    class Throwable {};
}

void foo_1() throw (const char) {
}

void foo_2() throw (const char&) {
}

void foo_3() throw (Exception) {
}

void foo_4() throw (const Exception&) {
}

void foo_5() throw (const ns::Throwable&) {
}


void foo_6() throw () {
}

int fun1(int, int);
int (*fp_fun1)(int, int) = fun1;

// An extremely perverted one
void foo_7() throw (int (*)(int, int)) {
 	throw fp_fun1;
}
