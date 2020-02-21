/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.mixeddev.java.jni.actions;

import javax.swing.text.Document;
import org.netbeans.modules.cnd.mixeddev.java.JNISupport;
import org.netbeans.modules.cnd.mixeddev.java.JavaContextSupport;
import static org.netbeans.modules.cnd.mixeddev.java.JavaContextSupport.renderQualifiedName;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaClassInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaEntityInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaFieldInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaMethodInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaParameterInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaTypeInfo;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class AbstractCopyJNIAccessCodeAction extends AbstractJNIAction {

    public AbstractCopyJNIAccessCodeAction(Lookup context) {
        super(context);
    }

    @Override
    protected boolean isEnabledAtPosition(Document doc, int caret) {
        JavaEntityInfo entity = resolveJavaEntity(doc, caret);
        if (entity != null) {
            if (entity instanceof JavaMethodInfo) {
                return !((JavaMethodInfo) entity).isNative();
            }
            return true;
        }
        return false;
    }
    
    protected String generateEntityGetter(JavaEntityInfo entity) {
        StringBuilder sb = new StringBuilder();
        if (entity instanceof JavaClassInfo) {
            generateJavaClassAccess(sb, (JavaClassInfo) entity);
        } else if (entity instanceof JavaMethodInfo) {
            generateJavaMethodCall(sb, (JavaMethodInfo) entity);
        } else if (entity instanceof JavaFieldInfo) {
            generateJavaFieldGetValue(sb, (JavaFieldInfo) entity);
        }
        return sb.toString();
    }
    
    protected String generateEntitySetter(JavaEntityInfo entity) {
        StringBuilder sb = new StringBuilder();
        if (entity instanceof JavaFieldInfo) {
            generateJavaFieldSetValue(sb, (JavaFieldInfo) entity);
        }
        return sb.toString();
    }
    
    private void generateJavaClassAccess(StringBuilder sb, JavaClassInfo cls) {
        // Class id
        sb.append("jclass cls = env->FindClass(\"") // NOI18N
            .append(renderQualifiedName(cls.getQualifiedName()))
            .append("\");\n"); // NOI18N
    }
    
    private void generateJavaMethodCall(StringBuilder sb, JavaMethodInfo mtd) {
        // Class id
        sb.append("jclass cls = env->FindClass(\"") // NOI18N
            .append(renderQualifiedName(mtd.getQualifiedName().subList(0, mtd.getQualifiedName().size() - 1)))
            .append("\");\n"); // NOI18N
        
        // Method id
        sb.append("jmethodID mtdId = env->") // NOI18N
            .append(mtd.isStatic() ? "GetStaticMethodID" : "GetMethodID") // NOI18N
            .append("(cls, \"") // NOI18N
            .append(mtd.getName())
            .append("\", \"") // NOI18N
            .append(JNISupport.getJNISignature(mtd))
            .append("\");\n"); // NOI18N
        
        // Call
        sb.append("env->"); // NOI18N
        sb.append("Call"); // NOI18N
        if (mtd.isStatic()) {
            sb.append("Static"); // NOI18N
        }
        sb.append(getTypeSpelling(mtd.getReturnType()));
        sb.append("Method(cls, mtdId"); // NOI18N
        for (JavaParameterInfo param : mtd.getParameters()) {
            sb.append(", ").append(param.getName()); // NOI18N
        }
        sb.append(");"); // NOI18N
    }
    
    private void generateJavaFieldAccess(StringBuilder sb, JavaFieldInfo fld) {
        sb.append("jclass cls = env->FindClass(\"") // NOI18N
            .append(renderQualifiedName(fld.getQualifiedName().subList(0, fld.getQualifiedName().size() - 1)))
            .append("\");\n"); // NOI18N
        sb.append("jmethodID fieldId = env->") // NOI18N
            .append(fld.isStatic() ? "GetStaticFieldID" : "GetFieldID") // NOI18N
            .append("(cls, \"") // NOI18N
            .append(fld.getName())
            .append("\", \"") // NOI18N
            .append(JNISupport.getJNISignature(fld))
            .append("\");\n"); // NOI18N
    }
    
    private void generateJavaFieldGetValue(StringBuilder sb, JavaFieldInfo fld) {
        generateJavaFieldAccess(sb, fld);
        sb.append("env->Get"); // NOI18N
        if (fld.isStatic()) {
            sb.append("Static"); // NOI18N
        }
        sb.append(getTypeSpelling(fld.getType()));
        sb.append("Field(cls, fieldId);"); // NOI18N
    }
    
    private void generateJavaFieldSetValue(StringBuilder sb, JavaFieldInfo fld) {
        generateJavaFieldAccess(sb, fld);
        sb.append("env->Set"); // NOI18N
        if (fld.isStatic()) {
            sb.append("Static"); // NOI18N
        }
        sb.append(getTypeSpelling(fld.getType()));
        sb.append("Field(cls, fieldId, value);"); // NOI18N
    }
    
    private String getTypeSpelling(JavaTypeInfo type) {
        if (type != null) {
            String typeName = type.getName().toString();
            switch (typeName) {
                case "boolean": // NOI18N
                    return "Boolean"; // NOI18N
                case "byte": // NOI18N
                    return "Byte"; // NOI18N
                case "char": // NOI18N
                    return "Char"; // NOI18N
                case "short": // NOI18N
                    return "Short"; // NOI18N
                case "int": // NOI18N
                    return "Int"; // NOI18N
                case "long": // NOI18N
                    return "Long"; // NOI18N
                case "float": // NOI18N
                    return "Float"; // NOI18N
                case "double": // NOI18N
                    return "Double"; // NOI18N
                case "void": // NOI18N
                    return "Void"; // NOI18N
            }
            return "Object"; // NOI18N
        }
        return ""; // NOI18N
    }
}
