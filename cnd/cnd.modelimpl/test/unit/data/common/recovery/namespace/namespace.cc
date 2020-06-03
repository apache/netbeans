//class Customer;
//class list;
//class string;

namespace {
    list<Customer> customers;
 
    int foo(int name){
        for(list<Customer>::iterator it = customers.begin(); it != customers.end(); ++it) {
            if ((*it).GetName() == name) {
                return (*it).GetDiscount();
            }
        }
        return -1;
    }
}

int main(int argc, char** argv) {
    foo(argv);
    return 0;
}
