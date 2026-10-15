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
package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.awt.Dialog;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import javax.swing.JEditorPane;

import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints;
import org.netbeans.modules.websvc.api.jaxws.project.config.Handler;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChains;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChainsProvider;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.ServerType;
import org.netbeans.modules.websvc.core.WSStackUtils;
import org.netbeans.modules.websvc.core.WebServiceReference;
import org.netbeans.modules.websvc.core.WebServiceTransferable;
import org.netbeans.modules.websvc.core.jaxws.actions.AddOperationAction;
import org.netbeans.modules.websvc.core.jaxws.actions.JaxWsRefreshAction;
import org.netbeans.modules.websvc.core.jaxws.actions.WsTesterPageAction;
import org.netbeans.modules.websvc.spi.support.ConfigureHandlerAction;
import org.netbeans.modules.websvc.api.support.ConfigureHandlerCookie;
import org.netbeans.modules.websvc.spi.support.MessageHandlerPanel;
import org.netbeans.modules.websvc.core.wseditor.support.WSEditAttributesAction;
import org.netbeans.modules.websvc.jaxws.api.JaxWsRefreshCookie;
import org.netbeans.modules.websvc.jaxws.api.JaxWsTesterCookie;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint;
import org.netbeans.modules.websvc.api.jaxws.project.config.EndpointsProvider;
import org.netbeans.modules.websvc.core.WsWsdlCookie;
import org.netbeans.modules.websvc.core.jaxws.actions.ConvertToRestAction;
import org.netbeans.modules.websvc.core.jaxws.actions.ConvertToRestCookieImpl;
import org.netbeans.modules.websvc.core.jaxws.actions.JaxWsGenWSDLAction;
import org.netbeans.modules.websvc.core.jaxws.actions.JaxWsGenWSDLImpl;
import org.netbeans.modules.websvc.core.wseditor.support.EditWSAttributesCookieImpl;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

public class JaxWsNode extends AbstractNode implements
        WsWsdlCookie, JaxWsTesterCookie, ConfigureHandlerCookie {

    private static final RequestProcessor RP = new RequestProcessor(JaxWsNode.class);
    Service service;
    FileObject srcRoot;
    JaxWsModel jaxWsModel;
    private FileObject implBeanClass;
    InstanceContent content;
    Project project;
    
    private final RequestProcessor.Task implClassModifiedTask = RP.create(new Runnable () {
        @Override
        public void run() {
            setShortDescription(getWsdlURL());
        }
    });

    public JaxWsNode(JaxWsModel jaxWsModel, Service service, FileObject srcRoot, FileObject implBeanClass) {
        this(jaxWsModel, service, srcRoot, implBeanClass, new InstanceContent());
    }

    private JaxWsNode(JaxWsModel jaxWsModel, Service service, FileObject srcRoot, FileObject implBeanClass, InstanceContent content) {
        super(new JaxWsChildren(service, srcRoot, implBeanClass), new AbstractLookup(content));
        this.jaxWsModel = jaxWsModel;
        this.service = service;
        this.srcRoot = srcRoot;
        this.content = content;
        this.implBeanClass = implBeanClass;
        project = FileOwnerQuery.getOwner(srcRoot);
        if (implBeanClass.getAttribute("jax-ws-service") == null ||
                service.isUseProvider() && implBeanClass.getAttribute("jax-ws-service-provider") == null) {
            try {
                if (implBeanClass.getAttribute("jax-ws-service") == null) {
                    implBeanClass.setAttribute("jax-ws-service", Boolean.TRUE);
                }
                if (service.isUseProvider() && implBeanClass.getAttribute("jax-ws-service-provider") == null) {
                    implBeanClass.setAttribute("jax-ws-service-provider", Boolean.TRUE);
                }
                //getDataObject().setValid(false);
                Mutex.EVENT.writeAccess( new Runnable(){
                    /* (non-Javadoc)
                     * @see java.lang.Runnable#run()
                     */
                    @Override
                    public void run() {
                        try {
                            EditorCookie cookie = getDataObject().getCookie(EditorCookie.class);
                            JEditorPane[] panes = cookie.getOpenedPanes();
                            getDataObject().setValid(false);
                            if ( panes != null && panes.length >0 ){
                                getDataObject().getCookie(EditorCookie.class).open();
                            }
                        }
                        catch (PropertyVetoException ex) {
                            Logger.getLogger(JaxWsNode.class.getName()).log( 
                                    Level.WARNING, null , ex);
                        } 
                    }
                });
            /*} catch (PropertyVetoException ex) {
                ErrorManager.getDefault().notify(ex);*/
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        String serviceName = service.getName();
        setName(serviceName);
        content.add(this);
        content.add(service);
        content.add(implBeanClass);
        content.add(new EditWSAttributesCookieImpl(this, jaxWsModel));
        if (service.getWsdlUrl() != null && !service.isUseProvider()) {
            content.add(new RefreshServiceImpl());
        } else {
            content.add(new JaxWsGenWSDLImpl(project, serviceName));
        }
        if (isWebProject()) {
            content.add(new ConvertToRestCookieImpl(this));
        }
        OpenCookie cookie = new OpenCookie() {

            @Override
            public void open() {
                OpenCookie oc = getOpenCookie();
                if (oc != null) {
                    oc.open();
                }
            }
        };
        content.add(cookie);
        RP.post(new Runnable() {

            @Override
            public void run() {
                JaxWsNode.this.setValue("wsdl-url", getWsdlURL());      // NOI18N
                setShortDescription(getWsdlURL());
            }
        });
        try {
            DataObject dataObject = DataObject.find(implBeanClass);
            dataObject.addPropertyChangeListener( new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ( DataObject.PROP_MODIFIED.equals( evt.getPropertyName()) ) {
                        implClassModifiedTask.schedule(500);
                    }
                }
            });
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    private boolean isWebProject() {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
            if (J2eeModule.Type.WAR.equals(moduleType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        if (service.getWsdlUrl() != null) {
            return NbBundle.getMessage(JaxWsNode.class, "LBL_serviceNodeName", service.getServiceName(), service.getPortName());
        } else {
            return service.getName();
        }
    }

    /*@Override
    public String getShortDescription() {
        return getWsdlURL();
    }*/
    private static final String WAITING_BADGE = "org/netbeans/modules/websvc/core/webservices/ui/resources/waiting.png"; // NOI18N
    private static final String ERROR_BADGE = "org/netbeans/modules/websvc/core/webservices/ui/resources/error-badge.gif"; //NOI18N
    private static final String SERVICE_BADGE = "org/netbeans/modules/websvc/core/webservices/ui/resources/XMLServiceDataIcon.png"; //NOI18N
    private java.awt.Image cachedWaitingBadge;
    private java.awt.Image cachedErrorBadge;
    private java.awt.Image cachedServiceBadge;

    @Override
    public java.awt.Image getIcon(int type) {
        WsdlModeler wsdlModeler = ((JaxWsChildren) getChildren()).getWsdlModeler();
        if (wsdlModeler == null) {
            return getServiceImage();
        } else if (wsdlModeler.getCreationException() == null) {
            if (((JaxWsChildren) getChildren()).isModelGenerationFinished()) {
                return getServiceImage();
            } else {
                return ImageUtilities.mergeImages(getServiceImage(), getWaitingBadge(), 15, 8);
            }
        } else {
            Image dirtyNodeImage = ImageUtilities.mergeImages(getServiceImage(), getErrorBadge(), 6, 6);
            if (((JaxWsChildren) getChildren()).isModelGenerationFinished()) {
                return dirtyNodeImage;
            } else {
                return ImageUtilities.mergeImages(dirtyNodeImage, getWaitingBadge(), 15, 8);
            }
        }
    }

    private java.awt.Image getServiceImage() {
        if (cachedServiceBadge == null) {
            cachedServiceBadge = ImageUtilities.loadImage(SERVICE_BADGE);
        }
        return cachedServiceBadge;
    }

    private java.awt.Image getErrorBadge() {
        if (cachedErrorBadge == null) {
            cachedErrorBadge = ImageUtilities.loadImage(ERROR_BADGE);
        }
        return cachedErrorBadge;
    }

    private java.awt.Image getWaitingBadge() {
        if (cachedWaitingBadge == null) {
            cachedWaitingBadge = ImageUtilities.loadImage(WAITING_BADGE);
        }
        return cachedWaitingBadge;
    }

    void changeIcon() {
        fireIconChange();
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    private DataObject getDataObject() {
        FileObject f = getImplBean();
        if (f != null) {
            try {
                return DataObject.find(f);
            } catch (DataObjectNotFoundException de) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, de.toString());
            }
        }
        return null;
    }

    private OpenCookie getOpenCookie() {
        OpenCookie oc = null;
        FileObject f = getImplBean();
        if (f != null) {
            try {
                DataObject d = DataObject.find(f);
                oc = d.getCookie(OpenCookie.class);
            } catch (DataObjectNotFoundException de) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, de.toString());
            }
        }
        return oc;
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }

    // Create the popup menu:
    @Override
    public Action[] getActions(boolean context) {
        //DataObject dobj = getCookie(DataObject.class);
        ArrayList<Action> actions = new ArrayList<Action>(Arrays.asList(
                SystemAction.get(OpenAction.class),
                SystemAction.get(JaxWsRefreshAction.class),
                null,
                SystemAction.get(AddOperationAction.class),
                null,
                SystemAction.get(WsTesterPageAction.class),
                null,
                SystemAction.get(WSEditAttributesAction.class),
                null,
                SystemAction.get(ConfigureHandlerAction.class),
                null,
                SystemAction.get(JaxWsGenWSDLAction.class),
                null,
                SystemAction.get(ConvertToRestAction.class),
                null,
                SystemAction.get(DeleteAction.class),
                null,
                SystemAction.get(PropertiesAction.class)));
        addFromLayers(actions, "WebServices/Services/Actions");
        return actions.toArray(new Action[0]);
    }

    private void addFromLayers(List<Action> actions, String path) {
        Lookup look = Lookups.forPath(path);
        for (Object next : look.lookupAll(Object.class)) {
            if (next instanceof Action) {
                actions.add((Action) next);
            } else if (next instanceof javax.swing.JSeparator) {
                actions.add(null);
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    // Handle deleting:
    @Override
    public boolean canDestroy() {
        return true;
    }

    /**
     * get URL for Web Service WSDL file
     */
    private ServerContextInfo getServerContextInfo() {
        String portNumber = "8080"; //NOI18N
        String hostName = "localhost"; //NOI18N

        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        String serverInstanceID = provider.getServerInstanceID();
        if (serverInstanceID == null) {
            Logger.getLogger(JaxWsNode.class.getName()).log(Level.INFO, "Can not detect target J2EE server"); //NOI18N
            return null;
        }
        // getting port and host name
        ServerInstance serverInstance = Deployment.getDefault().getServerInstance(serverInstanceID);
        try {
            ServerInstance.Descriptor instanceDescriptor = serverInstance.getDescriptor();
            if (instanceDescriptor != null) {
                int port = instanceDescriptor.getHttpPort();
                portNumber = port == 0 ? "8080" : String.valueOf(port); //NOI18N
                String hstName = instanceDescriptor.getHostname();
                if (hstName != null) {
                    hostName = hstName;
                }
            } else {
                // using the old way to obtain port name and host name
                // should be removed if ServerInstance.Descriptor is implemented in server plugins
                InstanceProperties instanceProperties = provider.getInstanceProperties();
                if (instanceProperties == null) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(JaxWsNode.class, "MSG_MissingServer"), NotifyDescriptor.ERROR_MESSAGE));
                } else {
                    portNumber = getPortNumber(instanceProperties);
                    hostName = getHostName(instanceProperties);
                }
            }
        } catch (InstanceRemovedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Removed ServerInstance", ex); //NOI18N
        }

        String contextRoot = ""; //NOI18N
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();

        WSStackUtils stackUtils = new WSStackUtils(project);
        if (J2eeModule.Type.WAR.equals(moduleType)) {
            J2eeModuleProvider.ConfigSupport configSupport = provider.getConfigSupport();
            try {
                contextRoot = configSupport.getWebContextRoot();
            } catch (ConfigurationException e) {
                // TODO the context root value could not be read, let the user know about it
            }
            if (contextRoot != null && contextRoot.startsWith("/")) {
                //NOI18N
                contextRoot = contextRoot.substring(1);
            }
        } else if (J2eeModule.Type.EJB.equals(moduleType) && ServerType.JBOSS == stackUtils.getServerType()) {
            // JBoss type
            contextRoot = project.getProjectDirectory().getName();
        }

        return new ServerContextInfo(hostName, portNumber, contextRoot);
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("DE_MIGHT_IGNORE")
    private ServiceInfo getServiceInfo() {
        ServiceInfo serviceInfo = new ServiceInfo();
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        boolean isEjb = J2eeModule.Type.EJB.equals(provider.getJ2eeModule().getType());
        try {
            resolveServiceInfo(serviceInfo, isEjb);
        } catch (UnsupportedEncodingException ex) {}
        return serviceInfo;
    }

    private String getServiceUri(String contextRoot) {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        // need to compute from annotations
        String wsURI = null;

        WSStackUtils stackUtils = new WSStackUtils(project);
        boolean isJsr109Supported = stackUtils.isJsr109Supported();
        if (J2eeModule.Type.WAR.equals(moduleType) && ServerType.JBOSS == stackUtils.getServerType()) {
            // JBoss type
            try {                // JBoss type
                wsURI = (contextRoot == null ? "" : contextRoot+"/")+getUriFromDD(moduleType);
            } catch (UnsupportedEncodingException ex) {
                // this shouldn't happen'
            }
        } else if (J2eeModule.Type.EJB.equals(moduleType) && ServerType.JBOSS == stackUtils.getServerType()) {
            // JBoss type
            wsURI = (contextRoot == null ? "" : contextRoot+"/")+getNameFromPackageName(service.getImplementationClass());
        } else if (isJsr109Supported && ProjectUtil.isJavaEE5orHigher(project) || JaxWsUtils.isEjbJavaEE5orHigher(project)) {
            try {

                ServiceInfo serviceInfo = new ServiceInfo();
                resolveServiceInfo(serviceInfo, J2eeModule.Type.EJB.equals(moduleType));

                boolean fromStack = false;
                WSStack<JaxWs> jaxWsStack = stackUtils.getWsStack(JaxWs.class);
                if (jaxWsStack != null) {
                    JaxWs.UriDescriptor uriDescriptor = jaxWsStack.get().getWsUriDescriptor();
                    if (uriDescriptor != null) {
                        fromStack = true;

                        //ServerContextInfo serverContextInfo = getServerContextInfo();
                        wsURI = uriDescriptor.getServiceUri(contextRoot, serviceInfo.getServiceName(), serviceInfo.getPortName(), serviceInfo.isEjb());
                    }
                }

                if (!fromStack) {
                    // default service URI
                    String portName = serviceInfo.getPortName();
                    wsURI = (contextRoot == null ? "" : contextRoot+"/")+serviceInfo.getServiceName()+ (portName == null ? "" : "/"+portName);
                }

            } catch (UnsupportedEncodingException ex) {
                // this shouldn't happen'
            }
        } else {
            // non jsr109 type (Tomcat)
            try {
                wsURI = (contextRoot == null ? "" : contextRoot+"/")+getNonJsr109Uri(moduleType);
            } catch (UnsupportedEncodingException ex) {
                // this shouldn't happen'
            }
        }
        return wsURI;
    }

    private String getNonJsr109Uri(J2eeModule.Type moduleType) throws UnsupportedEncodingException {
        if (J2eeModule.Type.WAR.equals(moduleType)) {
            WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
            FileObject webInfFo = webModule.getWebInf();
            if (webInfFo != null) {
                FileObject sunJaxwsFo = webInfFo.getFileObject("sun-jaxws", "xml"); //NOI18N
                if (sunJaxwsFo != null) {
                    try {
                        Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunJaxwsFo);
                        if (endpoints != null) {
                            String urlPattern = findUrlPattern(endpoints, service.getImplementationClass());
                            if (urlPattern != null) {
                                return URLEncoder.encode(urlPattern, "UTF-8"); //NOI18N
                            }
                        }
                    } catch (IOException ex) {
                        ErrorManager.getDefault().log(ex.getLocalizedMessage());
                    }
                }
            }
        }
        return URLEncoder.encode(getNameFromPackageName(service.getImplementationClass()), "UTF-8"); //NOI18N
    }

    private String getUriFromDD(J2eeModule.Type moduleType) throws UnsupportedEncodingException {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null) {
            FileObject ddFo = webModule.getDeploymentDescriptor();
            if (ddFo != null) {
                try {
                    WebApp webApp = DDProvider.getDefault().getDDRoot(ddFo);
                    if (webApp != null) {
                        String urlPattern = findUrlPattern(webApp, service.getImplementationClass());
                        if (urlPattern != null) {
                            return URLEncoder.encode(urlPattern, "UTF-8"); //NOI18N
                        }
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
                }
            }
        }
        return URLEncoder.encode(getNameFromPackageName(service.getImplementationClass()), "UTF-8"); //NOI18N
    }

    private String findUrlPattern(Endpoints endpoints, String implementationClass) {
        Endpoint[] endp = endpoints.getEndpoints();
        for (int i = 0; i < endp.length; i++) {
            if (implementationClass.equals(endp[i].getImplementation())) {
                String urlPattern = endp[i].getUrlPattern();
                if (urlPattern != null) {
                    return urlPattern.startsWith("/") ? urlPattern.substring(1) : urlPattern; //NOI18N
                }
            }
        }
        return null;
    }

    private String findUrlPattern(WebApp webApp, String implementationClass) {
        for (Servlet servlet : webApp.getServlet()) {
            if (implementationClass.equals(servlet.getServletClass())) {
                String servletName = servlet.getServletName();
                if (servletName != null) {
                    for (ServletMapping servletMapping : webApp.getServletMapping()) {
                        if (servletName.equals(servletMapping.getServletName())) {
                            String urlPattern = ((ServletMapping25)servletMapping).getUrlPatterns()[0];
                            return urlPattern.startsWith("/") ? urlPattern.substring(1) : urlPattern; //NOI18N
                        }
                    }
                }
            }
        }
        return null;
    }

    private void resolveServiceInfo(final ServiceInfo serviceInfo, 
            final boolean inEjbProject) throws UnsupportedEncodingException 
    {
        final String[] serviceName = new String[1];
        final String[] name = new String[1];
        final boolean[] isProvider = {false};
        serviceInfo.setEjb(inEjbProject);
        JavaSource javaSource = getImplBeanJavaSource();
        if (javaSource != null) {
            CancellableTask<CompilationController> task = new 
                CancellableTask<CompilationController>() 
                {

                    @Override
                    public void run(CompilationController controller) 
                        throws IOException 
                    {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        TypeElement typeElement = SourceUtils.
                            getPublicTopLevelElement(controller);
                        if ( typeElement == null ){
                            return;
                        }
                        boolean foundWsAnnotation = resolveServiceUrl(controller, 
                                typeElement,  serviceName, name);
                        if (!foundWsAnnotation) {
                            isProvider[0] = JaxWsUtils.hasAnnotation(typeElement, 
                                    "javax.xml.ws.WebServiceProvider");     // NOI18N
                        }
                        if (!inEjbProject) {
                            serviceInfo.setEjb(isStatelessEjb(typeElement));
                        }
                    }

                @Override
                public void cancel() {
                }
            };
            try {
                javaSource.runUserActionTask(task, true);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }

        String qualifiedImplClassName = service.getImplementationClass();
        String implClassName = getNameFromPackageName(qualifiedImplClassName);
        if (serviceName[0] == null) {
            serviceName[0] = URLEncoder.encode(implClassName + "Service", "UTF-8");
        }
        serviceInfo.setServiceName(serviceName[0]);
        if (name[0] == null) {
            if (isProvider[0]) {
                //per JSR 109, use qualified impl class name for EJB
                name[0] = qualifiedImplClassName;
            } else {
                name[0] = implClassName;
            }
            name[0] = URLEncoder.encode(name[0], "UTF-8"); //NOI18N
        }
        serviceInfo.setPortName(name[0]);
    }
    
    private boolean resolveServiceUrl(CompilationController controller, 
            TypeElement targetElement, String[] serviceName, String[] name) 
                throws IOException 
    {
        boolean foundWsAnnotation = false;
        List<? extends AnnotationMirror> annotations = targetElement.getAnnotationMirrors();
        for (AnnotationMirror anMirror : annotations) {
            boolean isWebMethodAnnotation = JaxWsUtils.hasFqn(anMirror, 
                    "javax.jws.WebService");   // NOI18N
            if (isWebMethodAnnotation) {
                foundWsAnnotation = true;
                Map<? extends ExecutableElement, ? extends AnnotationValue> 
                    expressions = anMirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, 
                        ? extends AnnotationValue> entry : expressions.entrySet()) 
                {
                    if (entry.getKey().getSimpleName().contentEquals("serviceName")) { //NOI18N
                        serviceName[0] = (String) expressions.get(entry.getKey()).
                            getValue();
                        if (serviceName[0] != null) {
                            serviceName[0] = URLEncoder.encode(serviceName[0], 
                                    "UTF-8"); //NOI18N
                        }
                    } else if (entry.getKey().getSimpleName().
                            contentEquals("name"))  //NOI18N
                    {
                        name[0] = (String) expressions.get(entry.getKey()).getValue();
                        if (name[0] != null) {
                            name[0] = URLEncoder.encode(name[0], "UTF-8"); //NOI18N
                        }
                    }
                    if (serviceName[0] != null && name[0] != null) {
                        break;
                    }
                }
                break;
            } // end if
        } // end for
        return foundWsAnnotation;
    }

    private boolean isStatelessEjb(TypeElement targetElement) {
        return JaxWsUtils.hasAnnotation(targetElement, "javax.ejb.Stateless"); // NOI18N
    }

    private String getNameFromPackageName(String packageName) {
        int index = packageName.lastIndexOf("."); //NOI18N
        return index >= 0 ? packageName.substring(index + 1) : packageName;
    }

    @Override
    public String getWsdlURL() {
        String wsdlUrl = getWebServiceURL();
        return wsdlUrl.length() == 0 ? "" : wsdlUrl + "?wsdl"; //NOI18N
    }

    private String getWebServiceURL() {
        ServerContextInfo serverContextInfo = getServerContextInfo();
        if ( serverContextInfo == null ){
            return "";
        }
        String contextRoot = serverContextInfo.getContextRoot();
        return "http://" + serverContextInfo.getHost() + ":" + serverContextInfo.getPort() + "/" + //NOI18N
                getServiceUri(contextRoot);
    }

    /**
     * get URL for Web Service Tester Page
     */
    @Override
    public String getTesterPageURL() {
        ServerContextInfo serverContextInfo = getServerContextInfo();
        ServiceInfo serviceInfo = getServiceInfo();
        WSStackUtils stackUtils = new WSStackUtils(project);
        boolean isJsr109Supported = stackUtils.isJsr109Supported();
        if (isJsr109Supported && ProjectUtil.isJavaEE5orHigher(project) || JaxWsUtils.isEjbJavaEE5orHigher(project)) {
            WSStack<JaxWs> jaxWsStack = stackUtils.getWsStack(JaxWs.class);
            if (jaxWsStack != null) {
                JaxWs.UriDescriptor uriDescriptor = jaxWsStack.get().getWsUriDescriptor();
                if (uriDescriptor != null) {
                    return uriDescriptor.getTesterPageUri(serverContextInfo.getHost(), serverContextInfo.getPort(), serverContextInfo.getContextRoot(), serviceInfo.getServiceName(), serviceInfo.getPortName(), serviceInfo.isEjb());
                }
            }
        } else if (!serviceInfo.isEjb()) { //Tomcat case
            try {
                String serviceUri = getNonJsr109Uri(J2eeModule.Type.WAR);
                String contextRoot = serverContextInfo.getContextRoot();
                return "http://"+serverContextInfo.getHost()+":"+serverContextInfo.getPort()+"/"+ //NOI18N
                        (contextRoot==null ? "" : contextRoot+"/") + serviceUri; //NOI18N
            } catch (UnsupportedEncodingException ex) {}
        }
        return getWebServiceURL(); //NOI18N
    }

    @Override
    public void destroy() throws java.io.IOException {
        String serviceName = service.getName();
        NotifyDescriptor.Confirmation notifyDesc = new NotifyDescriptor.Confirmation(NbBundle.getMessage(JaxWsNode.class, "MSG_CONFIRM_DELETE", serviceName));
        DialogDisplayer.getDefault().notify(notifyDesc);
        if (notifyDesc.getValue() == NotifyDescriptor.YES_OPTION) {
            JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
            if (wss != null) {
                FileObject localWsdlFolder = wss.getLocalWsdlFolderForService(serviceName, false);
                if (localWsdlFolder != null) {
                    // removing local wsdl and xml artifacts
                    FileLock lock = null;
                    FileObject clientArtifactsFolder = localWsdlFolder.getParent();
                    try {
                        lock = clientArtifactsFolder.lock();
                        clientArtifactsFolder.delete(lock);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                    }
                    // removing wsdl and xml artifacts from WEB-INF/wsdl
                    FileObject wsdlFolder = wss.getWsdlFolder(false);
                    if (wsdlFolder != null) {
                        FileObject serviceWsdlFolder = wsdlFolder.getFileObject(serviceName);
                        if (serviceWsdlFolder != null) {
                            try {
                                lock = serviceWsdlFolder.lock();
                                serviceWsdlFolder.delete(lock);
                            } finally {
                                if (lock != null) {
                                    lock.releaseLock();
                                }
                            }
                        }
                    }
                    // cleaning java artifacts
                    FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
                    try {
                        ExecutorTask wsimportTask = ActionUtils.runTarget(buildImplFo, new String[]{"wsimport-service-clean-" + serviceName}, null); //NOI18N
                        wsimportTask.waitFinished();
                    } catch (java.io.IOException ex) {
                        ErrorManager.getDefault().log(ex.getLocalizedMessage());
                    } catch (IllegalArgumentException ex) {
                        ErrorManager.getDefault().log(ex.getLocalizedMessage());
                    }
                }

                // removing service from jax-ws.xml
                wss.removeService(serviceName);

                // remove non JSR109 entries
                Boolean isJsr109 = jaxWsModel.getJsr109();
                if (isJsr109 != null && !isJsr109) {
                    if (service.getWsdlUrl() != null) {
                        //if coming from wsdl
                        serviceName = service.getServiceName();
                    }
                    wss.removeNonJsr109Entries(serviceName);
                }
                super.destroy();
            }
        }
    }

    private FileObject getImplBean() {
        String implBean = service.getImplementationClass();
        if (implBean != null) {
            return srcRoot.getFileObject(implBean.replace('.', '/') + ".java");
        }
        return null;
    }

    private JavaSource getImplBeanJavaSource() {
        FileObject implBean = getImplBean();
        if (implBean != null) {
            return JavaSource.forFileObject(implBean);
        }
        return null;
    }

    /**
     * Adds possibility to display custom delete dialog
     */
    @Override
    public Object getValue(String attributeName) {
        Object retValue;
        if (attributeName.equals("customDelete")) {
            //NOI18N
            retValue = Boolean.TRUE;
        } else {
            retValue = super.getValue(attributeName);
        }
        return retValue;
    }

    /**
     * Implementation of the ConfigureHandlerCookie
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @Override
    public void configureHandler() {
        FileObject implBeanFo = getImplBean();
        if (implBeanFo == null) {
            // unable to find implementation class
            NotifyDescriptor.Message dialogDesc = new NotifyDescriptor.Message(
                    NbBundle.getMessage(JaxWsNode.class, 
                            "ERR_missingImplementationClass"),  // NOI18N  
                                NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(dialogDesc);
            return;
        }
        List<String> handlerClasses = new ArrayList<String>();
        FileObject handlerFO = null;
        HandlerChains handlerChains = null;
        //obtain the handler config file, if any from annotation in implbean
        final String[] handlerFileName = new String[1];
        final boolean[] isNew = new boolean[]{true};
        JavaSource implBeanJavaSrc = JavaSource.forFileObject(implBeanFo);
        CancellableTask<CompilationController> task = 
            new CancellableTask<CompilationController>() 
            {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(
                        controller);
                AnnotationMirror handlerAnnotation =JaxWsUtils.getAnnotation(typeElement, 
                        "javax.jws.HandlerChain"); //NOI18N
                if (handlerAnnotation != null) {
                    isNew[0] = false;
                    Map<? extends ExecutableElement, 
                            ? extends AnnotationValue> expressions = 
                                handlerAnnotation.getElementValues();
                    for (Entry<? extends ExecutableElement, 
                            ? extends AnnotationValue> entry : expressions.entrySet()) {
                        ExecutableElement ex = entry.getKey();
                        if (ex.getSimpleName().contentEquals("file")) {   //NOI18N
                            handlerFileName[0] = (String) entry.getValue().getValue();
                            break;
                        }
                    }
                }
            }
            @Override
            public void cancel() {
            }
        };
        try {
            implBeanJavaSrc.runUserActionTask(task, true);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }

        if (!isNew[0] && handlerFileName[0] != null) {
            try {
                // look for handlerFile
                FileObject parent = implBeanFo.getParent();
                File parentFile = FileUtil.toFile(parent);

                File file = new File(parentFile, handlerFileName[0]);
                if (file.exists()) {
                    file = file.getCanonicalFile();
                    handlerFO = FileUtil.toFileObject(file);
                }
                if (handlerFO != null) {
                    try {
                        handlerChains = HandlerChainsProvider.getDefault().
                            getHandlerChains(handlerFO);
                    } catch (Exception e) {
                        ErrorManager.getDefault().notify(e);
                        return; //TODO handle this
                    }
                    HandlerChain[] handlerChainArray = handlerChains.getHandlerChains();
                    //there is always only one, so get the first one
                    HandlerChain chain = handlerChainArray[0];
                    Handler[] handlers = chain.getHandlers();
                    for (int i = 0; i < handlers.length; i++) {
                        handlerClasses.add(handlers[i].getHandlerClass());
                    }
                } else {
                    //unable to find the handler file, display a warning
                    NotifyDescriptor.Message dialogDesc = new NotifyDescriptor.
                        Message(NbBundle.getMessage(JaxWsNode.class, 
                                "MSG_HANDLER_FILE_NOT_FOUND", handlerFileName), // NOI18N 
                                    NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(dialogDesc);
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        final MessageHandlerPanel panel = new MessageHandlerPanel(project, handlerClasses, true, service.getName());
        String title = NbBundle.getMessage(JaxWsNode.class, "TTL_MessageHandlerPanel");
        DialogDescriptor dialogDesc = new DialogDescriptor(panel, title);
        dialogDesc.setButtonListener(new HandlerButtonListener(panel, handlerChains, handlerFO, implBeanFo, service, isNew[0]));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        dialog.getAccessibleContext().setAccessibleDescription(dialog.getTitle());
        dialog.setVisible(true);
    }

    void refreshImplClass() {
        if (implBeanClass != null) {
            content.remove(implBeanClass);
        }
        implBeanClass = getImplBean();
        content.add(implBeanClass);
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public boolean canCut() {
        return true;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        URL url = new URL(getWsdlURL());
        boolean connectionOK = false;
        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                try {
                    httpConnection.setRequestMethod("GET"); //NOI18N
                    httpConnection.connect();
                    if (HttpURLConnection.HTTP_OK == httpConnection.getResponseCode()) {
                        connectionOK = true;
                    }
                } catch (java.net.ConnectException ex) {
                    //TODO: throw exception here?
                    url = null;
                } finally {
                    httpConnection.disconnect();
                }
                if (!connectionOK) {
                    //TODO: throw exception here?
                    url = null;
                }
            }
        } catch (IOException ex) {
            //TODO: throw exception here?
            url = null;
        }

        return new WebServiceTransferable(new WebServiceReference(url, 
                service.getWsdlUrl() != null ? 
                        service.getServiceName() : service.getName(), 
                            project.getProjectDirectory().getName()));
    }

    private class RefreshServiceImpl implements JaxWsRefreshCookie {

        /**
         * refresh service information obtained from wsdl (when wsdl file was changed)
         */
        public void refreshService(boolean downloadWsdl) {
            if (downloadWsdl) {
                String result = RefreshWsDialog.open(downloadWsdl, 
                        service.getImplementationClass(), service.getWsdlUrl());
                if (RefreshWsDialog.CLOSE.equals(result)) {
                    return;
                }
                if (result.startsWith(RefreshWsDialog.DO_ALL)) {
                    ((JaxWsChildren) getChildren()).refreshKeys(true, true, result.substring(1));
                } else if (result.startsWith(RefreshWsDialog.DOWNLOAD_WSDL)) {
                    ((JaxWsChildren) getChildren()).refreshKeys(true, false, result.substring(1));
                } else if (RefreshWsDialog.REGENERATE_IMPL_CLASS.equals(result)) {
                    ((JaxWsChildren) getChildren()).refreshKeys(false, true, null);
                } else {
                    ((JaxWsChildren) getChildren()).refreshKeys(false, false, null);
                }
            } else {
                String result = RefreshWsDialog.openWithOKButtonOnly(downloadWsdl, 
                        service.getImplementationClass(), service.getWsdlUrl());
                if (RefreshWsDialog.REGENERATE_IMPL_CLASS.equals(result)) {
                    ((JaxWsChildren) getChildren()).refreshKeys(false, true, null);
                } else {
                    ((JaxWsChildren) getChildren()).refreshKeys(false, false, null);
                }
            }
        }
    }

    /** Old way to obtain port number from server instance (using instance properties)
     * 
     * @param instanceProperties
     * @return port number
     */
    private String getPortNumber(InstanceProperties instanceProperties) {
        String portNumber = instanceProperties.getProperty(InstanceProperties.HTTP_PORT_NUMBER);
        if (portNumber == null || portNumber.equals("")) { //NOI18N
            String serverURL = instanceProperties.getProperty(InstanceProperties.URL_ATTR);
            if (serverURL == null) {
                return "8080"; //NOI18N
            } else {
                String port = parseServerURL(serverURL);
                return (port == null ? "8080" : port); //NOI18N
            }
        } else {
            return portNumber;
        }
    }

    private String parseServerURL(String serverURL) {
        int index1 = serverURL.indexOf("http://"); //NOI18N
        if (index1 >= 0) {
            String s = serverURL.substring(index1+7);
            int index2 = s.indexOf(":");
            if (index2 > 0) {
                s = s.substring(index2+1);
                if (s.length() > 0) {
                    StringBuffer buf = new StringBuffer();
                    int i=0;
                    while (Character.isDigit(s.charAt(i)) && i < s.length()) {
                        buf.append(s.charAt(i++));
                    }
                    if (buf.length() > 0) {
                        return buf.toString();
                    }
                }
            }
        }
        return null;
    }

    /** Old way to obtain host name from server instance (using instance properties)
     * 
     * @param instanceProperties
     * @return host name
     */
    private String getHostName(InstanceProperties instanceProperties) {
        String serverUrl = instanceProperties.getProperty(InstanceProperties.URL_ATTR);
        String hostName = "localhost"; //NOI18N
        if (serverUrl != null && serverUrl.indexOf("::") > 0) { //NOI18N
            //NOI18N
            int index1 = serverUrl.indexOf("::"); //NOI18N
            int index2 = serverUrl.lastIndexOf(":"); //NOI18N
            if (index2 > index1 + 2) {
                hostName = serverUrl.substring(index1 + 2, index2);
            }
        }
        return hostName;
    }
    
    private static class ServerContextInfo {
        private String host, port, contextRoot;

        public ServerContextInfo(String host, String port, String contextRoot) {
            this.host = host;
            this.port = port;
            this.contextRoot = contextRoot;
        }


        public String getHost() {
            return host;
        }

        public String getPort() {
            return port;
        }

        public String getContextRoot() {
            return contextRoot;
        }
    }

    private static class ServiceInfo {
        private String serviceName, portName;
        private boolean ejb;

        public void setEjb(boolean ejb) {
            this.ejb = ejb;
        }

        public void setPortName(String portName) {
            this.portName = portName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public boolean isEjb() {
            return ejb;
        }

        public String getPortName() {
            return portName;
        }

        public String getServiceName() {
            return serviceName;
        }
    }

}
