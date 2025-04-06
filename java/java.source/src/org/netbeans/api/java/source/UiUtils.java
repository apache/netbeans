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
package org.netbeans.api.java.source;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.tools.javac.util.Context;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Icon;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.java.source.pretty.VeryPretty;
import org.netbeans.modules.java.source.save.DiffContext;
import org.netbeans.modules.java.ui.Icons;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.text.NbDocument;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;

/** This class contains various methods bound to visualization of Java model 
 * elements. It was formerly included under SourceUtils
 *
 * XXX - needs cleanup
 *
 * @author Jan Lahoda
 * @deprecated Replaced by various classes in the org.netbeans.modules.java.sourceui module.
 */
@Deprecated
public final class  UiUtils {

    private UiUtils() {}
    
    /** Gets correct icon for given ElementKind.
     * @param modifiers Can be null for empty modifiers collection
     * @deprecated Use <a href="@org-netbeans-modules-java-sourceui@/org/netbeans/api/java/source/ui/ElementIcons.html#getElementIcon(javax.lang.model.element.ElementKind,java.util.Collection)">ElementIcons#getElementIcon(javax.lang.model.element.ElementKind, java.util.Collection)</a>
     *             of the <a href="@org-netbeans-modules-java-sourceui@">org.netbeans.modules.java.sourceui module</a>
     */
    @Deprecated
    public static Icon getElementIcon( ElementKind elementKind, Collection<Modifier> modifiers ) {
        return Icons.getElementIcon(elementKind, modifiers);
    }

    /**
     *
     * @deprecated Use <a href="@org-netbeans-modules-java-sourceui@/org/netbeans/api/java/source/ui/ElementIcons.html#getElementIcon(javax.lang.model.element.ElementKind,java.util.Collection)">ElementIcons#getElementIcon(javax.lang.model.element.ElementKind, java.util.Collection)</a>
     *             of the <a href="@org-netbeans-modules-java-sourceui@">org.netbeans.modules.java.sourceui module</a>
     */
    @Deprecated
    public static Icon getDeclarationIcon(Element element) {
        return getElementIcon(element.getKind(), element.getModifiers());
    }
    
    
    /**
     * Opens given {@link Element}.
     * 
     * @param cpInfo fileobject whose {@link ClasspathInfo} will be used
     * @param el    declaration to open
     * @return true if and only if the declaration was correctly opened,
     *                false otherwise
     * @deprecated Use <a href="@org-netbeans-modules-java-sourceui@/org/netbeans/api/java/source/ui/ElementOpen.html#open(org.netbeans.api.java.source.ClasspathInfo,javax.lang.model.element.Element)">org.netbeans.api.java.source.ui.ElementOpen#open(org.netbeans.api.java.source.ClasspathInfo, org.netbeans.api.java.source.ElementHandle</a>
     *             of the <a href="@org-netbeans-modules-java-sourceui@">org.netbeans.modules.java.sourceui module</a>
     */
    @Deprecated
    public static boolean open(final ClasspathInfo cpInfo, final Element el) {
	Object[] openInfo = getOpenInfo (cpInfo, el);
	if (openInfo != null) {
	    assert openInfo[0] instanceof FileObject;
	    assert openInfo[1] instanceof Integer;
	    return doOpen((FileObject)openInfo[0],(Integer)openInfo[1]);
	}
	return false;
    }

    /**
     *
     * @deprecated Use <a href="@org-netbeans-modules-java-sourceui@/org/netbeans/api/java/source/ui/ElementOpen.html#open(org.openide.filesystems.FileObject,org.netbeans.api.java.source.ElementHandle)">org.netbeans.api.java.source.ui.ElementOpen#open(org.openide.filesystems.FileObject, org.netbeans.api.java.source.ElementHandle)</a>
     *             of the <a href="@org-netbeans-modules-java-sourceui@">org.netbeans.modules.java.sourceui module</a>
     */
    @Deprecated
    public static boolean open(final FileObject toSearch, final ElementHandle<? extends Element> toOpen) {
        if (toSearch == null || toOpen == null) {
            throw new IllegalArgumentException("null not supported");
        }
        
        Object[] openInfo = getOpenInfo (toSearch, toOpen);
        if (openInfo != null) {
            assert openInfo[0] instanceof FileObject;
            assert openInfo[1] instanceof Integer;
            return doOpen((FileObject)openInfo[0],(Integer)openInfo[1]);
        }
        return false;
    }
    
    private static String getMethodHeader(MethodTree tree, CompilationInfo info, String s) {
        Context context = info.impl.getJavacTask().getContext();
        VeryPretty veryPretty = new VeryPretty(new DiffContext(info));
        return veryPretty.getMethodHeader(tree, s);
    }

    private static String getClassHeader(ClassTree tree, CompilationInfo info, String s) {
        Context context = info.impl.getJavacTask().getContext();
        VeryPretty veryPretty = new VeryPretty(new DiffContext(info));
        return veryPretty.getClassHeader(tree, s);
    }
    private static String getVariableHeader(VariableTree tree, CompilationInfo info, String s) {
        Context context = info.impl.getJavacTask().getContext();
        VeryPretty veryPretty = new VeryPretty(new DiffContext(info));
        return veryPretty.getVariableHeader(tree, s);
    }

    /**
     *
     * @deprecated Use constants from <a href="@org-netbeans-modules-java-sourceui@/org/netbeans/api/java/source/ui/ElementHeaders.html">org.netbeans.api.java.source.ui.ElementHeaders</a>
     *             of the <a href="@org-netbeans-modules-java-sourceui@">org.netbeans.modules.java.sourceui module</a>
     */
    @Deprecated
    public static final class PrintPart {
        private PrintPart() {}
        public static final String ANNOTATIONS = VeryPretty.ANNOTATIONS;
        public static final String NAME = VeryPretty.NAME;
        public static final String TYPE = VeryPretty.TYPE;
        public static final String THROWS = VeryPretty.THROWS;
        public static final String IMPLEMENTS = VeryPretty.IMPLEMENTS;
        public static final String EXTENDS = VeryPretty.EXTENDS;
        public static final String TYPEPARAMETERS = VeryPretty.TYPEPARAMETERS;
        public static final String FLAGS = VeryPretty.FLAGS;
        public static final String PARAMETERS = VeryPretty.PARAMETERS;
    }
    
    /**
     * example of formatString:
     * "method " + PrintPart.NAME + PrintPart.PARAMETERS + " has return type " + PrintPart.TYPE
     * @deprecated Use <a href="@org-netbeans-modules-java-sourceui@/org/netbeans/api/java/source/ui/ElementHeaders.html#getHeader(com.sun.source.util.TreePath,org.netbeans.api.java.source.CompilationInfo,java.lang.String)">org.netbeans.api.java.source.ui.ElementHeaders#getHeader(com.sun.source.util.TreePath, org.netbeans.api.java.source.CompilationInfo, java.lang.String)</a>
     *             of the <a href="@org-netbeans-modules-java-sourceui@">org.netbeans.modules.java.sourceui module</a>
     */
    @Deprecated
    public static String getHeader(TreePath treePath, CompilationInfo info, String formatString) {
        assert info != null;
        assert treePath != null;
        Element element = info.getTrees().getElement(treePath);
        if (element!=null)
            return getHeader(element, info, formatString);
        return null;
    }

    /**
     * example of formatString:
     * "method " + PrintPart.NAME + PrintPart.PARAMETERS + " has return type " + PrintPart.TYPE
     * @deprecated Use <a href="@org-netbeans-modules-java-sourceui@/org/netbeans/api/java/source/ui/ElementHeaders.html#getHeader(javax.lang.model.element.Element,org.netbeans.api.java.source.CompilationInfo,java.lang.String)">org.netbeans.api.java.source.ui.ElementHeaders#getHeader(javax.lang.model.element.Element, org.netbeans.api.java.source.CompilationInfo, java.lang.String)</a>
     *             of the <a href="@org-netbeans-modules-java-sourceui@">org.netbeans.modules.java.sourceui module</a>
     */
    @Deprecated
    public static String getHeader(Element element, CompilationInfo info, String formatString) {
        assert element != null;
        assert info != null;
        assert formatString != null;
        Tree tree = info.getTrees().getTree(element);
        if (tree != null) {
            if (tree.getKind() == Tree.Kind.METHOD) {
                return getMethodHeader((MethodTree) tree, info, formatString);
            } else if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
                return getClassHeader((ClassTree)tree, info, formatString);
            } else if (tree.getKind() == Tree.Kind.VARIABLE) {
                return getVariableHeader((VariableTree)tree, info, formatString);
            }
        }
        return formatString.replaceAll(PrintPart.NAME, element.getSimpleName().toString()).replaceAll("%[a-z]*%", ""); //NOI18N
    }
    
    /**
     * Opens given {@link Element}.
     * 
     * @param fo fileobject whose {@link ClasspathInfo} will be used
     * @param offset  offset with fileobject
     * @return true if and only if the declaration was correctly opened,
     *                false otherwise
     */
    public @Deprecated static boolean open(final FileObject fo, final int offset) {
        return doOpen(fo, offset);
    }
    
    static Object[] getOpenInfo (final ClasspathInfo cpInfo, final Element el) {
        FileObject fo = SourceUtils.getFile(el, cpInfo);
        if (fo != null) {
            return getOpenInfo(fo, ElementHandle.create(el));
        } else {
            return null;
        }
    }

    private static Logger log = Logger.getLogger(UiUtils.class.getName());
    static Object[] getOpenInfo(final FileObject fo, final ElementHandle<? extends Element> handle) {
        assert fo != null;
        
        try {
            int offset = getOffset(fo, handle);
            return new Object[] {fo, offset};
        } catch (IOException e) {
            if (log.isLoggable(Level.SEVERE))
                log.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }
    
    /** Computes distance between strings
     * @deprecated Use <a href="@org-netbeans-modules-java-sourceui@/org/netbeans/api/java/source/ui/ElementHeaders.html#getDistance(java.lang.String,java.lang.String)">org.netbeans.api.java.source.ui.ElementHeaders#getDistance(java.lang.String, java.lang.String)</a>
     *             of the <a href="@org-netbeans-modules-java-sourceui@">org.netbeans.modules.java.sourceui module</a>
     */
    @Deprecated
    public static int getDistance(String s, String t) {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        // Step 1

        n = s.length ();
        m = t.length ();
        if (n == 0) {
          return m;
        }
        if (m == 0) {
          return n;
        }
        d = new int[n+1][m+1];

        // Step 2

        for (i = 0; i <= n; i++) {
          d[i][0] = i;
        }

        for (j = 0; j <= m; j++) {
          d[0][j] = j;
        }

        // Step 3

        for (i = 1; i <= n; i++) {

          s_i = s.charAt (i - 1);

          // Step 4

          for (j = 1; j <= m; j++) {

            t_j = t.charAt (j - 1);

            // Step 5

            if (s_i == t_j) {
              cost = 0;
            }
            else {
              cost = 1;
            }

            // Step 6
            d[i][j] = min (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);

          }

        }

        // Step 7

        return d[n][m];        
    }
  
    private static int min (int a, int b, int c) {
        int mi;
               
        mi = a;
        if (b < mi) {
          mi = b;
        }
        if (c < mi) {
          mi = c;
        }
        return mi;

   }
    
    // Private methods ---------------------------------------------------------
                    
    private static boolean doOpen(FileObject fo, int offset) {
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = od.getLookup().lookup(EditorCookie.class);
            LineCookie lc = od.getLookup().lookup(LineCookie.class);
            
            if (ec != null && lc != null && offset != -1) {
                StyledDocument doc = null;
                try {
                    doc = ec.openDocument();
                } catch (UserQuestionException uqe) {
                    final Object value = DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Confirmation(uqe.getLocalizedMessage(),
                            NbBundle.getMessage(UiUtils.class, "TXT_Question"),
                            NotifyDescriptor.YES_NO_OPTION));
                    if (value != NotifyDescriptor.YES_OPTION) {
                        return false;
                    }
                    uqe.confirmed();
                    doc = ec.openDocument();
                }
                if (doc != null) {
                    int line = NbDocument.findLineNumber(doc, offset);
                    int lineOffset = NbDocument.findLineOffset(doc, line);
                    int column = offset - lineOffset;
                    
                    if (line != -1) {
                        Line l = lc.getLineSet().getCurrent(line);
                        
                        if (l != null) {
                            doShow( l, column);
                            return true;
                        }
                    }
                }
            }
            
            OpenCookie oc = od.getLookup().lookup(OpenCookie.class);
            
            if (oc != null) {
                doOpen(oc);
                return true;
            }
        } catch (IOException e) {
            if (log.isLoggable(Level.INFO))
                log.log(Level.INFO, e.getMessage(), e);
        }
        
        return false;
    }
    
    private static void doShow(final Line l, final int column) {
        Mutex.EVENT.readAccess(() ->
            l.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column)
        );
    }

    private static void doOpen(final OpenCookie oc) {
        Mutex.EVENT.readAccess(oc::open);
    }
    
    private static int getOffset(FileObject fo, final ElementHandle<? extends Element> handle) throws IOException {
        assert handle != null;
        final int[]  result = new int[] {-1};
        
        
        JavaSource js = JavaSource.forFileObject(fo);
        js.runUserActionTask(new Task<CompilationController>() {
                        
            public void run(CompilationController info) {
                try {
                    info.toPhase(JavaSource.Phase.RESOLVED);
                } catch (IOException ioe) {
                    if (log.isLoggable(Level.SEVERE))
                        log.log(Level.SEVERE, ioe.getMessage(), ioe);
                }
                Element el = handle.resolve(info);                
                if (el == null)
                    throw new IllegalArgumentException();
                
                FindDeclarationVisitor v = new FindDeclarationVisitor(el, info);
                
                CompilationUnitTree cu = info.getCompilationUnit();

                v.scan(cu, null);                
                Tree elTree = v.declTree;
                
                if (elTree != null)
                    result[0] = (int)info.getTrees().getSourcePositions().getStartPosition(cu, elTree);
            }
        },true);
        return result[0];
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private static class FindDeclarationVisitor extends ErrorAwareTreePathScanner<Void, Void> {
        
        private Element element;
        private Tree declTree;
        private CompilationInfo info;
        
        public FindDeclarationVisitor(Element element, CompilationInfo info) {
            this.element = element;
            this.info = info;
        }
        
	@Override
        public Void visitClass(ClassTree tree, Void d) {
            handleDeclaration();
            super.visitClass(tree, d);
            return null;
        }
        
	@Override
        public Void visitMethod(MethodTree tree, Void d) {
            handleDeclaration();
            super.visitMethod(tree, d);
            return null;
        }
        
	@Override
        public Void visitVariable(VariableTree tree, Void d) {
            handleDeclaration();
            super.visitVariable(tree, d);
            return null;
        }
    
        public void handleDeclaration() {
            Element found = info.getTrees().getElement(getCurrentPath());
            
            if ( element.equals( found ) ) {
                declTree = getCurrentPath().getLeaf();
            }
        }
    
    }
    
}
