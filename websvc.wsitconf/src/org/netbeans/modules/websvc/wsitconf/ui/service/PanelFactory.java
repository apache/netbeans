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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.ui.service;

import javax.swing.undo.UndoManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class PanelFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    
    private ToolBarDesignEditor editor;
    private Node node;
    private UndoManager undoManager;
    private Project project;
    private JaxWsModel jaxwsmodel;
    
    /**
     * Creates a new instance of PanelFactory
     */
    PanelFactory(ToolBarDesignEditor editor, Node node, UndoManager undoManager, Project p, JaxWsModel jxwsmodel) {
        this.editor=editor;
        this.node = node;
        this.project = p;
        this.jaxwsmodel = jxwsmodel;
        this.undoManager = undoManager;
    }

    public SectionInnerPanel createInnerPanel(Object key) {
        if (key instanceof Binding) {
            Binding b = (Binding)key;
            return new BindingPanel((SectionView) editor.getContentView(), node, project, b, undoManager, jaxwsmodel);
        }
        if (key instanceof BindingOperation) {
            BindingOperation o = (BindingOperation)key;
            return new OperationPanel((SectionView) editor.getContentView(), node, project, o);
        }
        if (key instanceof BindingInput) {
            BindingInput i = (BindingInput)key;
            return new InputPanel((SectionView) editor.getContentView(), i, undoManager);
        }
        if ((key instanceof BindingOutput) || (key instanceof BindingFault)) {
            return new GenericElementPanel((SectionView) editor.getContentView(),(WSDLComponent) key, undoManager);
        }
        return null;
    }
}
