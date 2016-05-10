package org.black.kotlin.navigation.references;

public enum ReferenceAccess {
    READ(true,false), WRITE(false,true), READ_WRITE(true,true);
    
    private final boolean isRead, isWrite;
    
    ReferenceAccess(boolean isRead, boolean isWrite){
        this.isRead = isRead;
        this.isWrite = isWrite;
    }
}
