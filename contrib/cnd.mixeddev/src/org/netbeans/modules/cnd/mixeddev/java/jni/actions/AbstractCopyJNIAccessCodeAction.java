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
