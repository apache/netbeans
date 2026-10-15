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

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.java.GoToSupport;
import org.netbeans.modules.editor.java.JavaKit;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class GoToSuperTypeAction extends BaseAction {
    
    public GoToSuperTypeAction() {
        super(JavaKit.gotoSuperImplementationAction, SAVE_POSITION | ABBREV_RESET);
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(JavaKit.class, "goto-super-implementation"));
        String name = NbBundle.getMessage(JavaKit.class, "goto-super-implementation-trimmed");
        putValue(ExtKit.TRIMMED_TEXT,name);
        putValue(POPUP_MENU_TEXT, name);
    }
    
    @Override
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        final JavaSource js = JavaSource.forDocument(target.getDocument());
        
        if (js == null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GoToSupport.class, "WARN_CannotGoToGeneric",1));
            return;
        }
        
        final int caretPos = target.getCaretPosition();
        final AtomicBoolean cancel = new AtomicBoolean();
        
        BaseProgressUtils.runOffEventDispatchThread(() -> {
            goToImpl(target, js, caretPos, cancel);
        }, NbBundle.getMessage(JavaKit.class, "goto-super-implementation"), cancel, false);
    }

    private static void goToImpl(final JTextComponent c, final JavaSource js, final int caretPos, final AtomicBoolean cancel) {
       try {
            js.runUserActionTask(controller -> {
                if (cancel != null && cancel.get())
                    return ;
                controller.toPhase(Phase.RESOLVED); //!!!
                
                ExecutableElement ee = resolveMethodElement(controller, caretPos);
                
                if (ee == null) {
                    ee = resolveMethodElement(controller, caretPos + 1);
                }
                
                if (ee == null) {
                    Toolkit.getDefaultToolkit().beep();
                    return ;
                }
                
                final List<ElementDescription> result = new ArrayList<>();
                final AnnotationType type = ComputeOverriding.detectOverrides(controller, (TypeElement) ee.getEnclosingElement(), ee, result);
                
                if (type == null) {
                    Toolkit.getDefaultToolkit().beep();
                    return ;
                }
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        Point p = c.modelToView(c.getCaretPosition()).getLocation();
                        IsOverriddenAnnotationAction.mouseClicked(Map.of(IsOverriddenAnnotationAction.computeCaption(type, ""), result), c, p);
                    } catch (BadLocationException e) {
                        Exceptions.printStackTrace(e);
                    }
                });
            }, true);
            
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    private static ExecutableElement resolveMethodElement(CompilationInfo info, int caret) {
        TreePath path = info.getTreeUtilities().pathFor(caret);

        while (path != null && path.getLeaf().getKind() != Kind.METHOD) {
            path = path.getParentPath();
        }

        if (path == null) {
            return null;
        }

        Element resolved = info.getTrees().getElement(path);

        if (resolved == null || resolved.getKind() != ElementKind.METHOD) {
            return null;
        }

        return (ExecutableElement) resolved;
    }

}
