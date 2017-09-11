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
 * A class representing the enclosing method of an inner class.  An
 * enclosing method is similar to a CPMethodInfo type, but differs
 * in two respects.  First, the classfile stores this information in
 * an "EnclosingMethod" attribute, rather than in the constant pool
 * Second, an enclosing method attribute may not actually have a
 * method reference (only a class reference).  This is because the
 * inner class is defined in an init block instead of an actual
 * method.  
 *
 * @see org.netbeans.modules.classfile.ClassFile#getEnclosingMethod
 * @author Thomas Ball
 */
public final class EnclosingMethod {
    final CPClassInfo classInfo;
    final CPNameAndTypeInfo methodInfo;

    EnclosingMethod(ConstantPool pool, CPClassInfo classInfo, int iMethod) {
	this.classInfo = classInfo;
	methodInfo = iMethod > 0 ? (CPNameAndTypeInfo)pool.get(iMethod) : null;
    }

    public ClassName getClassName() {
        return classInfo.getClassName();
    }

    /**
     * Returns the constant pool entry for the enclosing class.
     */
    public CPClassInfo getClassInfo() {
	return classInfo;
    }

    /**
     * Returns whether the enclosing method attribute describes a method
     * the inner class is defined within.  If false, then the inner
     * class was defined in an init block (or statement) in the class,
     * outside of any method or constructor bodies.
     */
    public boolean hasMethod() {
	return methodInfo != null;
    }

    /**
     * Returns the constant pool entry for the enclosing method, or
     * null if the inner class was defined outside of any method or
     * constructor bodies.
     *
     * Note: a CPNameAndTypeInfo instance is returned because the method
     * is external to the enclosed class.  Do not attempt to cast it to a
     * CPMethodInfo type, which is an internal method structure.
     */
    public CPNameAndTypeInfo getMethodInfo() {
	return methodInfo;
    }

    @Override
    public String toString() {
	String methodString = methodInfo != null 
	    ? methodInfo.toString() : "<no method>";
        return "enclosing method: class=" + getClassName() + //NOI18N
	    ", method=" + methodString;   //NOI18N
    }
}
