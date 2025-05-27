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

package org.netbeans.modules.javadoc.hints;

import com.sun.source.doctree.DocCommentTree;
import static org.netbeans.modules.javadoc.hints.JavadocUtilities.*;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.text.Document;
import org.netbeans.api.java.queries.AccessibilityQuery;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.CustomizerProvider;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * A hint provider checking missing and broken javadocs.
 * 
 * @author Jan Pokorsky
 * @author Ralph Benjamin Ruijs
 */
public final class JavadocHint {

    public static final String SCOPE_KEY = "scope";             // NOI18N
    public static final String SCOPE_DEFAULT = "protected"; // NOI18N
    public static final String AVAILABILITY_KEY = "availability"; // NOI18N

    @Hint(id = "create-javadoc", category = "JavaDoc", description = "#DESC_CREATE_JAVADOC_HINT", displayName = "#DN_CREATE_JAVADOC_HINT", hintKind = Hint.Kind.INSPECTION, severity = Severity.HINT, customizerProvider = JavadocHint.CustomizerProviderImplCreate.class)
    @TriggerTreeKind({Kind.METHOD, Kind.ANNOTATION_TYPE, Kind.CLASS, Kind.ENUM, Kind.INTERFACE, Kind.VARIABLE})
    public static List<ErrorDescription> createHint(HintContext ctx) {
        Preferences pref = ctx.getPreferences();
        boolean createJavadocForNonPublic = pref.getBoolean(AVAILABILITY_KEY + true, false);

        CompilationInfo javac = ctx.getInfo();
        Boolean publiclyAccessible = AccessibilityQuery.isPubliclyAccessible(javac.getFileObject().getParent());
        boolean isPubliclyA11e = publiclyAccessible == null ? true : publiclyAccessible;

        if (!isPubliclyA11e && !createJavadocForNonPublic) {
            return null;
        }

        if (javac.getElements().getTypeElement("java.lang.Object") == null) { // NOI18N
            // broken java platform
            return Collections.<ErrorDescription>emptyList();
        }

        TreePath currentPath = ctx.getPath();
        Severity severity = ctx.getSeverity();
        Access access = Access.resolve(pref.get(SCOPE_KEY, SCOPE_DEFAULT));
        
        List<ErrorDescription> errors = Collections.<ErrorDescription>emptyList();
        int caretLocation = ctx.isBulkMode()? -1 : CaretAwareJavaSourceTaskFactory.getLastPosition(ctx.getInfo().getFileObject());
        Tree node = currentPath.getLeaf();
        if (javac.getTreeUtilities().isSynthetic(currentPath) || !isValid(javac, currentPath, severity, access, caretLocation)) {
            return errors;
        }
        // check javadoc
        Element elm = javac.getTrees().getElement(currentPath);

        if (elm == null) {
            Logger.getLogger(Analyzer.class.getName()).log(
                    Level.INFO, "Cannot resolve element for {0} in {1}", new Object[]{node, javac.getFileObject()}); // NOI18N
            return errors;
        }
        {
            Document doc = null;
            try {
                doc = javac.getDocument();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            if (doc != null && isGuarded(node, javac, doc)) {
                return errors;
            }
        }
        DocCommentTree docCommentTree = ((DocTrees) javac.getTrees()).getDocCommentTree(currentPath);
        // create hint descriptor + prepare javadoc
        if (docCommentTree == null ||
                (docCommentTree.getFirstSentence().isEmpty() &&
                 docCommentTree.getBody().isEmpty() &&
                 docCommentTree.getBlockTags().isEmpty() &&
                 elm.getKind() != ElementKind.ENUM_CONSTANT &&
                 elm.getKind() != ElementKind.FIELD)) {
            if (hasErrors(node) || JavadocUtilities.hasInheritedDoc(javac, elm)) {
                return errors;
            }
            int[] positions = createSignaturePositions(node, javac);
            if (positions == null) {
                return errors;
            }

            TreePathHandle handle = TreePathHandle.create(currentPath, javac);

            String description;
            if (elm.getKind() == ElementKind.CONSTRUCTOR) {
                description = elm.getEnclosingElement().getSimpleName().toString();
            } else {
                description = elm.getSimpleName().toString();
            }

            errors = new ArrayList<>();
            GenerateJavadocFix javadocFix = new GenerateJavadocFix(description, handle, ctx.getInfo().getSourceVersion());
            errors.add(ErrorDescriptionFactory.forSpan(ctx, positions[0], positions[1], NbBundle.getMessage(Analyzer.class, "MISSING_JAVADOC_DESC"), javadocFix.toEditorFix()));
        }
        return errors;
    }

    @Hint(id = "error-in-javadoc", category = "JavaDoc", description = "#DESC_ERROR_IN_JAVADOC_HINT", displayName = "#DN_ERROR_IN_JAVADOC_HINT", hintKind = Hint.Kind.INSPECTION, severity = Severity.WARNING, customizerProvider = JavadocHint.CustomizerProviderImplError.class)
    @TriggerTreeKind({Kind.METHOD, Kind.ANNOTATION_TYPE, Kind.CLASS, Kind.ENUM, Kind.INTERFACE, Kind.VARIABLE})
    public static List<ErrorDescription> errorHint(final HintContext ctx) {
        Preferences pref = ctx.getPreferences();
        boolean correctJavadocForNonPublic = pref.getBoolean(AVAILABILITY_KEY + false, false);

        CompilationInfo javac = ctx.getInfo();
        Boolean publiclyAccessible = AccessibilityQuery.isPubliclyAccessible(javac.getFileObject().getParent());
        boolean isPubliclyA11e = publiclyAccessible == null ? true : publiclyAccessible;

        if (!isPubliclyA11e && !correctJavadocForNonPublic) {
            return null;
        }

        if (javac.getElements().getTypeElement("java.lang.Object") == null) { // NOI18N
            // broken java platform
            return Collections.<ErrorDescription>emptyList();
        }

        TreePath path = ctx.getPath();
        {
            Document doc = null;
            try {
                doc = javac.getDocument();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            if (doc != null && isGuarded(path.getLeaf(), javac, doc)) {
                return null;
            }
        }
        
        Access access = Access.resolve(pref.get(SCOPE_KEY, SCOPE_DEFAULT));
        Analyzer a = new Analyzer(javac, path, access, ctx);
        return a.analyze();
    }

    private static JComponent createCustomizer(final Preferences node, final boolean createJavadocKind) {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setOpaque(false);

        JPanel res = new JPanel(new GridBagLayout());
        res.setOpaque(false);
        res.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(JavadocHint.class, "LBL_SCOPE"))); //NOI18N
        ActionListener l = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JRadioButton rb = (JRadioButton) e.getSource();
                node.put(SCOPE_KEY, rb.getText());
            }
        };
        ButtonGroup group = new ButtonGroup();

        int row = 0;
        JRadioButton radio = new JRadioButton();
        Mnemonics.setLocalizedText(radio, NbBundle.getMessage(JavadocHint.class, "CTL_PUBLIC_OPTION")); // NOI18N
        radio.setToolTipText(NbBundle.getMessage(JavadocHint.class, "HINT_PUBLIC_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavadocHint.class, "ACD_PUBLIC_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleName(NbBundle.getMessage(JavadocHint.class, "ACN_PUBLIC_OPTION")); // NOI18N
        radio.addActionListener(l);
        group.add(radio);
        radio.setSelected(radio.getText().equals(node.get(SCOPE_KEY, SCOPE_DEFAULT)));
        if(radio.isSelected()) {
            node.put(SCOPE_KEY, radio.getText());
        }
        radio.setOpaque(false);
        res.add(radio, new GridBagConstraints(0, row++, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 8, 0, 8), 0, 0));

        radio = new JRadioButton();
        Mnemonics.setLocalizedText(radio, NbBundle.getMessage(JavadocHint.class, "CTL_PROTECTED_OPTION")); // NOI18N
        radio.setToolTipText(NbBundle.getMessage(JavadocHint.class, "HINT_PROTECTED_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavadocHint.class, "ACD_PROTECTED_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleName(NbBundle.getMessage(JavadocHint.class, "ACN_PROTECTED_OPTION")); // NOI18N
        radio.addActionListener(l);
        group.add(radio);
        radio.setSelected(radio.getText().equals(node.get(SCOPE_KEY, SCOPE_DEFAULT)));
        if(radio.isSelected()) {
            node.put(SCOPE_KEY, radio.getText());
        }
        radio.setOpaque(false);
        res.add(radio, new GridBagConstraints(0, row++, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 8, 0, 8), 0, 0));

        radio = new JRadioButton();
        Mnemonics.setLocalizedText(radio, NbBundle.getMessage(JavadocHint.class, "CTL_PACKAGE_OPTION")); // NOI18N
        radio.setToolTipText(NbBundle.getMessage(JavadocHint.class, "HINT_PACKAGE_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavadocHint.class, "ACD_PACKAGE_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleName(NbBundle.getMessage(JavadocHint.class, "ACN_PACKAGE_OPTION")); // NOI18N
        radio.addActionListener(l);
        group.add(radio);
        radio.setSelected(radio.getText().equals(node.get(SCOPE_KEY, SCOPE_DEFAULT)));
        if(radio.isSelected()) {
            node.put(SCOPE_KEY, radio.getText());
        }
        radio.setOpaque(false);
        res.add(radio, new GridBagConstraints(0, row++, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 8, 0, 8), 0, 0));

        radio = new JRadioButton();
        Mnemonics.setLocalizedText(radio, NbBundle.getMessage(JavadocHint.class, "CTL_PRIVATE_OPTION")); // NOI18N
        radio.setToolTipText(NbBundle.getMessage(JavadocHint.class, "HINT_PRIVATE_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavadocHint.class, "ACD_PRIVATE_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleName(NbBundle.getMessage(JavadocHint.class, "ACN_PRIVATE_OPTION")); // NOI18N
        radio.addActionListener(l);
        group.add(radio);
        radio.setSelected(radio.getText().equals(node.get(SCOPE_KEY, SCOPE_DEFAULT)));
        if(radio.isSelected()) {
            node.put(SCOPE_KEY, radio.getText());
        }
        radio.setOpaque(false);
        res.add(radio, new GridBagConstraints(0, row++, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 8, 0, 8), 0, 0));

        outerPanel.add(res, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        JCheckBox apiCheckbox = new JCheckBox();
        apiCheckbox.setText(NbBundle.getMessage(JavadocHint.class, "CTL_APICHECKBOX"));
        apiCheckbox.setSelected(node.getBoolean(AVAILABILITY_KEY + createJavadocKind, false));
        apiCheckbox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(JavadocHint.class, "AN_APICHECKBOX")); // NOI18N
        apiCheckbox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavadocHint.class, "AD_APICHECKBOX")); // NOI18N

        apiCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                node.putBoolean(AVAILABILITY_KEY + createJavadocKind, cb.isSelected());
            }
        });
        outerPanel.add(apiCheckbox, new GridBagConstraints(0, 2, 1, 2, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        node.putBoolean(AVAILABILITY_KEY + createJavadocKind, apiCheckbox.isSelected());
        return outerPanel;
    }

    public static final class CustomizerProviderImplCreate implements CustomizerProvider {

        @Override
        public JComponent getCustomizer(Preferences prefs) {
            return createCustomizer(prefs, true);
        }
    }

    public static final class CustomizerProviderImplError implements CustomizerProvider {

        @Override
        public JComponent getCustomizer(Preferences prefs) {
            return createCustomizer(prefs, false);
        }
    }
}
