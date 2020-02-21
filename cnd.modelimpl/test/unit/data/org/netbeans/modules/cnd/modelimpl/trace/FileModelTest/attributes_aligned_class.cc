namespace align {
    struct  __attribute__((__aligned__(2)))     a2 {};
    struct  __attribute__((__aligned__(4)))     a4 {};
    class   __attribute__((__aligned__(8)))     a8 {};
    class   __attribute__((__aligned__(16)))    a16 {};
    union   __attribute__((__aligned__(32)))    a32 {};
}
