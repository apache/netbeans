template< typename T > struct remove_reference {
    typedef T type;
};

template <typename ParserT, typename ScannerT>
struct parser_result {
    typedef typename remove_reference<ParserT>::type parser_type;
    typedef typename parser_type::template result<ScannerT>::type type;
};

template <typename A, typename B>
struct longest_alternative {
    typedef longest_alternative<A, B> self_t;

    template <typename ScannerT>
            void parse(ScannerT const& scan) const {
        typedef typename parser_result<self_t, ScannerT>::type result_t;
        result_t l;
        l.length();
    }
};