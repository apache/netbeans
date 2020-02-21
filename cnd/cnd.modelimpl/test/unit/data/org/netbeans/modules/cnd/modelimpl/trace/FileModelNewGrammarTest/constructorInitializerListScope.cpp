struct AAA {    
    
    typedef int category;
    
    category field1, field2;

    AAA(int cat);
        
};

AAA::AAA(int cat) : field1((category)cat), field2((category)(cat + 1)) {} ;
