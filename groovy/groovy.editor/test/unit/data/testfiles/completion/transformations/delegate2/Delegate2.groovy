public class Delegation2 {
    public String fieldDelegatedShowMe
}


class Showcase {
    @Delegate public Delegation2 fieldDelegation
    
    public String fieldNormal
}

Showcase showcase = new Showcase();
showcase.f
