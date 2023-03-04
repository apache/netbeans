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

package org.netbeans.modules.spellchecker.bindings.java;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
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
        
        private static final class ScannerImpl extends ErrorAwareTreeScanner<Void, List<Position[]>> {
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
