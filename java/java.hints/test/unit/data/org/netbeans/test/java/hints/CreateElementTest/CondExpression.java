package org.netbeans.test.java.hints;

public class CondExpression {
    
    public CondExpression() {
    }
    
    public void main(String[] args) {
        String s = null;
        CharSequence o = b ? d : s;
        
        o = b ? s : d;
    }
}
