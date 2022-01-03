struct sse_t { _Alignas(16) float sse_data[4]; };
struct data { char x; _Alignas(128) char cacheline[128]; };
_Thread_local static int local_state;
int main(void) {
    int s1 = sizeof (struct data);
    int a1 = _Alignof(struct sse_t);
    _Alignas(2048) struct data d;
    int a2 = _Alignof(char);
    int a3 = _Alignof(float[10]);
    //int a4 = _Alignof(struct { char c; int n;}); // unresolved c and n
    //int a5 = sizeof(struct { char c; int n;}); // unresolved c and n
    _Static_assert(2 * 2 == 4, "2*2 must be 4!");
}
