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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.jaxb.ui;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEventListener;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEventListenerAdapter;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schemas;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.nodes.Node;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;

/**
 *
 * @author gpatil
 */
@NodeFactory.Registration(projectType={"org-netbeans-modules-java-j2seproject"} ,position=275)
public class JAXBNodeFactory implements NodeFactory {
    public static final String JAXB_NODE_NAME = "JAXB Bindings" ; // NOI18N

    @NodeFactory.Registration(projectType="org-netbeans-modules-web-project",position=425)
    public static JAXBNodeFactory webproject() {
        return new JAXBNodeFactory();
    }

    @NodeFactory.Registration(projectType="org-netbeans-modules-j2ee-ejbjarproject",position=175)
    public static JAXBNodeFactory ejbproject() {
        return new JAXBNodeFactory();
    }

    public JAXBNodeFactory() {
    }
    
    public synchronized NodeList<String> createNodes(Project project) {
        return new JAXBRootNodeList(project); 
    }
    
    private class JAXBRootNodeList  implements NodeList<String> {
        private Project project;
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private JAXBWizEventListener modelListener = new ModelListener();
        
        private List<String> rootKeys = null;
        
        public JAXBRootNodeList(Project prj){
            this.project = prj;
            rootKeys = new ArrayList<String>();
            ProjectHelper.addModelListener(prj, modelListener);
            updateKeys();
        }
        
        private synchronized void updateKeys(){
            Schemas scs = ProjectHelper.getXMLBindingSchemas(project);        
            updateKeys(scs);
        }

        private synchronized void updateKeys(Schemas scs){
            rootKeys.clear();
            if (scs != null && scs.sizeSchema() > 0){
                rootKeys.add(JAXB_NODE_NAME);
            }            
        }

        public List<String> keys() {
            List<String> immutable = Collections.unmodifiableList(this.rootKeys);
            return immutable;
        }
        
        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        public void addNotify() {
        }
        
        public void removeNotify() {
        }
        
        public synchronized Node node(String key) {
            Node ret = null;
            if (JAXB_NODE_NAME.equals(key)){
                ret = new JAXBWizardRootNode(this.project);
            }
            return ret;
        }
        
        private void fireChange() {
            ArrayList<ChangeListener> list = new ArrayList<ChangeListener>();
            synchronized (this) {
                list.addAll(listeners);
            }
            Iterator<ChangeListener> it = list.iterator();
            while (it.hasNext()) {
                ChangeListener elem = it.next();
                elem.stateChanged(new ChangeEvent( this ));
            }
        }
        
        private final class ModelListener extends JAXBWizEventListenerAdapter {

            @Override
            public void bindingAdded(JAXBWizEvent event) {
                if (event.getSource() instanceof Schemas){
                    updateKeys((Schemas) event.getSource());    
                    fireChange();
                }
            }
            
            @Override
            public void bindingDeleted(JAXBWizEvent event) {
                if (event.getSource() instanceof Schemas){
                    updateKeys((Schemas) event.getSource());    
                    fireChange();
                }
            }

            @Override
            public void configFileEdited(JAXBWizEvent event) {
                if (event.getSource() instanceof Schemas){
                    updateKeys((Schemas) event.getSource());    
                    fireChange();
                }
            }            
        }
    }
}
