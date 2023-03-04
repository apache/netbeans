/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * Contributor(s): Thomas Ball
 */

package org.netbeans.modules.classfile;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * An entry in the exception table of a method's code attribute.
 *
 * @author  Thomas Ball
 */
public final class ExceptionTableEntry {

    int startPC;
    int endPC;
    int handlerPC;
    CPClassInfo catchType;  // may be null for "finally" exception handler

    static ExceptionTableEntry[] loadExceptionTable(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int n = in.readUnsignedShort();
        ExceptionTableEntry[] exceptions = new ExceptionTableEntry[n];
        for (int i = 0; i < n; i++)
            exceptions[i] = new ExceptionTableEntry(in, pool);
        return exceptions;
    }

    /** Creates new ExceptionTableEntry */
    ExceptionTableEntry(DataInputStream in, ConstantPool pool) 
      throws IOException {
        loadExceptionEntry(in, pool);
    }

    private void loadExceptionEntry(DataInputStream in, ConstantPool pool) 
      throws IOException {
        startPC = in.readUnsignedShort();
        endPC = in.readUnsignedShort();
        handlerPC = in.readUnsignedShort();
        int typeIndex = in.readUnsignedShort();
        if (typeIndex != 0) // may be 0 for "finally" exception handler
	    try {
	        catchType = pool.getClass(typeIndex);
	    } catch (IndexOutOfBoundsException e) {
	        throw new InvalidClassFileAttributeException(
                    "invalid catchType (" + typeIndex + ") in exception table entry", e);
	    }
    }
    
    /**
     * Returns the beginning offset into the method's bytecodes of this
     * exception handler.
     * @return beginning offset of the exception handling code into
     * the method bytecode
     */
    public final int getStartPC() {
        return startPC;
    }
    
    /**
     * Returns the ending offset into the method's bytecodes of this
     * exception handler, or the length of the bytecode array if the
     * handler supports the method's last bytecodes (JVM 4.7.3).
     * @return ending offset of the exception handling code into
     * the method bytecode
     */
    public final int getEndPC() {
        return endPC;
    }
    
    /**
     * Returns the starting offset into the method's bytecodes of the 
     * exception handling code.
     * @return starting offset of the exception handling code into
     * the method bytecode
     */
    public final int getHandlerPC() {
        return handlerPC;
    }
    
    /**
     * Returns the type of exception handler, or <code>null</code>
     * if this handler catches all exceptions, such as an exception
     * handler for a "<code>finally</code>" clause (JVM 4.7.3).
     * @return type of exception handler
     */
    public final CPClassInfo getCatchType() {
        return catchType;
    }
}
