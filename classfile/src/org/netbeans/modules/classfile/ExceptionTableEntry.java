/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
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
     */
    public final int getStartPC() {
        return startPC;
    }
    
    /**
     * Returns the ending offset into the method's bytecodes of this
     * exception handler, or the length of the bytecode array if the
     * handler supports the method's last bytecodes (JVM 4.7.3).
     */
    public final int getEndPC() {
        return endPC;
    }
    
    /**
     * Returns the starting offset into the method's bytecodes of the 
     * exception handling code.
     */
    public final int getHandlerPC() {
        return handlerPC;
    }
    
    /**
     * Returns the type of exception handler, or <code>null</code>
     * if this handler catches all exceptions, such as an exception
     * handler for a "<code>finally</code>" clause (JVM 4.7.3).
     */
    public final CPClassInfo getCatchType() {
        return catchType;
    }
}
