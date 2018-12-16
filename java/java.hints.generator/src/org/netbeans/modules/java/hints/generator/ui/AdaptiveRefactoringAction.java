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
package org.netbeans.modules.java.hints.generator.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.java.hints.generator.PatternGenerator;
import org.netbeans.modules.java.hints.generator.PatternGenerator.Result;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.Presenter;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 *
 * @author lahvac
 */
public class AdaptiveRefactoringAction extends BaseAction implements Presenter.Toolbar {

    private static JToggleButton toggleButton;
    private static PatternGenerator generator;

    public static final String adaptiveRefactoringAction = "adaptive-refactoring-action";
    static final long serialVersionUID = 0L;

    public AdaptiveRefactoringAction() {
        super(adaptiveRefactoringAction);
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/java/hints/generator/resources/adaptive_refactoring.png", false)); //NOI18N
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    private Collection<FileObject> allSources() {
        //TODO: should also analyze boot&compile and translate to sources if desired
        return GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)
                                              .stream()
                                              .flatMap(cp -> Stream.of(cp.getRoots()))
                                              .collect(Collectors.toSet());
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (toggleButton.isSelected()) {
            assert generator == null;
            generator = ProgressDialog.showProgress("Recording current state", (progress, cancel) -> {
                try {
                    return PatternGenerator.record(allSources(), progress, cancel);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            });

            if (generator == null) {
                toggleButton.setSelected(false);
            }
        } else {
            assert generator != null;
            Result result = ProgressDialog.showProgress("Inspecting changes", (progress, cancel) -> {
                try {
                    return generator.updated(allSources(), progress, cancel);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            });

            if (result != null) {
                AdaptiveRefactoringTopComponent tc = new AdaptiveRefactoringTopComponent(result);
                Mode outputMode = WindowManager.getDefault().findMode("output");
                if (outputMode != null) {
                    outputMode.dockInto(tc);
                }
                tc.open();
                tc.requestActive();
            }
            generator = null;
        }
    }

    @Override
    public Component getToolbarPresenter() {
        if (toggleButton == null) {
            toggleButton = new JToggleButton();
            toggleButton.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
            toggleButton.setIcon((Icon) getValue(SMALL_ICON));
            toggleButton.setAction(this); // this will make hard ref to button => check GC
        }
        return toggleButton;
    }

}
