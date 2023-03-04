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

package org.netbeans.modules.beans.beaninfo;

import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.Collection;
import javax.lang.model.element.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.spi.DiffElement;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandler;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandlerFactory;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;

/**
 * The handler allows to refactor {@link java.beans.PropertyEditor property editor}
 * and other custom code.
 * 
 * @author Jan Pokorsky
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.GuardedBlockHandlerFactory.class)
public final class BIGuardedBlockHandlerFactory implements GuardedBlockHandlerFactory {

    public GuardedBlockHandler createInstance(AbstractRefactoring refactoring) {
        return new BIGuardedBlockHandler();
    }
    
    private static final class BIGuardedBlockHandler implements GuardedBlockHandler {
        
        public Problem handleChange(RefactoringElementImplementation proposedChange, Collection<RefactoringElementImplementation> replacements, Collection<Transaction> transaction) {
            if (proposedChange instanceof DiffElement) {
                DiffElement diffel = (DiffElement) proposedChange;
                FileObject fo = diffel.getParentFile();
                if (BIDataLoader.isBeanInfo(fo) && checkChange(fo, diffel.getPosition())) {
                    Difference diff = diffel.getLookup().lookup(Difference.class);
                    if (diff != null) {
                        diffel.setEnabled(true);
                        diff.setCommitToGuards(true);
                        replacements.add(proposedChange);
                    }
                }
            }
            return null;
        }
        
        private boolean checkChange(FileObject bifile, final PositionBounds span) {
            final boolean[] result = new boolean[]{false};
            JavaSource js = JavaSource.forFileObject(bifile);
            if (js != null) {
                try {
                    js.runUserActionTask(new Task<CompilationController>() {

                        public void run(CompilationController javac) throws Exception {
                            javac.toPhase(JavaSource.Phase.RESOLVED);
                            result[0] = checkChange(javac, span);
                        }
                    }, true);
                } catch (IOException ex) {
                    // XXX create Problem?
                    Exceptions.printStackTrace(ex);
                }
            }
            return result[0];
        }

        public void run(CompilationController javac) throws Exception {
            
        }
        
        private boolean checkChange(CompilationController javac, PositionBounds span) throws IOException, BadLocationException {
            final int begin = span.getBegin().getOffset();
            final Trees trees = javac.getTrees();
            TreePath path = javac.getTreeUtilities().pathFor(begin + 1);
            if (path == null) {
                return false;
            }
            
            Element element = trees.getElement(path);
            if (element == null) {
                return false;
            }

            TreePath decl = trees.getPath(element);
            if (decl != null) {
                SourcePositions sourcePositions = trees.getSourcePositions();
                long declBegin = sourcePositions.getStartPosition(decl.getCompilationUnit(), decl.getLeaf());
                FileObject fo = SourceUtils.getFile(element, javac.getClasspathInfo());
                Document doc = javac.getDocument();
                GuardedSectionManager guards = GuardedSectionManager.getInstance((StyledDocument) doc);
                
                if (fo != javac.getFileObject() || guards != null && !isGuarded(guards, doc.createPosition((int) declBegin))) {
                    // tree being refactored is declared outside of this file
                    // or out of guarded sections. It should be safe to make change
                    return true;
                }
            } else {
                // e.g. package; change is OK
                    return true;
            }
            return false;
        }
        
        private boolean isGuarded(GuardedSectionManager guards, Position pos) {
            for (GuardedSection guard : guards.getGuardedSections()) {
                if (guard.contains(pos, true)) {
                    return true;
                }
            }
            return false;
        }
        
    }
    
}
