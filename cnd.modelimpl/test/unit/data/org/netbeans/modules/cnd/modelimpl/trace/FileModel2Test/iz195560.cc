#define M195560_1(arg...) f195560_1(int i, arg)
M195560_1(double k) {}

#define M195560_2(arg...) f195560_2(double i, ##arg, int kkkk)
#define M195560_3(i1, arg...) f195560_3(float i, i1, ##arg)
M195560_2(int k) {}
M195560_2() {}

M195560_3(int k, int j) {}
M195560_3(int k) {}
