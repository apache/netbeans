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

package org.netbeans.modules.groovy.refactoring.findusages.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.groovy.refactoring.findusages.FindUsagesElement;
import org.netbeans.modules.groovy.refactoring.findusages.model.ClassRefactoringElement;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Base class for a various type of find usage strategies.
 *
 * It might be implemented in a different ways (e.g. we are looking only for
 * subclasses and we are not interested in other usages etc.)
 *
 * @author Martin Janicek
 */
public abstract class AbstractFindUsages {

    protected final RefactoringElement element;
    protected final List<FindUsagesElement> usages;

    
    protected AbstractFindUsages(RefactoringElement element) {
        this.element = element;
        this.usages = new ArrayList<FindUsagesElement>();
    }

    protected abstract List<AbstractFindUsagesVisitor> getVisitors(ModuleNode moduleNode, String defClass);

    protected List<AbstractFindUsagesVisitor> singleVisitor(AbstractFindUsagesVisitor visitor) {
        return Collections.singletonList(visitor);
    }

    /**
     * Collects find usages for a given <code>FileObject</code>
     * @param fo file where we are looking for usages
     */
    public final void findUsages(FileObject fo) {
        try {
            ParserManager.parse(Collections.singleton(Source.create(fo)), new AddFindUsagesElementsTask(fo));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public final void clear() {
        usages.clear();
    }

    public final List<FindUsagesElement> getResults() {
        return usages;
    }

    
    protected final class AddFindUsagesElementsTask extends UserTask {

        private final FileObject fo;
        private final String defClass;

        /**
         * Creates find usages task for a specific <code>FileObject</code>
         *
         * @param fo file where we are looking for usages
         * @param defClass fully qualified name of the class for which we are
         * trying to find usages
         */
        public AddFindUsagesElementsTask(FileObject fo) {
            this.fo = fo;
            this.defClass = element.getOwnerName();
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            final GroovyParserResult result = ASTUtils.getParseResult(resultIterator.getParserResult());
            final ModuleNode moduleNode = result.getRootElement().getModuleNode();
            final BaseDocument doc = GroovyProjectUtil.getDocument(result, fo);
            
            for (AbstractFindUsagesVisitor visitor : getVisitors(moduleNode, defClass)) {
                for (ASTNode node : visitor.findUsages()) {
                    if (node.getLineNumber() != -1 && node.getColumnNumber() != -1) {
                        usages.add(new FindUsagesElement(new ClassRefactoringElement(fo, node), doc));
                    }
                }
            }
            Collections.sort(usages);
        }
    }
}
