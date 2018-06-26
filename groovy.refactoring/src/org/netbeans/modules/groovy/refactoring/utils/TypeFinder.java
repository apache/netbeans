/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
