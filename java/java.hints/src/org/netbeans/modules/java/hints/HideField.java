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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
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
import javax.lang.model.util.Elements;
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
    
    @Override
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
        Elements elements = compilationInfo.getElements();
        for (Element field : getAllMembers(compilationInfo, te)) {
            if (stop) {
                return null;
            }
            if (elements.hides(el, field)) {
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
        List<Fix> fixes = List.of(new HideFieldFix(
                (span[1] + span[0]) / 2,
                compilationInfo.getFileObject(),
                false
        ));


        bounds[0] = span[0];
        bounds[1] = span[1];
        return fixes;
    }
    
    @Override
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

        return List.of(ed);
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(DoubleCheck.class, "MSG_HiddenField"); // NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(DoubleCheck.class, "HINT_HiddenField"); // NOI18N
    }

    @Override
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
        var map = (Map<TypeElement, Iterable<? extends Element>>) info.getCachedValue(KEY_MEMBERS_CACHE);

        if (map == null) {
            map = new HashMap<>();
            info.putCachedValue(KEY_MEMBERS_CACHE, map, CacheClearPolicy.ON_SIGNATURE_CHANGE);
        }

        return map.computeIfAbsent(clazz, k -> info.getElements().getAllMembers(clazz));
    }

    record HideFieldFix(int caret, FileObject file, boolean hideFieldByVariable) implements Fix, Runnable {
        
        @Override
        public String getText() {
            return hideFieldByVariable ? NbBundle.getMessage(DoubleCheck.class, "MSG_FixHiddenByVariableFiledText") // NOI18N
                                       : NbBundle.getMessage(DoubleCheck.class, "MSG_FixHiddenFiledText"); // NOI18N
        }
        
        @Override
        public ChangeInfo implement() throws IOException {
            SwingUtilities.invokeLater(this);
            return null;
        }
        
        @Override
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
                
    }
    
}
