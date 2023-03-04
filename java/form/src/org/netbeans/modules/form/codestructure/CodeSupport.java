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

package org.netbeans.modules.form.codestructure;

import java.util.*;
import java.lang.reflect.*;

/**
 * @author Tomas Pavek
 */

class CodeSupport {

    private CodeSupport() {
    }

    static String getSourceClassName(Class cls) {
        return cls.getName().replace('$', '.').replace('+', '.').replace('/', '.'); // NOI18N
    }

    // ----------
    // implementation classes of CodeStatement interface

    static final class MethodStatement extends AbstractCodeStatement {
        private Method performMethod;
        private CodeExpression[] parameters;

        public MethodStatement(CodeExpression exp,
                               Method m,
                               CodeExpression[] params)
        {
            super(exp);
            performMethod = m;
            parameters = params != null ? params : CodeStructure.EMPTY_PARAMS;
        }

        @Override
        public Object getMetaObject() {
            return performMethod;
        }

        @Override
        public CodeExpression[] getStatementParameters() {
            return parameters;
        }
        
        @Override
        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuilder buf = new StringBuilder();

            if (parentStr != null && !parentStr.equals("")) {
                buf.append(parentStr);
                buf.append("."); // NOI18N
            }

            buf.append(performMethod.getName());
            buf.append("("); // NOI18N

            for (int i=0; i < paramsStr.length; i++) {
                buf.append(paramsStr[i]);
                if (i+1 < parameters.length)
                    buf.append(", "); // NOI18N
            }

            buf.append(");"); // NOI18N
            // we do add ; at the end

            return buf.toString();
        }
    }

    static final class FieldStatement extends AbstractCodeStatement {
        private Field assignField;
        private CodeExpression[] parameters;

        public FieldStatement(CodeExpression exp,
                              Field f,
                              CodeExpression assignedExp)
        {
            super(exp);
            assignField = f;
            parameters = new CodeExpression[] { assignedExp };
        }

        @Override
        public Object getMetaObject() {
            return assignField;
        }

        @Override
        public CodeExpression[] getStatementParameters() {
            return parameters;
        }

        @Override
        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuilder buf = new StringBuilder();

            if (parentStr != null && !parentStr.equals("")) {
                buf.append(parentStr);
                buf.append("."); // NOI18N
            }

            buf.append(assignField.getName());
            buf.append(" = "); // NOI18N
            buf.append(paramsStr[0]);
            buf.append(";"); // NOI18N
            // we do add ; at the end

            return buf.toString();
        }
    }

    static final class AssignVariableStatement extends AbstractCodeStatement {
        private CodeVariable variable;

        public AssignVariableStatement(CodeVariable var, CodeExpression exp) {
            super(exp);
            variable = var;
        }

        @Override
        public Object getMetaObject() {
            return parentExpression;
        }

        @Override
        public CodeExpression[] getStatementParameters() {
            return parentExpression.getOrigin().getCreationParameters();
        }

        @Override
        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuilder buf = new StringBuilder();
            int varType = variable.getType();

            int declareMask = CodeVariable.SCOPE_MASK
                              | CodeVariable.DECLARATION_MASK;
            if ((varType & declareMask) == CodeVariable.LOCAL) {
                // no explicit local variable declaration, so we make the
                // declaration together with the assignment
                if ((varType & CodeVariable.FINAL) == CodeVariable.FINAL)
                    buf.append("final "); // NOI18N

                buf.append(getSourceClassName(variable.getDeclaredType()));
                buf.append(" "); // NOI18N
            }

            buf.append(variable.getName());
            buf.append(" = "); // NOI18N
            buf.append(parentExpression.getOrigin().getJavaCodeString(
                                                     parentStr, paramsStr));
            buf.append(";"); // NOI18N

            return buf.toString();
        }
    }

    static final class DeclareVariableStatement extends AbstractCodeStatement {
        private CodeVariable variable;

        public DeclareVariableStatement(CodeVariable var) {
            super(null);
            variable = var;
        }

        @Override
        public Object getMetaObject() {
            return variable;
        }

        @Override
        public CodeExpression[] getStatementParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }

        @Override
        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuilder buf = new StringBuilder();
            int type = variable.getType();

            if ((type & CodeVariable.SCOPE_MASK) == CodeVariable.FIELD) {
                switch (type & CodeVariable.ACCESS_MODIF_MASK) {
                    case CodeVariable.PUBLIC:
                        buf.append("public "); // NOI18N
                        break;
                    case CodeVariable.PRIVATE:
                        buf.append("private "); // NOI18N
                        break;
                    case CodeVariable.PROTECTED:
                        buf.append("protected "); // NOI18N
                        break;
                }

                if ((type & CodeVariable.STATIC) == CodeVariable.STATIC)
                    buf.append("static "); // NOI18N

                if ((type & CodeVariable.FINAL) == CodeVariable.FINAL)
                    buf.append("final "); // NOI18N

                if ((type & CodeVariable.TRANSIENT) == CodeVariable.TRANSIENT)
                    buf.append("transient "); // NOI18N

                if ((type & CodeVariable.VOLATILE) == CodeVariable.VOLATILE)
                    buf.append("volatile "); // NOI18N
            }
            else { // local variable
                if ((type & CodeVariable.FINAL) == CodeVariable.FINAL)
                    buf.append("final "); // NOI18N
            }

            buf.append(getSourceClassName(variable.getDeclaredType()));
            
            String typeParameters = variable.getDeclaredTypeParameters();
            if ((typeParameters != null) && !"".equals(typeParameters)) { // NOI18N
                buf.append(typeParameters);
            }

            buf.append(" "); // NOI18N
            buf.append(variable.getName());
            buf.append(";"); // NOI18N

            return buf.toString();
        }
    }

    // ------------
    // implementation classes of CodeExpressionOrigin interface

    static final class ConstructorOrigin implements CodeExpressionOrigin {
        private Constructor constructor;
        private CodeExpression[] parameters;

        public ConstructorOrigin(Constructor ctor, CodeExpression[] params) {
            constructor = ctor;
            parameters = params != null ? params : CodeStructure.EMPTY_PARAMS;
        }

        @Override
        public Class getType() {
            return constructor.getDeclaringClass();
        }

        @Override
        public CodeExpression getParentExpression() {
            return null;
        }

        @Override
        public Object getMetaObject() {
            return constructor;
        }

        @Override
        public Object getValue() {
            Object[] params = new Object[parameters.length];
            for (int i=0; i < params.length; i++) {
                CodeExpressionOrigin paramOrigin = parameters[i].getOrigin();
                Object value = paramOrigin.getValue();
                Class type = paramOrigin.getType();
                if (value == null && type.isPrimitive())
                    return null;
                params[i] = value;
            }

            try {
                return constructor.newInstance(params);
            }
            catch (Exception ex) {
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                return null;
            }
        }

        @Override
        public CodeExpression[] getCreationParameters() {
            return parameters;
        }

        @Override
        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuilder buf = new StringBuilder();

            buf.append("new "); // NOI18N
            buf.append(constructor.getName());
            buf.append("("); // NOI18N

            for (int i=0; i < paramsStr.length; i++) {
                buf.append(paramsStr[i]);
                if (i+1 < parameters.length)
                    buf.append(", "); // NOI18N
            }

            buf.append(")"); // NOI18N

            return buf.toString();
        }
    }

    static final class MethodOrigin implements CodeExpressionOrigin {
        private CodeExpression parentExpression;
        private Method creationMethod;
        private CodeExpression[] parameters;

        public MethodOrigin(CodeExpression parent,
                            Method m,
                            CodeExpression[] params)
        {
            parentExpression = parent;
            creationMethod = m;
            parameters = params != null ? params : CodeStructure.EMPTY_PARAMS;
        }

        @Override
        public Class getType() {
            return creationMethod.getReturnType();
        }

        @Override
        public CodeExpression getParentExpression() {
            return parentExpression;
        }

        @Override
        public Object getMetaObject() {
            return creationMethod;
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public CodeExpression[] getCreationParameters() {
            return parameters;
        }

        @Override
        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuilder buf = new StringBuilder();

            if (parentExpression != null) {
                if (parentStr != null && !parentStr.equals("")) {
                    buf.append(parentStr);
                    buf.append("."); // NOI18N
                }
            }
            else { // we suppose a static method
                buf.append(getSourceClassName(creationMethod.getDeclaringClass()));
                buf.append("."); // NOI18N
            }

            buf.append(creationMethod.getName());
            buf.append("("); // NOI18N

            for (int i=0; i < paramsStr.length; i++) {
                buf.append(paramsStr[i]);
                if (i+1 < parameters.length)
                    buf.append(", "); // NOI18N
            }

            buf.append(")"); // NOI18N

            return buf.toString();
        }
    }

    static final class FieldOrigin implements CodeExpressionOrigin {
        private CodeExpression parentExpression;
        private Field originField;

        public FieldOrigin(CodeExpression parent, Field f) {
            parentExpression = parent;
            originField = f;
        }

        @Override
        public Class getType() {
            return originField.getType();
        }

        @Override
        public CodeExpression getParentExpression() {
            return parentExpression;
        }

        @Override
        public Object getMetaObject() {
            return originField;
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public CodeExpression[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }

        @Override
        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuilder buf = new StringBuilder();

            if (parentExpression != null) {
                if (parentStr != null && !parentStr.equals("")) {
                    buf.append(parentStr);
                    buf.append("."); // NOI18N
                }
            }
            else { // we suppose a static field
                buf.append(getSourceClassName(originField.getDeclaringClass()));
                buf.append("."); // NOI18N
            }

            buf.append(originField.getName());

            return buf.toString();
        }
    }

    static final class ValueOrigin implements CodeExpressionOrigin {
        private Class expressionType;
        private Object expressionValue;
        private String javaString;

        public ValueOrigin(Class type, Object value, String javaStr) {
            expressionType = type;
            expressionValue = value;
            javaString = javaStr;
        }

        @Override
        public Class getType() {
            return expressionType;
        }

        @Override
        public CodeExpression getParentExpression() {
            return null;
        }

        @Override
        public Object getMetaObject() {
            return null;
        }

        @Override
        public Object getValue() {
            return expressionValue;
        }

        @Override
        public CodeExpression[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }

        @Override
        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            return javaString;
        }
    }

    // --------
    // implementation of CodeGroup interface

    // temporary reduced implementation
    static final class DefaultCodeGroup implements CodeGroup {

        private List<Object/*CodeStatement or CodeGroup*/> statements = new ArrayList<Object>();

        @Override
        public void addStatement(CodeStatement statement) {
            statements.add(statement);
        }

        @Override
        public void addStatement(int index, CodeStatement statement) {
            statements.add(index, statement);
        }

        @Override
        public void addGroup(CodeGroup group) {
            statements.add(group);
        }

        @Override
        public void addGroup(int index, CodeGroup group) {
            statements.add(index, group);
        }

        @Override
        public CodeStatement getStatement(int index) {
            Object obj = statements.get(index);
            if (obj instanceof CodeStatement)
                return (CodeStatement) obj;
            if (obj instanceof CodeGroup)
                return ((CodeGroup)obj).getStatement(0);
            return null;
        }

        @Override
        public int indexOf(Object object) {
            return statements.indexOf(object);
        }

        @Override
        public void remove(Object object) {
            statements.remove(object);
        }

        @Override
        public void remove(int index) {
            statements.remove(index);
        }

        @Override
        public void removeAll() {
            statements.clear();
        }

        @Override
        public Iterator getStatementsIterator() {
            return new StatementsIterator();
        }

        class StatementsIterator implements Iterator {
            int index = 0;
            int count = statements.size();
            Iterator subIter;

            @Override
            public boolean hasNext() {
                if (subIter != null) {
                    if (subIter.hasNext())
                        return true;
                    subIter = null;
                    index++;
                }

                while (index < count) {
                    Object item = statements.get(index);
                    if (item instanceof CodeGroup) {
                        subIter = ((CodeGroup)item).getStatementsIterator();
                        if (subIter.hasNext())
                            return true;
                        subIter = null;
                    }
                    else if (item instanceof CodeStatement)
                        return true; 
                    index++;
                }

                return false;
            }

            @Override
            public Object next() {
                if (!hasNext())
                    throw new NoSuchElementException();

                return subIter != null ? subIter.next() :
                                         statements.get(index++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }
}
