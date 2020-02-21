#define C1(x) x##0_1
void C1(foo_149225_)() {}

#define C2(x) x##999_z_z_00
void C2(foo_149225_)() {}

#define C3(x) x##0xDEADBEEF_z_z_z
void C3(foo_149225_)() {}

#define C4(x,y) x##y
void C4(foo_,149225_a)() {}

#define C5(x) C4(x,149225_b)
void C5(foo_)() {}
