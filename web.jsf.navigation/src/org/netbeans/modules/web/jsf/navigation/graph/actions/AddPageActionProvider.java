/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.netbeans.modules.web.jsf.navigation.PageFlowController;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author joelle
 */
public class AddPageActionProvider extends AbstractAction implements ContextAwareAction {

    public AddPageActionProvider() {
        super();
    }
    
    public Action createContextAwareInstance(Lookup lookup) {
        Action addPageAction;
        final PageFlowScene scene = lookup.lookup(PageFlowScene.class);
        if( scene != null) {
            setEnabled(true);
            addPageAction = new AddPageAction(scene);
        } else {
            setEnabled(false);
            addPageAction = null;
        }
        return addPageAction;
    }
  
//    @Override
//    public boolean isEnabled() {
//        return super.isEnabled();
//    }

    
    
    public void actionPerformed(ActionEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private static final String LBL_AddPage =  NbBundle.getMessage(AddPageActionProvider.class, "LBL_AddPage");
    private class AddPageAction extends AbstractAction {
        
        private final PageFlowScene scene;
        
        /** Creates a new instance of OpenPageAction
         * @param scene
         */
        public AddPageAction(PageFlowScene scene) {
            super();
            putValue(NAME, LBL_AddPage);
            this.scene = scene;
        }
        
        /**
         *
         * @return The Display Name of this option.
         */
        protected String getDisplayName() {
            return LBL_AddPage;
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
                final PageFlowController pfc = scene.getPageFlowView().getPageFlowController();
                
                final FileObject webFileObject = pfc.getWebFolder();
                
                String name = FileUtil.findFreeFileName(webFileObject, "page", "jsp");
                name = JOptionPane.showInputDialog("Select Page Name", name);
                
                createIndexJSP(webFileObject, name);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            //            }
        }
        
        private void createIndexJSP(FileObject targetFolder, String name ) throws IOException {
            
            final FileObject jspTemplate = FileUtil.getConfigFile( "Templates/JSP_Servlet/JSP.jsp" ); // NOI18N
            
            if (jspTemplate == null) {
                return; // Don't know the template
            }
            
            final DataObject mt = DataObject.find(jspTemplate);
            final DataFolder webDf = DataFolder.findFolder(targetFolder);
            mt.createFromTemplate(webDf, name); // NOI18N
        }
    }
    
    
}
