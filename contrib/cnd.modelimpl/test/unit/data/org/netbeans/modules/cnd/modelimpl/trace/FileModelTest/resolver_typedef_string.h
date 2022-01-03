//
// File:   resolver_typedef_string.h
// Author: vv159170
//
// Created on May 4, 2006, 2:33 PM
//

#ifndef _resolver_typedef_string_H
#define	_resolver_typedef_string_H

namespace std_2 {
    template<typename _Alloc>
    class allocator;
    
    template<class _CharT>
    struct char_traits;
    
    template<typename _CharT, typename _Traits = char_traits<_CharT>,
    typename _Alloc = allocator<_CharT> >
    class basic_string {
    };
    
    typedef std_2::basic_string<char> string;
}

namespace std_2 {
    typedef basic_string<wchar_t> wstring;
}

using std_2::string;

std_2 :: wstring wrt_2(string str);

namespace resolver_typedef_string {
  string read();
  using namespace std_2;
  class ClassA {

  public:
    wstring read() const;
  };
}

#endif	/* _resolver_typedef_string_H */

