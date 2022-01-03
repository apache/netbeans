namespace Loki {
    class NullType {};
    template<class T1, class T2
    > class DefaultThreadingModel {};
    
    template <typename R = void, class TList = NullType,
        template<class, class> class ThreadingModel = DefaultThreadingModel>
    class Functor
    {
    public:
        bool empty() const;
        void clear();
    };

    template<class R = void()>
    struct Function;

    template<>
    struct Function<>
        : public Loki::Functor<>
    {
    };

    typedef Function<void()> func_void_type_2;
    func_void_type_2 v22;
    
    static void test_zero_args()
    {
        typedef Function<void()> func_void_type;
        func_void_type v1;
        v1.empty(); // empty is unresolved
        v1.clear(); // clear is unresolved
        v22.clear();
    }
}
