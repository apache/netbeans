// 
// File:   resolver_class_string.h
// Author: vv159170
//
// Created on May 4, 2006, 2:33 PM
//

#ifndef _resolver_class_string_H
#define	_resolver_class_string_H

namespace std_1 {
    template<typename _Alloc>
    class allocator;
    
    template<class _CharT>
    struct char_traits;
    
    template<typename _CharT, typename _Traits = char_traits<_CharT>,
    typename _Alloc = allocator<_CharT> >
    class basic_string {
    };
    
    class string : public std_1::basic_string<char> {
        
    };
}

namespace std_1 {
    class wstring1 : protected basic_string<wchar_t> {
        
    };
}

using std_1::string;

std_1 :: wstring1 wrt_1(string str);


namespace resolver_class_string {
  string read();
  using namespace std_1;
  class ClassA {
  public:
    wstring1 read() const;
  };
}

using resolver_class_string::ClassA;

#endif	/* _resolver_class_string_H */

