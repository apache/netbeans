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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.spellchecker.bindings.java;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class JavaSemanticTokenList implements TokenList {

    private List<Position[]> identifiers;
    private Document doc;

    public JavaSemanticTokenList() {
    }

    public synchronized void set(List<Position[]> identifiers, Document doc) {
        this.identifiers = identifiers;
        this.doc = doc;
        
        pcs.fireChange();
    }
    
    private int currentIndex;
    
    public synchronized void setStartOffset(int offset) {
        if (identifiers == null) return ;
        //XXX: binary search:
        currentIndex = 0;
        
        while (currentIndex < identifiers.size() && identifiers.get(currentIndex)[0].getOffset() <= offset) {
            currentIndex++;
        }
        
        if (currentIndex < identifiers.size()) {
            currentIndex--;
        }
    }

    public synchronized boolean nextWord() {
        if (identifiers == null) return false;
        //XXX: will ignore the first identifier:
        return ++currentIndex < identifiers.size();
    }

    public synchronized int getCurrentWordStartOffset() {
        int result = identifiers.get(currentIndex)[0].getOffset();
        
        return result;
    }

    public synchronized CharSequence getCurrentWordText() {
        try {
            String result = doc.getText(identifiers.get(currentIndex)[0].getOffset(), identifiers.get(currentIndex)[1].getOffset() - identifiers.get(currentIndex)[0].getOffset());
            
            return result;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }
    }

    private ChangeSupport pcs = new ChangeSupport(this);
    
    public void addChangeListener(ChangeListener l) {
        pcs.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        pcs.removeChangeListener(l);
    }
    
    private static Map<FileObject, JavaSemanticTokenList>  js2TokenList = new WeakHashMap<FileObject, JavaSemanticTokenList>();
    
    static synchronized JavaSemanticTokenList get(FileObject file) {
        if (!JavaTokenListProvider.ENABLE_SEMANTIC_TOKEN_LIST) {
            return null;
        }
        
        JavaSource js = JavaSource.forFileObject(file);
        
        if (js == null) {
            return null;
        }
        
        JavaSemanticTokenList l = js2TokenList.get(file);
        
        if (l == null) {
            js2TokenList.put(file, l = new JavaSemanticTokenList());
        }
        
        return l;
    }
    
    public static final class TaskImpl implements CancellableTask<CompilationInfo> {

        public void cancel() {
        }

        public void run(CompilationInfo parameter) throws Exception {
            JavaSemanticTokenList l = get(parameter.getFileObject());
            
            if (l == null) {
                return ;
            }

            Document doc = parameter.getDocument();
            
            if (doc == null) {
                return ;
            }
            
            ScannerImpl si = new ScannerImpl(doc, parameter);
            List<Position[]> pos = new ArrayList<Position[]>();
            
            si.scan(parameter.getCompilationUnit(), pos);
            
            l.set(pos, parameter.getDocument());
        }
        
        private static final class ScannerImpl extends TreeScanner<Void, List<Position[]>> {
            private Document doc;
            private CompilationInfo info;

            public ScannerImpl(Document doc, CompilationInfo info) {
                this.doc = doc;
                this.info = info;
            }
            
            @Override
            public Void visitClass(ClassTree node, List<Position[]> p) {
                int[] span = info.getTreeUtilities().findNameSpan(node);

                handleIdentifier(doc, p, span, node.getSimpleName().toString());
                
                return super.visitClass(node, p);
            }
            
            @Override
            public Void visitMethod(MethodTree node, List<Position[]> p) {
                int[] span = info.getTreeUtilities().findNameSpan(node);

                handleIdentifier(doc, p, span, node.getName().toString());
                
                return super.visitMethod(node, p);
            }
            
            @Override
            public Void visitVariable(VariableTree node, List<Position[]> p) {
                int[] span = info.getTreeUtilities().findNameSpan(node);

                handleIdentifier(doc, p, span, node.getName().toString());
                
                return super.visitVariable(node, p);
            }
            
        }
        
    }
    
    static List<String> separateWords(String ident, List<int[]> subWordsSpans) {
        int currentWordStart = 0;
        int index = 0;
        boolean wasCapital = false;
        List<String> result = new LinkedList<String>();
        
        for (char c : ident.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (!wasCapital) {
                    if (index - currentWordStart > 0) {
                        result.add(ident.substring(currentWordStart, index));
                        subWordsSpans.add(new int[] {currentWordStart, index});
                        currentWordStart = index;
                    }
                }
                
                wasCapital = true;
            } else {
                wasCapital = false;
            }
            
            index++;
        }
        
        if (index - currentWordStart > 0) {
            result.add(ident.substring(currentWordStart, index));
            subWordsSpans.add(new int[]{currentWordStart, index});
        }
        
        return result;
    }
    
    static void handleIdentifier(Document doc, List<Position[]> p, int[] span, String name) {
        if (span == null)
            return ;
        
        List<int[]> spans = new LinkedList<int[]>();
        List<String> separatedWords = separateWords(name, spans);
        
        for (int[] s : spans) {
            try {
                p.add(new Position[]{doc.createPosition(span[0] + s[0]), doc.createPosition(span[0] + s[1])});
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.api.java.source.JavaSourceTaskFactory.class)
    public static final class FactoryImpl extends EditorAwareJavaSourceTaskFactory {

        public FactoryImpl() {
            super(Phase.PARSED, Priority.LOW, TaskIndexingMode.ALLOWED_DURING_SCAN);
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new TaskImpl();
        }
        
    }

}
