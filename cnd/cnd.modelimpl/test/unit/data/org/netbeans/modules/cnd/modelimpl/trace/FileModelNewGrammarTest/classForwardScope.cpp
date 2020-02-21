
struct AAA
{   
    typedef int category;     
};

struct BBB {
    friend class AAA;
    
    AAA::category var;
};
