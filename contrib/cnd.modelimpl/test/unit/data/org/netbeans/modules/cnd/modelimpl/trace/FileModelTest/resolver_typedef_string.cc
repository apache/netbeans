#include "resolver_typedef_string.h"

std_2::wstring wrt_2(string str)
{
    using std_2::wstring;
    wstring out;
    return out;
}

namespace resolver_typedef_string {
    string read() {
        string str;
        return str;
    }
    
    wstring ClassA::read() const {
        wstring str;
        return str;
    }
}
