__declspec(naked) int test() {
    int x, y, z;

    _asm
    {
        push BP
        mov BP, SP
        sub SP, __LOCAL_SIZE
        mov BX, __LOCAL_SIZE[BP]
        mov BX, __LOCAL_SIZE + 2[BP]
        mov AX, __LOCAL_SIZE
        mov AX, __LOCAL_SIZE + 2
    }
    _asm
    {
        mov SP, BP
        pop BP
        ret
    }
    __asm__ ("call_pal %1" : "=r"(id) : "i"(PAL_rduniq));

    __asm(".long 0x47e00c20" : "=r"(_v0) : "0"(_v0));

    long _v0 __asm("$0") = -1;
    unsigned long id __asm__("$0");

}
