namespace bug242861 {
    struct AAA_242861 {
        friend struct BBB_242861; 
        int a_242861;
    }; 
     
    struct BBB_242861 {};
}  