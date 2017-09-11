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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.languages.features;

import java.lang.UnsupportedOperationException;
import java.util.Enumeration;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ConcurrentModificationException;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenSequence;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Top component which displays something.
 */
final class TokensBrowserTopComponent extends TopComponent {
    
    private static final String PREFERRED_ID = "TokensBrowserTopComponent";
    private static final long   serialVersionUID = 1L;
    private static TokensBrowserTopComponent instance;
    
    private JTree               tree;
    private Listener            listener;
    private HighlighterSupport  highlighting = new HighlighterSupport (Color.yellow);
    private boolean             listen = true;
    private CaretListener       caretListener;
    private JEditorPane         lastPane;
    private DocumentListener    documentListener;
    private AbstractDocument    lastDocument;
    
    
    private TokensBrowserTopComponent () {
        initComponents ();
        setLayout (new BorderLayout ());
        tree = new JTree ();
        tree.setCellRenderer (new Renderer ());
        tree.addTreeSelectionListener (new TreeSelectionListener () {
            public void valueChanged (TreeSelectionEvent e) {
                if (!listen) return;
                mark ();
            }
        });
        tree.addFocusListener (new FocusListener () {
            public void focusGained (FocusEvent e) {
                mark ();
            }
            public void focusLost (FocusEvent e) {
                mark ();
            }
        });
        tree.setRootVisible (false);
        add (new JScrollPane (tree), BorderLayout.CENTER);
        setName (NbBundle.getMessage (TokensBrowserTopComponent.class, "CTL_TokensBrowserTopComponent"));
        setToolTipText (NbBundle.getMessage (TokensBrowserTopComponent.class, "HINT_TokensBrowserTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized TokensBrowserTopComponent getDefault () {
        if (instance == null) {
            instance = new TokensBrowserTopComponent ();
        }
        return instance;
    }
    
    /**
     * Obtain the TokensBrowserTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized TokensBrowserTopComponent findInstance () {
        TopComponent win = WindowManager.getDefault ().findTopComponent (PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault ().log (ErrorManager.WARNING, "Cannot find TokensBrowser component. It will not be located properly in the window system.");
            return getDefault ();
        }
        if (win instanceof TokensBrowserTopComponent) {
            return (TokensBrowserTopComponent)win;
        }
        ErrorManager.getDefault ().log (ErrorManager.WARNING, "There seem to be multiple components with the '" + PREFERRED_ID + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault ();
    }
    
    public int getPersistenceType () {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    protected void componentShowing () {
        refresh ();
        if (listener == null)
            listener = new Listener (this);
    }

    protected void componentHidden () {
        if (listener != null) {
            listener.remove ();
            listener = null;
        }
        if (lastPane != null)
            lastPane.removeCaretListener (caretListener);
        lastPane = null;
        if (lastDocument != null)
            lastDocument.removeDocumentListener (documentListener);
        lastDocument = null;
        highlighting.removeHighlight ();
    }
    
    /** replaces this in object stream */
    public Object writeReplace () {
        return new ResolvableHelper ();
    }
    
    protected String preferredID () {
        return PREFERRED_ID;
    }
    
    private void mark () {
        Node[] ns = TopComponent.getRegistry ().getActivatedNodes ();
        if (ns.length == 1 && tree.isFocusOwner ()) {
            EditorCookie editorCookie = ns [0].getLookup ().
                lookup (EditorCookie.class);
            if (editorCookie != null) {
                THNode t = (THNode) tree.getLastSelectedPathComponent ();
                if (t == null) return;
                Token token = t.getToken ();
                if (token == null) return;
//                ASTToken stoken = ASTToken.create (
//                    t.getMimeType (),
//                    token.id ().ordinal (), 
//                    token.text ().toString (), 
//                    t.getOffset ()
//                );
                if (t != null) {
                    highlighting.highlight (
                        editorCookie.getDocument (), 
                        t.getOffset (),
                        t.getOffset () + token.length ()
                    );
                    return;
                }
            }
        }
        highlighting.removeHighlight ();
    }
    
    private JEditorPane getCurrentEditor () {
        Node[] ns = TopComponent.getRegistry ().getActivatedNodes ();
        if (ns.length != 1) return null;
        EditorCookie editorCookie = ns [0].getLookup ().
            lookup (EditorCookie.class);
        if (editorCookie == null) return null;
        if (editorCookie.getOpenedPanes () == null) return null;
        if (editorCookie.getOpenedPanes ().length < 1) return null;
        return editorCookie.getOpenedPanes () [0];
    }
    
    private AbstractDocument getCurrentDocument () {
        Node[] ns = TopComponent.getRegistry ().getActivatedNodes ();
        if (ns.length != 1) return null;
        EditorCookie editorCookie = ns [0].getLookup ().
            lookup (EditorCookie.class);
        if (editorCookie == null) return null;
        if (editorCookie.getOpenedPanes () == null) return null;
        if (editorCookie.getOpenedPanes ().length < 1) return null;
        JEditorPane pane = editorCookie.getOpenedPanes () [0];
        
        if (caretListener == null)
            caretListener = new CListener ();
        if (lastPane != null && lastPane != pane) {
            lastPane.removeCaretListener (caretListener);
            lastPane = null;
        }
        if (lastPane == null) {
            pane.addCaretListener (caretListener);
            lastPane = pane;
        }

        AbstractDocument doc = (AbstractDocument) editorCookie.getDocument ();
        if (documentListener == null)
            documentListener = new CDocumentListener ();
        if (lastDocument != null && lastDocument != doc) {
            lastDocument.removeDocumentListener (documentListener);
            lastDocument = null;
        }
        if (lastDocument == null) {
            doc.addDocumentListener (documentListener);
            lastDocument = doc;
        }
        return doc;
    }
    
    private RequestProcessor.Task task;
    
    private void refreshLater () {
        if (task != null) task.cancel ();
        task = RequestProcessor.getDefault ().post (
            new Runnable () {
                public void run () {
                    refresh ();
                    task = null;
                }
            }, 
            1000
        );
    }
    
    private void refresh () {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                AbstractDocument doc = getCurrentDocument ();
                TokenSequence ts = null;
                if (doc != null)
                    try {
                        doc.readLock ();
                        TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
                        if (tokenHierarchy == null) return;
                        ts = tokenHierarchy.tokenSequence ();
                    } finally {
                        doc.readUnlock ();
                    }
                if (ts == null)
                    tree.setModel (new DefaultTreeModel (new DefaultMutableTreeNode ()));
                else
                    tree.setModel (new DefaultTreeModel (new TSNode (null, ts, null, 0, 0)));
                JEditorPane editor = getCurrentEditor ();
                if (editor != null) {
                    int position = getCurrentEditor ().getCaret ().getDot ();
                    selectPath (position);
                }
            }
        });
    }
    
    private void selectPath (int offset) {
        Object root = tree.getModel ().getRoot ();
        if (!(root instanceof TSNode)) return;
        listen = false;
        TSNode n = (TSNode) root;
        TreePath path = new TreePath (n);
        path = findPath (path, offset);
        tree.setSelectionPath (path);
        tree.scrollPathToVisible (path);
        listen = true;
    }
    
    private TreePath findPath (TreePath path, int offset) {
        THNode parent = (THNode) path.getLastPathComponent ();
        Enumeration en = parent.children ();
        while (en.hasMoreElements ()) {
            THNode n = (THNode) en.nextElement ();
            if (n.getOffset () + n.getToken ().length () > offset) {
                if (offset < n.getOffset ()) 
                    return path;
                if (n.isLeaf ())
                    return new MPath (path, n);
                return findPath (new MPath (path, n), offset);
            }
        }
        return path;
    }
    
    
    // innerclasses ............................................................
    
    static interface THNode extends TreeNode {
        Token getToken ();
        String getMimeType ();
        int getOffset ();
        int getIndex ();
    }
    
    static class MPath extends TreePath {
        MPath (TreePath path, Object e) {
            super (path, e);
        }
    }
    
    static class TSNode implements THNode {
        
        private TSNode          parent;
        private TokenSequence   ts;
        private Token           token;
        private int             offset;
        private int             index;
        
        TSNode (TSNode parent, TokenSequence ts, Token token, int offset, int index) {
            this.parent = parent;
            this.ts = ts;
            this.token = token;
            this.offset = offset;
            this.index = index;
        }

        public TreeNode getChildAt (int index) {
            ts.moveIndex (index);
            ts.moveNext ();
            TokenSequence ts2 = ts.embedded ();
            if (ts2 != null)
                return new TSNode (this, ts2, ts.token (), ts.offset (), ts.index ());
            return new TNode (this, ts.token (), getMimeType (), index, ts.offset ());
        }

        public int getChildCount () {
            return ts.tokenCount ();
        }

        public TreeNode getParent () {
            return parent;
        }
        
        public String getMimeType () {
            return ts.language ().mimeType ();
        }

        public int getIndex (TreeNode node) {
            return ((THNode) node).getIndex ();
        }

        public boolean getAllowsChildren () {
            return true;
        }

        public boolean isLeaf () {
            return false;
        }

        public Enumeration children () {
            return new Enumeration() {
                private int i = 0;
                
                public boolean hasMoreElements () {
                    return i < getChildCount ();
                }

                public Object nextElement() {
                    return getChildAt (i++);
                }
            };
        }
        
        public Token getToken () {
            return token;
        }
        
        public int getOffset () {
            return offset;
        }
        
        public int getIndex () {
            return index;
        }
    }
    
    static class TNode implements THNode {
        
        private TSNode          parent;
        private Token           token;
        private String          mimeType;
        private int             index;
        private int             offset;
        
        TNode (TSNode parent, Token token, String mimeType, int index, int offset) {
            this.parent = parent;
            this.token = token;
            this.mimeType = mimeType;
            this.index = index;
            this.offset = offset;
        }

        public TreeNode getChildAt (int index) {
            throw new UnsupportedOperationException ();
        }

        public int getChildCount () {
            throw new UnsupportedOperationException ();
        }

        public TreeNode getParent() {
            return parent;
        }

        public int getIndex (TreeNode node) {
            throw new UnsupportedOperationException ();
        }

        public boolean getAllowsChildren () {
            return false;
        }

        public boolean isLeaf () {
            return true;
        }

        public Enumeration children () {
            throw new UnsupportedOperationException ();
        }
        
        public Token getToken () {
            return token;
        }
        
        public String getMimeType () {
            return mimeType;
        }
        
        public int getOffset () {
            return offset;
        }
        
        public int getIndex () {
            return index;
        }
    }
    
    class CDocumentListener implements DocumentListener {
        public void insertUpdate (DocumentEvent e) {
            refreshLater ();
        }

        public void removeUpdate (DocumentEvent e) {
            refreshLater ();
        }

        public void changedUpdate (DocumentEvent e) {
            refreshLater ();
        }
    }
    
    class CListener implements CaretListener {
        public void caretUpdate (CaretEvent e) {
            int position = e.getDot ();
            try {
                selectPath (position);
            } catch (ConcurrentModificationException ex) {
            }
        }
    }

    private static class Renderer extends DefaultTreeCellRenderer {
        
        private String e (CharSequence t) {
            if (t == null) return "null";
            StringBuilder sb = new StringBuilder ();
            int i, k = t.length ();
            for (i = 0; i < k; i++) {
                if (t.charAt (i) == '\t')
                    sb.append ("\\t");
                else
                if (t.charAt (i) == '\r')
                    sb.append ("\\r");
                else
                if (t.charAt (i) == '\n')
                    sb.append ("\\n");
                else
                    sb.append (t.charAt (i));
            }
            return sb.toString ();
        }
        
        public Component getTreeCellRendererComponent (
            JTree       tree, 
            Object      value,
            boolean     sel,
            boolean     expanded,
            boolean     leaf, 
            int         row,
            boolean     hasFocus
        ) {
            if (!(value instanceof THNode))
                return super.getTreeCellRendererComponent (
                    tree, value, sel, expanded, leaf, row, hasFocus
                );
            THNode node = (THNode) value;
            Token token = node.getToken ();
            if (token == null)
                return super.getTreeCellRendererComponent (
                    tree, value, sel, expanded, leaf, row, hasFocus
                );
            StringBuilder sb = new StringBuilder ().
                append ('<').
                append (node.getOffset ()).
                append (",\"").
                append (token.id ().name ()).
                append (",\"").
                append (e (token.text ())).
                append ("\">");
            return super.getTreeCellRendererComponent (
                tree, sb.toString (), sel, expanded, leaf, row, hasFocus
            );
        }
    }
    
    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve () {
            return TokensBrowserTopComponent.getDefault ();
        }
    }
    
    private static class Listener implements PropertyChangeListener {
        
        private WeakReference component;
        
        
        Listener (TokensBrowserTopComponent c) {
            component = new WeakReference (c);
            TopComponent.getRegistry ().addPropertyChangeListener (this);
        }

        TokensBrowserTopComponent getComponent () {
            TokensBrowserTopComponent c = (TokensBrowserTopComponent) component.get ();
            if (c != null) return c;
            remove ();
            return null;
        }
        
        void remove () {
            TopComponent.getRegistry ().removePropertyChangeListener (this);
        }
        
        public void propertyChange (PropertyChangeEvent evt) {
            TokensBrowserTopComponent c = getComponent ();
            if (c == null) return;
            c.refresh ();
        }
    }
}
