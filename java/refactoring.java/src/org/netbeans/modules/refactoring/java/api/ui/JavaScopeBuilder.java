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
package org.netbeans.modules.refactoring.java.api.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.java.ui.scope.CustomScopePanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;

/**
 * Support for creating a new Scope. Opens a dialog to select
 * different parts of open projects to include in the new scope.
 * 
 * @author Ralph Ruijs
 * @since 1.27.0
 */
public final class JavaScopeBuilder {

    /**
     * Open a modal dialog to specify a new Scope.
     * 
     * @param title - the title of the dialog
     * @param scope - the scope to use to preselect parts
     * @return a new Scope or null if the dialog was canceled
     */
    public static Scope open(String title, final Scope scope) {
        final CustomScopePanel panel = new CustomScopePanel();
        final AtomicBoolean successful = new AtomicBoolean();

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                panel,
                title,
                true, // modal
                new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.CANCEL_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == DialogDescriptor.OK_OPTION) {
                    successful.set(true);
                }
            }
        });

        dialogDescriptor.setClosingOptions(new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION});

        new Thread(new Runnable() {

            @Override
            public void run() {
                panel.initialize(scope);
            }
        }).start();
        
        DialogDisplayer.getDefault().createDialog(dialogDescriptor).setVisible(true);
        
        if(successful.get()) {
            return panel.getCustomScope();
        }
        return null;
    }
}
