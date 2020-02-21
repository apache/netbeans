#define BOOST_REGEX_DECL

namespace std {
    class locale {};
}

#define BOOST_REGEX_CHAR_T char

template <class charT>
struct cpp_regex_traits_base {
    std::locale imbue(const std::locale& l);
};

#define template __extension__ extern template

namespace re_detail{

template BOOST_REGEX_DECL
std::locale cpp_regex_traits_base<BOOST_REGEX_CHAR_T>::imbue(const std::locale& plocale);

}

#undef template
