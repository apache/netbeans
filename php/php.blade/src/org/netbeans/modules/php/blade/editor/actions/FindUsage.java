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
package org.netbeans.modules.php.blade.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.php.blade.editor.path.BladePathUtils;
import org.netbeans.modules.php.blade.editor.refactoring.BladePathInfo;
import org.netbeans.modules.php.blade.editor.refactoring.WhereBladePathUsedRefactoringUIImpl;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 *
 * @author bogdan
 */
@ActionID(id = "org.netbeans.modules.php.blade.editor.actions.FindUsage", category = "TemplateActions")
@ActionRegistration(displayName = "Template Usages")
public class FindUsage extends AbstractAction implements ActionListener {

    Node node;

    public FindUsage(Node node) {
        this.node = node;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FileObject fo = node.getLookup().lookup(FileObject.class);
        String bladePath = BladePathUtils.toBladeViewPath(fo);
        if (bladePath == null) {
            return;
        }
        BladePathInfo si = new BladePathInfo(fo, bladePath);
        UI.openRefactoringUI(new WhereBladePathUsedRefactoringUIImpl(si),
                TopComponent.getRegistry().getActivated());
    }

}
