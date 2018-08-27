package org.netbeans.test.java.hints;

public class MemberSelect {
    
    public MemberSelect() {
    }
    
    public static void main(String[] args) {
        MemberSelect m = null;
        int i = 0;
        
        MemberSelect.a = i;
        i = MemberSelect.a;
        
        m.a = i;
        i = m.a;
        
        Class c = Undef.class;
    }
}
