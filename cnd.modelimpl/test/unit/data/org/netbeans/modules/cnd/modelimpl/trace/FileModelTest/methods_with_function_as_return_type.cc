
template<int __inst>
class __malloc_alloc_template {

    static void (* __set_malloc_handler(void (*__f)()))() {
        void (* __old)() = __malloc_alloc_oom_handler;
        __malloc_alloc_oom_handler = __f;
        return __old;
    }
};
