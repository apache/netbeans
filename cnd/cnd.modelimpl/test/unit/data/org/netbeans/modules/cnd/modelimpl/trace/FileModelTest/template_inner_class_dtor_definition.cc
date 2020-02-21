template<class T> class Outer2 {
	struct Inner {
		~Inner();
		void foo();
	};

};

template<class T> Outer2<T>::Inner::~Inner() {
}

template<class T> void Outer2<T>::Inner::foo() {
}
