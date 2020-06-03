enum _boolean { TRUE, FALSE };
union _U { int i; long l; };
class _C {};
struct _S {};

typedef enum  _boolean boolean, *pboolean, *ppboolean, aboolean[];

boolean v1;
pboolean pv1;
ppboolean ppv1;
void foo(aboolean p);

typedef union _U U, *PU, **PPU, AU[];

U v2;
PU pv2;
PPU ppv2;
void foo(AU p);

typedef class _C C, *PC, **PPC, AC[];

C v3;
PC pv3;
PPC ppv3;
void foo(AC p);

typedef struct _S S, *PS, **PPS, AS[];

S v4;
PS pv4;
PPS ppv4;
void foo(AS p);

typedef enum { B_FALSE, B_TRUE } boolean_t;
boolean_t tt;

