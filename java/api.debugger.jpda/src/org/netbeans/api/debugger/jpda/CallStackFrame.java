/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;


/**
 * Represents one stack frame.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @author Jan Jancura
 */
public interface CallStackFrame {
    
    /**
     * Returns line number associated with this stack frame.
     *
     * @param struts a language name or null for default language
     * @return line number associated with this this stack frame
     */
    public abstract int getLineNumber (String struts);
    
    /**
     * Get the depth of this stack frame.
     * @return The depth
     * @since 2.16
     */
    public abstract int getFrameDepth();

    /**
     * Returns the current operation (if any) at the location of this call stack frame.
     *
     * @param struts a language name or null for default language
     * @return  The operation at the frame location if available and this frame does not
     *          represent a native method invocation; <CODE>null</CODE> otherwise
     */
    public abstract Operation getCurrentOperation(String struts);

    /**
     * Returns method name associated with this stack frame.
     *
     * @return method name associated with this stack frame
     */
    public abstract String getMethodName ();

    /**
     * Returns class name of this stack frame.
     *
     * @return class name of this stack frame
     */
    public abstract String getClassName ();

    /**
    * Returns name of default stratum.
    *
    * @return name of default stratum
    */
    public abstract String getDefaultStratum ();

    /**
    * Returns name of default stratum.
    *
    * @return name of default stratum
    */
    public abstract List<String> getAvailableStrata ();

    /**
     * Returns name of file this stack frame is stopped in.
     *
     * @param struts a language name or null for default language
     * @return name of file this stack frame is stopped in
     * @throws AbsentInformationException if information about source is not 
     *   included in class file
     */
    public abstract String getSourceName (String struts) 
    throws AbsentInformationException;
    
    /**
     * Returns source path of file this frame is stopped in or null.
     *
     * @return source path of file this frame is stopped in or null
     */
    public abstract String getSourcePath (String stratum) 
    throws AbsentInformationException;
    
    /**
     * Returns local variables.
     *
     * @return local variables
     */
    public abstract LocalVariable[] getLocalVariables () 
    throws AbsentInformationException;
    
    /**
     * Returns arguments of the current method (if any).
     * @return The array of arguments or
     *         <code>null</code> when it's not possible to retrieve the arguments.
     *
     * (Possible to uncomment if this method is necessary. We have the implementation.)
    public abstract LocalVariable[] getMethodArguments();
     */

    /**
     * Returns object reference this frame is associated with or null (
     * frame is in static method).
     *
     * @return object reference this frame is associated with or null
     */
    public abstract This getThisVariable ();
    
    /**
     * Sets this frame current.
     *
     * @see JPDADebugger#getCurrentCallStackFrame
     */
    public abstract void makeCurrent ();
    
    /**
     * Returns <code>true</code> if this frame is obsoleted.
     *
     * @return <code>true</code> if this frame is obsoleted
     */
    public abstract boolean isObsolete ();
    
    /** UNCOMMENT WHEN THIS METHOD IS NEEDED. IT'S ALREADY IMPLEMENTED IN THE IMPL. CLASS.
     * Determine, if this stack frame can be poped off the stack.
     *
     * @return <code>true</code> if this frame can be poped
     *
    public abstract boolean canPop();
     */
    
    /**
     * Pop stack frames. All frames up to and including the frame 
     * are popped off the stack. The frame previous to the parameter 
     * frame will become the current frame.
     */
    public abstract void popFrame ();
    
    /**
     * Returns thread.
     *
     * @return thread
     */
    public abstract JPDAThread getThread ();
    
    /**
     * Get the list of monitors aquired in this frame
     * 
     * @return the list of monitors aquired in this frame
     * @since 2.16
     */
    List<MonitorInfo> getOwnedMonitors();
}
 
