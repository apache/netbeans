
namespace NS191799 {
    struct Outer191799 {
        struct Inner191799;
        friend void foo191799();
    };

    struct Outer191799::Inner191799 {
        friend void foo191799();
    };
    
    void foo191799() {
        
    }
}
