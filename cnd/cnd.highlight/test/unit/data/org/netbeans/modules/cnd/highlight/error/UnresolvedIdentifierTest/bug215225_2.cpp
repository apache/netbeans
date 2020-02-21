#define bug215225_2_A       \
 union {                    \
   struct { int i; } x;     \
 } y;

struct bug215225_2_S
{
    bug215225_2_A
};

int bug215225_2_foo() {
     bug215225_2_S a;
     a.y.x.i++;
}