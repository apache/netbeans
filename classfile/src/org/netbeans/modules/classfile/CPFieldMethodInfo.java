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

/**
 * The base class for field, method, and interface method constant pool types.
 *
 * @author Thomas Ball
 */
abstract class CPFieldMethodInfo extends CPNameAndTypeInfo {
    int iClass;
    int iNameAndType;

    CPFieldMethodInfo(ConstantPool pool,int iClass,int iNameAndType) {
        super(pool);
        this.iClass = iClass;
        this.iNameAndType = iNameAndType;
    }

    public final int getClassID() {
        return iClass;
    }

    public final int getFieldID() {
        return iNameAndType;
    }

    public final ClassName getClassName() {
        return ClassName.getClassName(
            ((CPName)pool.cpEntries[iClass]).getName());
    }

    void setClassNameIndex(int index) {
	iClass = index;
    }

    public final String getFieldName() {
	return ((CPNameAndTypeInfo)pool.cpEntries[iNameAndType]).getName();
    }

    @Override
    public String toString() {
        return getClass().getName() + ": class=" + getClassName() +     //NOI18N
            ", name=" + getName() + ", descriptor=" + getDescriptor();  //NOI18N
    }
    
    public final String getSignature() {
        return getSignature(getDescriptor(), true);
    }
    
    static String getSignature(String s, boolean fullName) {
        StringBuffer sb = new StringBuffer();
        int arrays = 0;
        int i = 0;
        while (i < s.length()) {
            char ch = s.charAt(i++);
            switch (ch) {
                case 'B': sb.append("byte"); continue; //NOI18N
                case 'C': sb.append("char"); continue; //NOI18N
                case 'D': sb.append("double"); continue; //NOI18N
                case 'F': sb.append("float"); continue; //NOI18N
                case 'I': sb.append("int"); continue; //NOI18N
                case 'J': sb.append("long"); continue; //NOI18N
                case 'S': sb.append("short"); continue; //NOI18N
                case 'Z': sb.append("boolean"); continue; //NOI18N
                case 'V': sb.append("void"); continue; //NOI18N
                
                case 'L':
                    int l = s.indexOf(';');
                    String cls = s.substring(1, l).replace('/', '.');
                    if (!fullName) {
                        int idx = cls.lastIndexOf('.');
                        if (idx >= 0)
                            cls = cls.substring(idx+1);
                    }
                    sb.append(cls);
                    i = l + 1;
                    continue;
                
                case '[':
                    arrays++;
                    continue;
                default:
                    break; // invalid character
            }
        }
        while (arrays-- > 0)
            sb.append("[]");
        return sb.toString();
    }
    
    @Override
    void resolve(CPEntry[] cpEntries) {
        // Read in NameAndTypeInfo values.
        CPNameAndTypeInfo nati = (CPNameAndTypeInfo)cpEntries[iNameAndType];
        setNameIndex(nati.iName);
        setDescriptorIndex(nati.iDesc);
    }
}
