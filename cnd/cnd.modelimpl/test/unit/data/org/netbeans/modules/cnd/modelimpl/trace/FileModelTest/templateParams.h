template<unsigned int L, class T, template<class> class C>
    class OrderedStatic<L, T, Loki::NullType> : public Private::OrderedStaticBase<T>
    {
    public:
        OrderedStatic() : Private::OrderedStaticBase<T>(L)
        {
            OrderedStaticManager::Instance().registerObject
                                (L,this,&Private::OrderedStaticCreatorFunc::createObject);
        }

        C createObject()
        {
            Private::OrderedStaticBase<T>::SetLongevity(new C<T>);
        }

    private:
        OrderedStatic(const OrderedStatic&);
        OrderedStatic& operator=(const OrderedStatic&);
    };

    template <template <class, class> class ThreadingModel,
              class MX = LOKI_DEFAULT_MUTEX >
    struct RefCountedMTAdj
    {
        template <class P>
        class RefCountedMT : public ThreadingModel< RefCountedMT<P>, MX >
        {
            typedef ThreadingModel< RefCountedMT<P>, MX > base_type;
            typedef typename base_type::IntType       CountType;
            typedef volatile CountType               *CountPtrType;
        };
    };

template<unsigned int L, class T, template<class> class C> C foo(T t, C c) {
    T t1 = L;
    C c1;
    boo(L,T,c1);
};

T<int (int)> x;

class A{
public:
    static const int b;
};

const int A::b = 1;

template <bool b> class T {
};

typedef T<A::b == 1> Z;

template <typename ScannerT>
static typename parser_result<chlit<>, ScannerT>::type
parse_dot(ScannerT& scan) {
    return ch_p('.').parse(scan);
}

typedef MakeTypelist<>::Result null_tl;

typedef Loki::Seq
<
struct Foo,
struct Boo
>::Type ClassList;

// IZ 144225 : IDE highlights enum in template as wrong code
enum level_type {
    ONE, TWO
};

template<class T, enum level_type L>
inline bool compr(T t, enum level_type l)
{
    return t >= (int)l;
}

// IZ#151957: 9 parser's errors in boost 1.36
template<typename frac_sec_type,
typename frac_sec_type::int_type resolution_adjust>
class time_resolution_traits {
public:
    time_resolution_traits() {
    }
};

