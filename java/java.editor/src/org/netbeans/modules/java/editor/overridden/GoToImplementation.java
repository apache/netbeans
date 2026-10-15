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
package org.netbeans.modules.java.editor.overridden;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.java.GoToSupport;
import org.netbeans.modules.editor.java.GoToSupport.Context;
import org.netbeans.modules.editor.java.JavaKit;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

@EditorActionRegistration(
        name = "goto-implementation",
        mimeType = JavaKit.JAVA_MIME_TYPE,
        popupText = "#CTL_GoToImplementation"
)
public final class GoToImplementation extends BaseAction {

    public GoToImplementation() {
        super(SAVE_POSITION | ABBREV_RESET);
//        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(GoToImplementation.class, "CTL_GoToImplementation"));
//        String name = NbBundle.getMessage(GoToImplementation.class, "CTL_GoToImplementation_trimmed");
//        putValue(ExtKit.TRIMMED_TEXT,name);
//        putValue(POPUP_MENU_TEXT, name);
    }

    @Override
    public void actionPerformed(ActionEvent e, final JTextComponent c) {
        goToImplementation(c);
    }
    
    public static void goToImplementation(final JTextComponent c) {
        final Document doc = c.getDocument();
        final int caretPos = c.getCaretPosition();
        final AtomicBoolean cancel = new AtomicBoolean();
        
        BaseProgressUtils.runOffEventDispatchThread(() -> {
            goToImpl(c, doc, caretPos, cancel);
        }, NbBundle.getMessage(GoToImplementation.class, "CTL_GoToImplementation"), cancel, false);
    }

    public static void goToImpl(final JTextComponent c, final Document doc, final int caretPos, final AtomicBoolean cancel) {
        try {
            JavaSource js = JavaSource.forDocument(doc);
            if (js != null) {
                js.runUserActionTask(controller -> {
                    if (cancel != null && cancel.get())
                        return ;
                    controller.toPhase(Phase.RESOLVED);
                    
                    AtomicBoolean onDeclaration = new AtomicBoolean();
                    Element el = resolveTarget(controller, doc, caretPos, onDeclaration);
                    
                    if (el == null) {
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GoToImplementation.class, "LBL_NoMethod"));
                        return ;
                    }
                    
                    TypeElement type = el.getKind() == ElementKind.METHOD ? (TypeElement) el.getEnclosingElement() : (TypeElement) el;
                    final ExecutableElement method = el.getKind() == ElementKind.METHOD ? (ExecutableElement) el : null;
                    
                    Map<ElementHandle<? extends Element>, List<ElementDescription>> overriding = new ComputeOverriders(new AtomicBoolean()).process(controller, type, method, true);
                    
                    List<ElementDescription> overridingMethods = overriding != null ? overriding.get(ElementHandle.create(el)) : null;
                    if (!onDeclaration.get()) {
                        ElementDescription ed = new ElementDescription(controller, el, true);
                        if (overridingMethods == null) {
                            overridingMethods = List.of(ed);
                        } else {
                            overridingMethods.add(ed);
                        }
                    }
                    
                    if (overridingMethods == null || overridingMethods.isEmpty()) {
                        String key = el.getKind() == ElementKind.METHOD ? "LBL_NoOverridingMethod" : "LBL_NoOverridingType";
                        
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GoToImplementation.class, key));
                        return;
                    }
                    
                    final List<ElementDescription> finalOverridingMethods = overridingMethods;
                    final String caption = NbBundle.getMessage(GoToImplementation.class, method != null ? "LBL_ImplementorsOverridersMethod" : "LBL_ImplementorsOverridersClass");
                    
                    SwingUtilities.invokeLater(() -> {
                        try {
                            Rectangle view = c.modelToView(caretPos);
                            if (view != null) {
                                Point p = new Point(view.getLocation());
                                IsOverriddenAnnotationAction.mouseClicked(Map.of(caption, finalOverridingMethods), c, p);
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    });
                }, true);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    static Element resolveTarget(CompilationInfo info, Document doc, int caretPos, AtomicBoolean onDeclaration) {
        Context context = GoToSupport.resolveContext(info, doc, caretPos, false, false);

        TreePath tp = info.getTreeUtilities().pathFor(caretPos);

        if (tp.getLeaf().getKind() == Kind.MODIFIERS) {
            tp = tp.getParentPath();
        }

        int[] elementNameSpan = null;
        switch (tp.getLeaf().getKind()) {
            case ANNOTATION_TYPE, CLASS, ENUM, INTERFACE, RECORD -> {
                elementNameSpan = info.getTreeUtilities().findNameSpan((ClassTree) tp.getLeaf());
                onDeclaration.set(true);
            }
            case METHOD -> {
                elementNameSpan = info.getTreeUtilities().findNameSpan((MethodTree) tp.getLeaf());
                onDeclaration.set(true);
            }
        }
        if (context == null) {
            if (elementNameSpan != null && caretPos <= elementNameSpan[1]) {
                return info.getTrees().getElement(tp);
            }
            return null;
        } else if (!SUPPORTED_ELEMENTS.contains(context.resolved.getKind())) {
            return null;
        } else {
            return context.resolved;
        }
    }

    private static final Set<ElementKind> SUPPORTED_ELEMENTS = EnumSet.of(
            ElementKind.METHOD, ElementKind.ANNOTATION_TYPE, ElementKind.CLASS, ElementKind.ENUM, ElementKind.INTERFACE, ElementKind.RECORD);
    
}
