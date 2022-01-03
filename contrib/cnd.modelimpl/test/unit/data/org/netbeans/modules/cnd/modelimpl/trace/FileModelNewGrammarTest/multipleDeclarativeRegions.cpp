namespace BBB { 
    typedef int dummy_type;
}
          
namespace AAA {
    using namespace BBB;
    
    typedef int type_1;
    
    class XXX;
}

namespace BBB {
    using namespace AAA;
    
    typedef int type_2;
} 
 

BBB::type_1 var1;
AAA::type_2 var2; 


int main() {
    return 0;
}  