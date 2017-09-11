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
 * An InnerClass attribute of a classfile.
 *
 * @author  Thomas Ball
 */
public final class InnerClass {

    ClassName name;
    ClassName outerClassName;
    String simpleName;
    int access;

    static InnerClass[] loadInnerClasses(DataInputStream in, ConstantPool pool)
      throws IOException {
        int n = in.readUnsignedShort();
        InnerClass[] innerClasses = new InnerClass[n];
        for (int i = 0; i < n; i++)
            innerClasses[i] = new InnerClass(in, pool);
        return innerClasses;
    }

    InnerClass(DataInputStream in, ConstantPool pool) 
      throws IOException {
        loadInnerClass(in, pool);
    }

    private void loadInnerClass(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int index = in.readUnsignedShort();
        name = (index > 0) ? pool.getClass(index).getClassName() : null;
        index = in.readUnsignedShort();
	outerClassName = (index > 0) ? pool.getClass(index).getClassName() : null;
        index = in.readUnsignedShort();
        if (index > 0) {
            CPUTF8Info entry = (CPUTF8Info)pool.get(index);
            simpleName = entry.getName();
        }
        access = in.readUnsignedShort();
    }

    /** Returns the name of this class, including its package (if any).
     * If the compiler didn't define this value, the string 
     * "<not defined>" is returned.
     * @return the name of this class.
     */    
    public final ClassName getName() {
        return name;
    }
    
    /** Returns the name of the enclosing outer class, including 
     *  its package (if any).  
     * @return the name of this class, or null if not available.
     */    
    public final ClassName getOuterClassName() {
        return outerClassName;
    }

    /**
     * Returns the original simple name as given in the source code.
     * If this is an anonymous class, null is returned instead.
     * @return the simple name of this class, or null if anonymous.
     */
    public final String getSimpleName() {
        return simpleName;
    }

    /**
     * Returns the access flags of this class.
     */
    public final int getAccess() {
        return access;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("innerclass=");
        sb.append(name);
        if (simpleName != null) {
            sb.append(" (");
            sb.append(simpleName);
            sb.append(')');
        }
        sb.append(", outerclass=");
        sb.append(outerClassName);
        return sb.toString();
    }
}
