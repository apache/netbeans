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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.modules.java.editor.rename.InstantRenamePerformer;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav tulach
 */
public class HideField extends AbstractHint {

            static final String  KEY_WARN_HIDDEN_STATIC_FIELDS = "warn-hidden-static-fields";
    private static final boolean DEFAULT_WARN_HIDDEN_STATIC_FIELDS = true;

    transient volatile boolean stop;
    
    public HideField() {
        this("FieldNameHidesFieldInSuperclass");
    }

    public HideField(String... sw) {
        super( true, false, AbstractHint.HintSeverity.WARNING, sw);
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.VARIABLE);
    }

    protected List<Fix> computeFixes(CompilationInfo compilationInfo, TreePath treePath, int[] bounds) {
        stop = false;
        
        Element el = compilationInfo.getTrees().getElement(treePath);
        if (el == null || el.getKind() != ElementKind.FIELD) {
            return null;
        }
        if (el.getSimpleName().contentEquals("<error>")) { //NOI18N
            return null;
        }
        
        Element hidden = null;
        TypeElement te = (TypeElement)el.getEnclosingElement();
        for (Element field : getAllMembers(compilationInfo, te)) {
            if (stop) {
                return null;
            }
            if (compilationInfo.getElements().hides(el, field)) {
                hidden = field;
                break;
            }
        }
        if (hidden == null) {
            return null;
        }

        if (   !getPreferences(null).getBoolean(KEY_WARN_HIDDEN_STATIC_FIELDS, DEFAULT_WARN_HIDDEN_STATIC_FIELDS)
            && hidden.getModifiers().contains(Modifier.STATIC)) {
            return null;
        }
        
        int[] span = compilationInfo.getTreeUtilities().findNameSpan((VariableTree) treePath.getLeaf());
        if (span == null) {
            return null;
        }
        List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(
                (span[1] + span[0]) / 2,
                compilationInfo.getFileObject(),
                false
        ));


        bounds[0] = span[0];
        bounds[1] = span[1];
        return fixes;
    }
    
    public List<ErrorDescription> run(CompilationInfo compilationInfo,
                                      TreePath treePath) {
        int[] span = new int[2];
        List<Fix> fixes = computeFixes(compilationInfo, treePath, span);
        if (fixes == null) {
            return null;
        }

        ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(
            getSeverity().toEditorSeverity(),
            getDisplayName(),
            fixes,
            compilationInfo.getFileObject(),
            span[0],
            span[1]
        );

        return Collections.singletonList(ed);
    }

    public String getId() {
        return getClass().getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(DoubleCheck.class, "MSG_HiddenField"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(DoubleCheck.class, "HINT_HiddenField"); // NOI18N
    }

    public void cancel() {
        stop = true;
    }
    
    @Override
    public JComponent getCustomizer(Preferences node) {
        return new OneCheckboxCustomizer(node, NbBundle.getMessage(HideField.class, "LBL_WarnHiddenStaticFields"),
                                               NbBundle.getMessage(HideField.class, "TP_WarnHiddenStaticFields"),
                                               KEY_WARN_HIDDEN_STATIC_FIELDS,
                                               DEFAULT_WARN_HIDDEN_STATIC_FIELDS);
    }

    private static final Object KEY_MEMBERS_CACHE = new Object();
    protected static synchronized Iterable<? extends Element> getAllMembers(CompilationInfo info, TypeElement clazz) {
        Map<TypeElement, Iterable<? extends Element>> map = (Map<TypeElement, Iterable<? extends Element>>) info.getCachedValue(KEY_MEMBERS_CACHE);

        if (map == null) {
            info.putCachedValue(KEY_MEMBERS_CACHE, map = new HashMap<TypeElement, Iterable<? extends Element>>(), CacheClearPolicy.ON_SIGNATURE_CHANGE);
        }

        Iterable<? extends Element> members = map.get(clazz);

        if (members == null) {
            map.put(clazz, members = info.getElements().getAllMembers(clazz));
        }

        return members;
    }

    static class FixImpl implements Fix, Runnable {
        private final int caret;
        private final FileObject file;
        private final boolean hideFieldByVariable;
        
        public FixImpl(int caret, FileObject file, boolean hideFieldByVariable) {
            this.caret = caret;
            this.file = file;
            this.hideFieldByVariable = hideFieldByVariable;
        }
        
        
        public String getText() {
            return hideFieldByVariable ? NbBundle.getMessage(DoubleCheck.class, "MSG_FixHiddenByVariableFiledText") : NbBundle.getMessage(DoubleCheck.class, "MSG_FixHiddenFiledText"); // NOI18N
        }
        
        public ChangeInfo implement() throws IOException {
            SwingUtilities.invokeLater(this);
            return null;
        }
        
        public void run() {
            try {
                EditorCookie cook = DataObject.find(file).getLookup().lookup(EditorCookie.class);
                if (cook == null) {
                    return;
                }
                JEditorPane[] arr = cook.getOpenedPanes();
                if (arr == null) {
                    return;
                }
                arr[0].setCaretPosition(caret);
                InstantRenamePerformer.invokeInstantRename(arr[0]);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            
        }
        
        @Override public String toString() {
            return "FixHideField"; // NOI18N
        }

        public void cancel() {
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FixImpl other = (FixImpl) obj;
            if (this.caret != other.caret) {
                return false;
            }
            if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
                return false;
            }
            if (this.hideFieldByVariable != other.hideFieldByVariable) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + this.caret;
            hash = 17 * hash + (this.file != null ? this.file.hashCode() : 0);
            hash = 17 * hash + (this.hideFieldByVariable ? 1 : 0);
            return hash;
        }

        
    }
    
}
