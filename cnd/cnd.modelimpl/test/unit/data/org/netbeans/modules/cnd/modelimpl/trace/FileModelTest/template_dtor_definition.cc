template<class T> class Cls_1 {
	Cls_1();
	virtual ~Cls_1();
};


template<class T> inline Cls_1<T>::~Cls_1() {
}

template<class T> class Cls_2 {
	Cls_2();
	virtual ~Cls_2();
};


template<class T> Cls_2<T>::~Cls_2() {
}


template<class T> class Outer1 {
	struct Inner {
		~Inner();
	};

};

template<class T> Outer1<T>::Inner::~Inner() { 
}

