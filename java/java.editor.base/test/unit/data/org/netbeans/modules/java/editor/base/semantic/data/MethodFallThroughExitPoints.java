package test;

public class MethodFallThroughExitPoints {
    
    public void simpleFallThroughExitPoint() {
    }
    
    public void fallThroughExitPointWithIf() {
        if (Boolean.getBoolean(""))
            return;
    }
    
    public void notFallThroughIfWithElse() {
        if (Boolean.getBoolean(""))
            return;
        else
            return;
    }
    
    public void fallThroughExitPointWithTryCatch() {
        try {
            return;
        } catch (RuntimeException t) {
            return;
        } catch (Error t) {
        }
    }
    
    public void notFallThroughTryCatchWithReturns() {
        try {
            return;
        } catch (RuntimeException t) {
            return;
        } catch (Error t) {
            return;
        }
    }
    
    public void notFallThroughFinallyWithReturn() {
        try {
        } catch (Throwable t) {
        } finally {
            return ;
        }
    }
    
    public void notFallThroughThrow() {
        throw new IllegalStateException();
    }
    
}
