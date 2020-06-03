#ifndef _OPERATORS_H_
#define _OPERATORS_H_

class Cls {
    public:
		Cls& operator =  (const Cls&);
		Cls& operator += (const Cls&) {
		return *this;
	}
};

Cls operator + (const Cls&, const Cls&);

Cls operator - (const Cls& a, const Cls& b) {
	Cls cls;
	return cls;
}

//void operator << (ClassWithOps& obj, int);

#endif // _OPERATORS_H_

