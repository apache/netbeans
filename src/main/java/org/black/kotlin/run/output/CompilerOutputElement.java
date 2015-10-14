package org.black.kotlin.run.output;

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity;


public class CompilerOutputElement {
    
    private final CompilerMessageSeverity messageSeverity;
    private final String message;
    private final CompilerMessageLocation messageLocation;
    
    public CompilerOutputElement (CompilerMessageSeverity messageSeverity, String message, CompilerMessageLocation messageLocation) {
        this.messageSeverity = messageSeverity;
        this.message = message;
        this.messageLocation = messageLocation;
    }
    
    public CompilerMessageSeverity getMessageSeverity() {
        return messageSeverity;
    }
    
    public String getMessage() {
        return message;
    }
    
    public CompilerMessageLocation getMessageLocation() {
        return messageLocation;
    }
}
