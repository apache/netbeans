#define M(...) f(int i, ##__VA_ARGS__, int j)
M() {}
M(int k) {}
