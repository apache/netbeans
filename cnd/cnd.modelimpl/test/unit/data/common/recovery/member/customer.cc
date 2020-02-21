#include "customer.h"

 
class Customer {
    public:
        Customer(const string initName, int initDiscount);
        string GetName() const;
        
    private:
        string name;

        friend ostream& operator<< (ostream&, const Customer&);
};

Customer::Customer(const string initName, int initDiscount) :
    name(initName) {
}

string Customer::GetName() const {
    return name;
}

ostream& operator <<(ostream& output, const Customer& customer) {
    output << customer.name;
    return output;
}

