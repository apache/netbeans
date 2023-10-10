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
/*
 * Contributor(s): Thomas Ball
 */

package org.netbeans.modules.classfile;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * An entry in the local variable table of a method's code attribute.
 *
 * @author  Thomas Ball
 */
public final class LocalVariableTableEntry {

    int startPC;
    int length;
    String name;
    String description;
    int index;

    static LocalVariableTableEntry[] loadLocalVariableTable(DataInputStream in, ConstantPool pool)
      throws IOException {
        int n = in.readUnsignedShort();
        LocalVariableTableEntry[] entries = new LocalVariableTableEntry[n];
        for (int i = 0; i < n; i++)
            entries[i] = new LocalVariableTableEntry(in, pool);
        return entries;
    }

    /** Creates new LocalVariableTableEntry */
    LocalVariableTableEntry(DataInputStream in, ConstantPool pool) 
      throws IOException {
        loadLocalVariableEntry(in, pool);
    }

    private void loadLocalVariableEntry(DataInputStream in, ConstantPool pool) 
      throws IOException {
        startPC = in.readUnsignedShort();
        length = in.readUnsignedShort();
        Object o = pool.get(in.readUnsignedShort());
        if (!(o instanceof CPUTF8Info))
          throw new InvalidClassFormatException();
        CPUTF8Info entry = (CPUTF8Info)o;
        name = entry.getName();
        o = pool.get(in.readUnsignedShort());
        if (!(o instanceof CPUTF8Info))
          throw new InvalidClassFormatException();
        entry = (CPUTF8Info)o;
        description = entry.getName();
        index = in.readUnsignedShort();
    }

    /**
     * Returns the first byte code offset where this variable is valid.
     * @return first bytecode offset where this variable is valid
     */ 
    public final int getStartPC() {
        return startPC;
    }

    /**
     * Returns the length of the range of code bytes where this variable
     *         is valid.  
     * @return length of range of code bytes where this variable is valid
     */
    public final int getLength() {
        return length;
    }

    /**
     * Returns the name of this variable.
     * @return name of the variable
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the signature (type) of this variable.
     * @return signature of this variable
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Returns the variable's index into the local variable array
     *         for the current stack frame.
     * @return index of the variable
     */
    public final int getIndex() {
        return index;
    }
}
