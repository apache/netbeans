class Base {
	public:
		static Base convert(const char*);
		class Helper {
		};
};

class Derived : private Base {
	using Base::convert;
	//using Base::Helper;
	class Inner {
		//Helper h; error! Base::Helper is not accessible from Derived::Inner.
	};
	Helper h;
};

struct A {
	class I {};
};

struct B : A {
	I i;
};
