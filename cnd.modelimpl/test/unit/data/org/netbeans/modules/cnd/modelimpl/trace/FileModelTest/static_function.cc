// just a static definition
static void _foo1() {}

// declaration + definition, both static
static void _foo2();
static void _foo2() {}

// compile error: can't declare as non-static
// void _foo3();
// static void _foo3() {}


// static declaration + definition
static void _foo4();
void _foo4() {} // such function is also static, since it's

// non-statics
void foo5();
void foo5() {}
void foo6() {}

namespace NS {
	// just a static definition
	static void _foo1() {}
	
	// declaration + definition, both static
	static void _foo2();
	static void _foo2() {}
	
        // compile error: can't declare as non-static
        // void _foo3();
        // static void _foo3() {}
	
	// static declaration + definition
	static void _foo4();
	void _foo4() {} // such function is also static, since it's
	
	// non-statics
	void foo5();
	void foo5() {}
	void foo6() {}
}

static inline char *func_iz149125() {
    return 0;
}
