public interface Delegation {
    void showMe();
}

class Showcase {
    @Delegate Delegation delegation
    
    void normal() {
        
    }
}

Showcase showcase = new Showcase();
showcase.
