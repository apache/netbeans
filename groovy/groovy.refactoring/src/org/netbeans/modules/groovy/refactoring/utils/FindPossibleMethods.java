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

package org.netbeans.modules.groovy.refactoring.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.SourceUnit;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Most probably we can use visitor for finding correct MethodNode and we don't
 * need to care about fu*king lazy node initialization !!
 * 
 * @author Martin Janicek
 */
public final class FindPossibleMethods {

    public static Set<MethodNode> findPossibleMethods(FileObject relevantFiles, String fqn, String methodName) {
        final Set<MethodNode> methods = new HashSet<MethodNode>();
        for (FileObject fo : GroovyProjectUtil.getGroovyFilesInProject(relevantFiles)) {
            try {
                FindPossibleTask task = new FindPossibleTask(fqn, methodName);
                ParserManager.parse(Collections.singleton(Source.create(fo)), task);
                methods.addAll(task.getResult());

            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return methods;
    }

    private static class FindPossibleTask extends UserTask {

        private final String fqn;
        private final String methodName;
        private final Set<MethodNode> methods;


        public FindPossibleTask(String fqn, String methodName) {
            this.fqn = fqn;
            this.methodName = methodName;
            this.methods = new HashSet<MethodNode>();
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            final GroovyParserResult result = ASTUtils.getParseResult(resultIterator.getParserResult());
            final ModuleNode moduleNode = result.getRootElement().getModuleNode();

            methods.addAll(new MethodCollector(moduleNode, fqn, methodName).collectMethods());
        }

        public Set<MethodNode> getResult() {
            return methods;
        }
    }

    private static class MethodCollector extends ClassCodeVisitorSupport {

        private final ModuleNode moduleNode;
        private final String fqn;
        private final String methodName;
        private final Set<MethodNode> methods;


        public MethodCollector(ModuleNode moduleNode, String fqn, String methodName) {
            this.moduleNode = moduleNode;
            this.methodName = methodName;
            this.fqn = fqn;

            methods = new HashSet<MethodNode>();
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return moduleNode.getContext();
        }

        public Set<MethodNode> collectMethods() {
            for (ClassNode classNode : moduleNode.getClasses()) {
                visitClass(classNode);
            }
            return methods;
        }

        @Override
        public void visitMethod(MethodNode method) {
            if (methodName.equals(method.getName())) {
                methods.add(method);
            }
            super.visitMethod(method);
        }
    }
}
