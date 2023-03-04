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

package org.netbeans.modules.websvc.metro.model.ui;

import com.sun.xml.ws.runtime.config.MetroConfig;
import com.sun.xml.ws.runtime.config.ObjectFactory;
import com.sun.xml.ws.runtime.config.TubeFactoryConfig;
import com.sun.xml.ws.runtime.config.TubeFactoryList;
import com.sun.xml.ws.runtime.config.TubelineDefinition;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.xml.wsdl.model.*;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.jaxwsruntimemodel.JavaWsdlMapper;
import org.netbeans.modules.websvc.metro.model.MetroConfigLoader;
import org.netbeans.modules.xml.xam.ModelSource;

/**
 *
 * @author Martin Grebac
 */
public class TubesEditor implements WSEditor {

    private static final Logger logger = Logger.getLogger(TubesEditor.class.getName());

    private JAXWSLightSupport jaxWsSupport;
    private JaxWsService jaxWsService;

    private Project project;
    
    private ObjectFactory objFactory = new ObjectFactory();

    private String ref = null;
    
    private MetroConfigLoader cfgLoader = new MetroConfigLoader();
    private MetroConfig cfg = null;

    private List<TubeFactoryConfig> cfgList = null;

    private TubesConfigPanel panel = null;

    private boolean isClient = false;

    /**
     * Creates a new instance of TubesEditor
     */
    public TubesEditor(Project project) {
        this.project = project;
    }

    public TubesEditor(JAXWSLightSupport supp, JaxWsService jaxWsService, Project p) {
        this.project = p;
        this.jaxWsService = jaxWsService;
        this.jaxWsSupport = supp;
    }

    public String getTitle() {
        return NbBundle.getMessage(TubesEditor.class, "TUBES_EDITOR_TITLE"); //NOI18N
    }

    public JComponent createWSEditorComponent(Node node) {

        if (project == null) return new JPanel();
        
        if (jaxWsService == null) {
            Client client = node.getLookup().lookup(Client.class);
            Service service = node.getLookup().lookup(Service.class);

            isClient = (client != null);
            if (isClient) {
                final JAXWSClientSupport wscs = JAXWSClientSupport.getJaxWsClientSupport(project.getProjectDirectory());
                if (wscs != null) {
                    FileObject wsdlFolder = wscs.getLocalWsdlFolderForClient(client.getName(), false);
                    FileObject wsdl = (wsdlFolder != null) ? wsdlFolder.getFileObject(client.getLocalWsdlFile()) : null;
                    ref = getServiceRef(getModelFromFO(wsdl, false));

                    cfg = cfgLoader.loadMetroConfig(project);
                    if (cfg == null) {
                        cfg = cfgLoader.loadDefaultMetroConfig(project);
                        if (cfg == null) {
                            cfg = cfgLoader.createFreshMetroConfig();
                        }
                    }
                    boolean overrideDefaults = (cfgLoader.getTubeline(cfg, ref) != null);

                    TubelineDefinition tDef = cfgLoader.createTubeline(cfg, ref, client.getName() + "Tubeline");
                    TubeFactoryList factoryList = tDef.getClientSide();
                    if (factoryList == null) {
                        factoryList = objFactory.createTubeFactoryList();
                        tDef.setClientSide(factoryList);
                    }
                    cfgList = factoryList.getTubeFactoryConfigs();

                    try {
                        panel = new TubesConfigPanel(project, factoryList, true, overrideDefaults);
                        return panel;
                    } catch(Exception e){
                        logger.log(Level.SEVERE, null, e);
                    }
                }
            } else if (service != null) {
                final JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
                String serviceName = service.getServiceName();

                // WS from WSDL - get the referencing info from WSDL
                if (wss.isFromWSDL(service.getName())) {

                    FileObject wsdlFolder = wss.getLocalWsdlFolderForService(service.getName(), false);
                    FileObject wsdl = (wsdlFolder != null) ? wsdlFolder.getFileObject(service.getLocalWsdlFile()) : null;

                    WSDLModel wsdlModel = getModelFromFO(wsdl, false);
                    String namespace = wsdlModel.getDefinitions().getTargetNamespace();
                    String portName = service.getPortName();
                    ref = namespace + "#(" + serviceName + "/" + portName + ")";

                // WS from Java - calculate the referencing info from Java class
                } else {

                    FileObject implClass = node.getLookup().lookup(FileObject.class);
                    String namespace = JavaWsdlMapper.getNamespace(JavaWsdlMapper.getPackageFromClass(service.getImplementationClass()));
                    QName portQName = JavaWsdlMapper.getPortName(implClass, namespace);
                    QName serviceQName = JavaWsdlMapper.getServiceName(implClass);

                    serviceName = serviceQName.getLocalPart();

                    ref = namespace + "#(" + serviceName + "/" + portQName.getLocalPart() + ")";
                }

                cfg = cfgLoader.loadMetroConfig(project);
                if (cfg == null) {
                    cfg = cfgLoader.loadDefaultMetroConfig(project);
                    if (cfg == null) {
                        cfg = cfgLoader.createFreshMetroConfig();
                    }
                }
                boolean overrideDefaults = (cfgLoader.getTubeline(cfg, ref) != null);

                TubelineDefinition tDef = cfgLoader.createTubeline(cfg, ref, serviceName + "Tubeline");
                TubeFactoryList factoryList = tDef.getEndpointSide();
                if (factoryList == null) {
                    factoryList = objFactory.createTubeFactoryList();
                    tDef.setEndpointSide(factoryList);
                }
                cfgList = factoryList.getTubeFactoryConfigs();

                try {
                    panel = new TubesConfigPanel(project, factoryList, false, overrideDefaults);
                    return panel;
                } catch(Exception e){
                    logger.log(Level.SEVERE, null, e);
                }
            }
        } else {
            if (!jaxWsService.isServiceProvider()) { //client

                FileObject wsdlFolder = jaxWsSupport.getWsdlFolder(false);
                FileObject wsdl = (wsdlFolder != null) ? wsdlFolder.getFileObject(jaxWsService.getLocalWsdl()) : null;
                WSDLModel model = getModelFromFO(wsdl, false);
                ref = getServiceRef(model);

                String serviceName = null;
                Collection<org.netbeans.modules.xml.wsdl.model.Service> services = model.getDefinitions().getServices();
                for (org.netbeans.modules.xml.wsdl.model.Service s : services) {
                    serviceName = s.getName();
                    break;
                }

                cfg = cfgLoader.loadMetroConfig(project);
                if (cfg == null) {
                    cfg = cfgLoader.loadDefaultMetroConfig(project);
                    if (cfg == null) {
                        cfg = cfgLoader.createFreshMetroConfig();
                    }
                }
                boolean overrideDefaults = (cfgLoader.getTubeline(cfg, ref) != null);


                TubelineDefinition tDef = cfgLoader.createTubeline(cfg, ref, serviceName + "Tubeline");
                TubeFactoryList factoryList = tDef.getClientSide();
                if (factoryList == null) {
                    factoryList = objFactory.createTubeFactoryList();
                    tDef.setClientSide(factoryList);
                }
                cfgList = factoryList.getTubeFactoryConfigs();

                try {
                    panel = new TubesConfigPanel(project, factoryList, true, overrideDefaults);
                    return panel;
                } catch(Exception e){
                    logger.log(Level.SEVERE, null, e);
                }
            } else {
                String serviceName = jaxWsService.getServiceName();

                // WS from WSDL - get the referencing info from WSDL
                if (jaxWsService.getLocalWsdl() != null) {
                    FileObject wsdlFolder = jaxWsSupport.getWsdlFolder(false);
                    FileObject wsdl = (wsdlFolder != null) ? wsdlFolder.getFileObject(jaxWsService.getLocalWsdl()) : null;

                    WSDLModel wsdlModel = getModelFromFO(wsdl, false);
                    String namespace = wsdlModel.getDefinitions().getTargetNamespace();
                    String portName = jaxWsService.getPortName();
                    ref = namespace + "#(" + serviceName + "/" + portName + ")";

                // WS from Java - calculate the referencing info from Java class
                } else {
                    FileObject implClass = node.getLookup().lookup(FileObject.class);
                    String namespace = JavaWsdlMapper.getNamespace(JavaWsdlMapper.getPackageFromClass(jaxWsService.getImplementationClass()));
                    QName portQName = JavaWsdlMapper.getPortName(implClass, namespace);
                    QName serviceQName = JavaWsdlMapper.getServiceName(implClass);

                    serviceName = serviceQName.getLocalPart();

                    ref = namespace + "#(" + serviceName + "/" + portQName.getLocalPart() + ")";
                }

                cfg = cfgLoader.loadMetroConfig(project);
                if (cfg == null) {
                    cfg = cfgLoader.loadDefaultMetroConfig(project);
                    if (cfg == null) {
                        cfg = cfgLoader.createFreshMetroConfig();
                    }
                }
                boolean overrideDefaults = (cfgLoader.getTubeline(cfg, ref) != null);

                TubelineDefinition tDef = cfgLoader.createTubeline(cfg, ref, serviceName + "Tubeline");
                TubeFactoryList factoryList = tDef.getEndpointSide();
                if (factoryList == null) {
                    factoryList = objFactory.createTubeFactoryList();
                    tDef.setEndpointSide(factoryList);
                }
                cfgList = factoryList.getTubeFactoryConfigs();

                try {
                    panel = new TubesConfigPanel(project, factoryList, false, overrideDefaults);
                    return panel;
                } catch(Exception e){
                    logger.log(Level.SEVERE, null, e);
                }
            }
        }

        return new JPanel();
    }

    private String getServiceRef(WSDLModel model) {
        Definitions def = model.getDefinitions();

        String namespace = def.getTargetNamespace();
        String serviceName = null;
        String portName = null;
        
        Collection<org.netbeans.modules.xml.wsdl.model.Service> services = def.getServices();
        for (org.netbeans.modules.xml.wsdl.model.Service s : services) {
            serviceName = s.getName();
            Collection<org.netbeans.modules.xml.wsdl.model.Port> ports = s.getPorts();
            for (org.netbeans.modules.xml.wsdl.model.Port p : ports) {
                portName = p.getName();
                break;
            }
            break;
        }

        return namespace + "#(" + serviceName + "/" + portName + ")";
    }

    private static WSDLModel getModelFromFO(FileObject wsdlFO, boolean editable) {
        WSDLModel model = null;
        ModelSource ms = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(wsdlFO, editable);
        try {
            model = WSDLModelFactory.getDefault().getModel(ms);
            if (model != null) {
                model.sync();
            }
        } catch (Exception ex) { // we need this, as we can't rely on wsdl model at all
            logger.log(Level.INFO, null, ex);
        }
        return model;
    }

    public void save(Node node) {
        if (node == null) return;
        try {
            if ((panel != null) && (panel.isChanged())) {
                if (panel.isOverride()) {
                    cfgList.clear();
                    cfgList.addAll(cfgLoader.createTubeFactoryConfigList(panel.getTubeList()));
                } else {
                    cfgLoader.removeTubelineReference(cfg, ref);
                }
                cfgLoader.saveMetroConfig(cfg, project);
            }
        } catch (Exception e){
            logger.log(Level.SEVERE, null, e);
        }
    }

    public void cancel(Node node) {}
    
    public String getDescription() {
        return NbBundle.getMessage(TubesEditor.class, "TUBES_CONFIG_DESC");
    }
}
