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
package org.netbeans.modules.cnd.mixeddev.java;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationController;
import static org.netbeans.modules.cnd.mixeddev.java.JavaContextSupport.*;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaEntityInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaFieldInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaMethodInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaParameterInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaTypeInfo;

/**
 *
 */
public class ResolveJavaEntityTask extends AbstractResolveJavaContextTask<JavaEntityInfo> {
    
    public ResolveJavaEntityTask(int offset) {
        super(offset);
    }

    @Override
    protected void resolve(CompilationController controller, TreePath tp) {
        if (JavaContextSupport.isClassOrInterface(tp)) {
            result = createClassInfo(controller, tp);
        } else if (JavaContextSupport.isMethod(tp)) {
            result = validateMethodInfo(createMethodInfo(controller, tp));
        } else if (JavaContextSupport.isField(tp)) {
            result = validateFieldInfo(createFieldInfo(controller, tp));
        }
    }
    
    private JavaMethodInfo validateMethodInfo(JavaMethodInfo mtdInfo) {
        for (JavaParameterInfo param : mtdInfo.getParameters()) {
            if (param == null || param.getName() == null) {
                return null;
            }
        }
        return mtdInfo;
    }
    
    private JavaFieldInfo validateFieldInfo(JavaFieldInfo fieldInfo) {
        if (fieldInfo.getName() == null) {
            return null;
        }
        if (fieldInfo.getType() == null) {
            return null;
        }
        return fieldInfo;
    }
}