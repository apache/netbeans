package test;

public class FullMemberSelect109886 {

    public enum InnerEnum {
    }

    public class InnerClass {
    }
    
    public interface InnerInterface {
    }

    public static void main(String[] args) throws Exception {
	InnerEnum e1 = null;
	InnerClass c1 = null;
	InnerInterface i1 = null;
	Test.InnerEnum e2 = null;
	Test.InnerClass c2 = null;
	Test.InnerInterface i2 = null;
	test.Test.InnerEnum e3 = null;
	test.Test.InnerClass c3 = null;
	test.Test.InnerInterface i3 = null;
	test.Test test = null;
    }
}
