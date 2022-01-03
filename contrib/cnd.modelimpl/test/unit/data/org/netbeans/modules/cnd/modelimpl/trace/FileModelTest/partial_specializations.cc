template<class T1, class T2> class C {  
	void foo(T1 t1, T2 t2);
};

template<class T> class C<int, T> {
	void foo(int t1, T t2);
};

template<class T> class C<T, int> {
	void foo(T t1, int t2);
};

template<class T1, class T2> class C<T1*, T2> {
	void foo(T1 t1, T2 t2);
};

template<class T1, class T2> class C<T1, T2*> {
	void foo(T1 t1, T2 t2);
};

template<class T1, class T2> class C<T1*, T2*> {
	void foo(T1 t1, T2 t2);
};

template<class T> class C<T, T> {
	void foo(T t1, T t2);
};

template<class T, class P> bool operator<(T x, P y) {
	return x<y;
};
