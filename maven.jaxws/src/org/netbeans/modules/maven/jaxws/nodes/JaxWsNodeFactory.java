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

package org.netbeans.modules.maven.jaxws.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportProvider;
import org.netbeans.modules.websvc.project.api.WebServiceData;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Milan Kuchtiak
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-maven",position=85)
public class JaxWsNodeFactory implements NodeFactory {

    /** Creates a new instance of WebServicesNodeFactory */
    public JaxWsNodeFactory() {
    }
    
    @Override
    public NodeList createNodes(Project p) {
        assert p != null;
        return new WsNodeList(p);
    }
    
    private static class WsNodeList implements NodeList<String>, PropertyChangeListener, LookupListener, Runnable {
        // Web Services
        private static final String KEY_SERVICES = "web_services"; // NOI18N
        // Web Service Client
        private static final String KEY_SERVICE_REFS = "serviceRefs"; // NOI18N
        
        private final Project project;
        private JAXWSLightSupport jaxwsSupport;
        private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private Lookup.Result<JAXWSLightSupportProvider> lookupResult;
        private final AtomicReference<List<String>> nodeList; 
        
        private static final RequestProcessor JAX_WS_RP = new RequestProcessor(WsNodeList.class);
        
        public WsNodeList(Project proj) {
            project = proj;
            nodeList = new AtomicReference<List<String>>();
        }

        @Override
        public void run() {
            List<String> result = new ArrayList<String>(2);
            if ( jaxwsSupport != null) {
                List<JaxWsService> services = jaxwsSupport.getServices();
                boolean hasServices = false;
                boolean hasClients = false;
                for (JaxWsService s:services) {
                    if (!hasServices && s.isServiceProvider()) {
                        hasServices = true;
                    } else if (!hasClients && !s.isServiceProvider()) {
                        hasClients = true;
                    }
                    if (hasServices && hasClients) break;
                }
                if (hasServices) {
                    result.add(KEY_SERVICES);
                }
                if (hasClients) {
                    result.add(KEY_SERVICE_REFS);
                }
                nodeList.set( result );
                if (hasServices || hasClients) {
                    fireChange();
                }
            }
        }

        @Override
        public List<String> keys() {
            List<String> keys = nodeList.get();
            if ( keys != null && !keys.isEmpty()) {
                nodeList.compareAndSet( keys, null);
                return keys;
            } else {
                JAX_WS_RP.post(this, 100);
            }
            return Collections.emptyList();
        }
        
        @Override
        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
//        
        @Override
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
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
        
        @Override
        public Node node(String key) {
            if (KEY_SERVICES.equals(key)) {
                //JAXWSLightView view = JAXWSLightView.getJAXWSLightView(project.getProjectDirectory());
                //return view.createJAXWSView(project);
                WebServiceData wsData = WebServiceData.getWebServiceData(project);
                return new JaxWsRootNode(project, wsData);
            } else if (KEY_SERVICE_REFS.equals(key)) {
                WebServiceData wsData = WebServiceData.getWebServiceData(project);
                return new JaxWsClientRootNode(project, wsData);
            }
            return null;
        }
        
        @Override
        public void addNotify() {
            if (jaxwsSupport == null) {
                jaxwsSupport = JAXWSLightSupport.getJAXWSLightSupport(project.getProjectDirectory());
            }
            if (jaxwsSupport != null) {
                jaxwsSupport.addPropertyChangeListener(WeakListeners.propertyChange(this, jaxwsSupport));
            } else {
                lookupResult = project.getLookup().lookupResult(JAXWSLightSupportProvider.class);
                if (lookupResult.allInstances().size() == 0) {
                    lookupResult.addLookupListener(this);
                }
            }
        }
        
        @Override
        public void removeNotify() {
            if (jaxwsSupport != null) {
                jaxwsSupport.removePropertyChangeListener(WeakListeners.propertyChange(this, jaxwsSupport));
                jaxwsSupport = null;
            }
            if (lookupResult != null) {
                lookupResult.removeLookupListener(this);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            fireChange();
        }
        
        @Override
        public void resultChanged(LookupEvent evt) {
            jaxwsSupport = JAXWSLightSupport.getJAXWSLightSupport(project.getProjectDirectory());
            if (jaxwsSupport != null) {
                jaxwsSupport.addPropertyChangeListener(WeakListeners.propertyChange(WsNodeList.this, jaxwsSupport));
            }
        }
    }
    
}
