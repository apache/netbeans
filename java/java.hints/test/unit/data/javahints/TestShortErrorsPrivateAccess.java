package javahints;

public class TestShortErrorsPrivateAccess {
    
    public TestShortErrorsPrivateAccess() {
    }
    
    public static void test() {
        ReferredTo./*sdffds*/  test();
        int a = ReferredTo./*sdffds*/  a;
        
        new /*sdffds*/  ReferredTo();
        
        ReferredTo./*sdffds*/  A k;
    }
    
}
