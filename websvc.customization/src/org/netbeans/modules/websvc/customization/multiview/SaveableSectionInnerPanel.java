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
package org.netbeans.modules.websvc.customization.multiview;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;

/**
 *
 * @author rico
 */
public abstract class SaveableSectionInnerPanel extends SectionInnerPanel {

    private EnterKeyListener listener;

    /** Creates a new instance of SaveableSectionInnerPanel */
    public SaveableSectionInnerPanel(SectionView view) {
        super(view);
    }

    protected boolean isClient(Node node) {
        Client client = node.getLookup().lookup(Client.class);
        if (client != null) {
            return true;
        }
        JaxWsService service = node.getLookup().lookup(JaxWsService.class);
        if (service != null && !service.isServiceProvider()) {
            return true;
        }
        return false;
    }

    protected void setModelDirty(WSDLModel model) {
        try {
            ModelSource ms = model.getModelSource();
            FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
            DataObject wsdlDO = DataObject.find(fo);
            if (!wsdlDO.isModified()) {
                wsdlDO.setModified(true);
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    protected void disableEnterKey() {
        Component[] components = this.getComponents();
        listener = new EnterKeyListener();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (component.isFocusable() && !(component instanceof JLabel)) {
                KeyListener kl = (KeyListener) WeakListeners.create(KeyListener.class, listener,
                        component);
                component.addKeyListener(kl);
            }
        }
    }

    private static class EnterKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                e.consume();
            }
        }
    }

    /**
     * Perform anything here other than saving the wsdl
     */
    public abstract void save();

    /**
     * Does the jaxws model need to be saved?
     */
    public boolean jaxwsIsDirty() {
        return false;
    }

    /**
     * Has the wsdl been changed?
     */
    public abstract boolean wsdlIsDirty();
}
