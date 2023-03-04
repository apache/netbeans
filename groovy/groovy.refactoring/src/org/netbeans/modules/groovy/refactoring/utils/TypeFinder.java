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
 *
 * @author Martin Janicek
 */
public final class TypeFinder {

    private TypeFinder() {
    }

    /**
     * Goes through the whole project file base and return a set of {@link ClassNode}s
     * that has the same name as the given argument.
     *
     * NOTE: Please be aware that this method is currently highly ineffective since it
     * doesn't use index into account and parse the whole source base. It should be improve
     * once we will create a general API for both AST and Indexed elements.
     *
     * @param fileInProject one file from the project
     * @param className class name we are looking for
     * @return set of {@link ClassNode}s with the same name as the given argument
     */
    public static Set<ClassNode> findTypes(FileObject fileInProject, String className) {
        final Set<ClassNode> types = new HashSet<ClassNode>();
        for (FileObject fo : GroovyProjectUtil.getGroovyFilesInProject(fileInProject)) {
            try {
                FindPossibleTypesTask task = new FindPossibleTypesTask(className);
                ParserManager.parse(Collections.singleton(Source.create(fo)), task);
                types.addAll(task.getResult());

            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return types;
    }

    private static class FindPossibleTypesTask extends UserTask {

        private final String className;
        private final Set<ClassNode> types;


        public FindPossibleTypesTask(String className) {
            this.className = className;
            this.types = new HashSet<ClassNode>();
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            final GroovyParserResult result = ASTUtils.getParseResult(resultIterator.getParserResult());
            final ModuleNode moduleNode = result.getRootElement().getModuleNode();

            types.addAll(new TypesCollector(moduleNode, className).collectTypes());
        }

        public Set<ClassNode> getResult() {
            return types;
        }
    }

    private static class TypesCollector extends ClassCodeVisitorSupport {

        private final ModuleNode moduleNode;
        private final String className;
        private final Set<ClassNode> types;


        public TypesCollector(ModuleNode moduleNode, String className) {
            this.moduleNode = moduleNode;
            this.className = className;

            types = new HashSet<ClassNode>();
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return moduleNode.getContext();
        }

        public Set<ClassNode> collectTypes() {
            for (ClassNode classNode : moduleNode.getClasses()) {
                visitClass(classNode);
            }
            return types;
        }

        @Override
        public void visitClass(ClassNode classNode) {
            if (className.equals(classNode.getNameWithoutPackage())) {
                types.add(classNode);
            }
            super.visitClass(classNode);
        }
    }
}
