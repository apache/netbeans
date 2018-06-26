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

/*
 * ExternalBindingPanel.java
 *
 * Created on March 7, 2006, 11:21 PM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Binding;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author  Roderico Cruz
 */
public class ExternalBindingPanel extends SaveableSectionInnerPanel {
    private ExternalBindingTablePanel panel;
    private Node node;
    private boolean jaxwsIsDirty;
    
    /** Creates new form ExternalBindingPanel */
    public ExternalBindingPanel(SectionView sectionView, Node node) {
        super(sectionView);
        this.node = node;
        
        ExternalBindingTablePanel.EBTableModel model = new ExternalBindingTablePanel.EBTableModel();
        panel = new ExternalBindingTablePanel(model, node);
        panel.populateModel();
        initComponents2();
    }
    
    public JComponent getErrorComponent(String errorId) {
        return null;
    }
    
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public void setValue(JComponent source, Object value) {
        
    }
    
    private void initComponents2() {
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
                );
    }// </editor-fold>
    
    public boolean wsdlIsDirty() {
        return false;
    }
    
    @Override
    public boolean jaxwsIsDirty(){
        return jaxwsIsDirty;
    }
    
    public void save() {
        FileObject srcRoot = (FileObject)node.getLookup().lookup(FileObject.class);
        FileObject bindingsFolder = null;
        if(isClient()){
            JAXWSClientSupport support = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
            bindingsFolder = support.getBindingsFolderForClient(node.getName(), true);
            
        } else{
            JAXWSSupport support = JAXWSSupport.getJAXWSSupport(srcRoot);
            bindingsFolder = support.getBindingsFolderForService(node.getName(), true);
        }
        assert srcRoot != null : "Cannot find srcRoot";
        
        Client client = (Client)node.getLookup().lookup(Client.class);
        Service service = (Service)node.getLookup().lookup(Service.class);
        Map<String, FileObject> addedBindings = panel.getAddedBindings();
        Set<String> removedBindings = panel.getRemovedBindings();
        
        if(addedBindings.size() > 0 || removedBindings.size() > 0){
            jaxwsIsDirty = true;
        }
        
        //add new binding files
        for(Map.Entry<String, FileObject> bindingEntrySet: addedBindings.entrySet()){
            String bindingName = bindingEntrySet.getKey();
            FileObject bindingFO = bindingEntrySet.getValue();
            if(bindingFO != null){
                String normalizedBindingName = bindingName;
                String ext = bindingFO.getExt();
                if(!ext.equals("")){
                    int index = normalizedBindingName.indexOf(ext);
                    normalizedBindingName = normalizedBindingName.substring(0, index - 1);
                }
                try{
                    FileObject copiedBinding = FileUtil.copyFile(bindingFO, bindingsFolder,
                            normalizedBindingName, bindingFO.getExt());
                    
                    DataObject dobj = DataObject.find(copiedBinding);
                    String relativePath = panel.getRelativePathToWsdl();
                    boolean changed = org.netbeans.modules.websvc.core.JaxWsUtils.addRelativeWsdlLocation(copiedBinding, relativePath);
                    if(changed){
                        if(dobj != null){
                            SaveCookie sc = (SaveCookie)dobj.getCookie(SaveCookie.class);
                            if(sc != null){
                                sc.save();
                            }
                        }
                    }
                    if(dobj != null){
                        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
                        ec.open();
                    }
                }catch(IOException e){
                    ErrorManager.getDefault().notify(e);
                }
                
                
                
            }
            if(client != null){
                Binding binding = client.newBinding();
                binding.setFileName(bindingName);
                //binding.setOriginalFileUrl(bindingFileUri.toString());
                client.addBinding(binding);
            } else{
                if(service != null){
                    Binding binding = service.newBinding();
                    binding.setFileName(bindingName);
                    //binding.setOriginalFileUrl(bindingFileUri.toString());
                    service.addBinding(binding);
                }
            }
        }
        //remove deleted bindings from the metadata file
        //TODO Shd we also delete the binding file from the bindings directory?
        
        for(String removedBinding : removedBindings){
            if(client != null){
                Binding binding = client.getBindingByFileName(removedBinding);
                if(binding != null){
                    client.removeBinding(binding);
                }
            } else if(service != null){
                Binding binding = service.getBindingByFileName(removedBinding);
                if(binding != null){
                    service.removeBinding(binding);
                }
            }
        }
        
    }
    
    private boolean isClient(){
        Client client = (Client)node.getLookup().lookup(Client.class);
        if(client != null){
            return true;
        }
        return false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
