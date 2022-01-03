typedef struct _AAA258327 {
    _Atomic(int) a258327;
} AAA258327;

typedef _Atomic(AAA258327 (*)(int)) funtype258327;
typedef _Atomic(AAA258327) type258327;
typedef _Atomic(AAA258327)* ptype258327;
typedef const _Atomic(AAA258327)* cptype258327;

const _Atomic(int) foo258327() {
    return 0;
} 

const _Atomic int boo258327() {
    const _Atomic(int) p1;
    const _Atomic(int*)* p2;
    _Atomic(AAA258327(*)(int)) p3;
    return 0;
}