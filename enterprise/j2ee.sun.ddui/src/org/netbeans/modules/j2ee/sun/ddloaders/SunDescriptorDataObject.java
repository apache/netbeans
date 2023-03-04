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

package org.netbeans.modules.j2ee.sun.ddloaders;

import java.util.logging.Level;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.xml.sax.InputSource;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.impl.RootInterfaceImpl;
import org.netbeans.modules.glassfish.eecommon.api.config.J2EEBaseVersion;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.appclient.SunAppClientOverviewMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.EnvironmentMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.SecurityRoleMappingMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.EjbMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.SunEjbOverviewMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.JmsMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.ServletMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.SunWebOverviewMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.WebServiceMultiViewElement;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
import org.netbeans.modules.xml.multiview.XmlMultiViewElement;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Represents a DD object in the Repository.
 *
 * @author pfiala
 * @author Peter Williams
 */
@MIMEResolver.Registration(
    displayName="org.netbeans.modules.j2ee.sun.share.Bundle#SunResolver",
    position=350,
    resource="../share/sun-dd-mime-resolver.xml"
)
public class SunDescriptorDataObject extends DDMultiViewDataObject
{

    @Override
    protected int getXMLMultiViewIndex() {
        return xmlIndex; // 6; //super.getXMLMultiViewIndex();
    }

    /**
     * Property name for documentDTD property
     */
    public static final String PROP_DOCUMENT_DTD = "documentDTD";   // NOI18N
    
    // Serialization
    private static final long serialVersionUID = 8957663189355029479L;
    
    
    private final Object proxyMonitor = new Object();
    private volatile RootInterfaceImpl ddRootProxy;
    private PropertyChangeListener ddRootChangeListener;
    private DDType descriptorType;
    private final int xmlIndex;
    
    public SunDescriptorDataObject(FileObject pf, SunDescriptorDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        
        descriptorType = DDType.getDDType(pf.getNameExt());
        
        // XML Validation cookies
        InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        CookieSet set = getCookieSet();
        set.add(validateCookie);
        if (null == descriptorType) {
            xmlIndex = 0;
        } else if (descriptorType.equals(DDType.DD_GF_WEB_APP) || 
                descriptorType.equals(DDType.DD_SUN_WEB_APP)) {
            xmlIndex = 6;
        } else if (descriptorType.equals(DDType.DD_GF_APPLICATION) || 
                descriptorType.equals(DDType.DD_SUN_APPLICATION)) {
            xmlIndex = 1;
        } else if (descriptorType.equals(DDType.DD_GF_EJB_JAR) || 
                descriptorType.equals(DDType.DD_SUN_EJB_JAR)) {
            xmlIndex = 5;
        } else if (descriptorType.equals(DDType.DD_GF_APP_CLIENT) || 
                descriptorType.equals(DDType.DD_SUN_APP_CLIENT)) {
            xmlIndex = 4;
        } else {
            xmlIndex = 0;
        }
    }
    
    /** Returns what the module type ought to be for this particular descriptor 
     *  file (e.g. if someone puts sun-ejb-jar.xml into a web module folder, this
     *  api will return J2eeModule.Type.EJB for this dataobject even though j2eeserver
     *  will return J2eeModule.Type.WAR for the project's module type.
     */
    public J2eeModule.Type getModuleType() {
        // FIXME What should this return for a sun-resource.xml file?  Right, it returns null.
        return descriptorType.getEditorModuleType();
    }    
    
    private Project getProject() {
        return FileOwnerQuery.getOwner(getPrimaryFile());
    }
    
    public FileObject getProjectDirectory() {
        Project project = getProject();
        return project == null ? null : project.getProjectDirectory();
    }
    
    public ASDDVersion getASDDVersion() {
        // !PW FIXME default version ought to be current project server version,
        // if any, otherwise, current installed server, if any.
        return DDProvider.getASDDVersion(getDDModel(), ASDDVersion.SUN_APPSERVER_8_1);
    }
    
    /** Ask the configuration (if we have one) what the J2EE/JavaEE version of
     *  this project is.
     * 
     * @return J2EE version object for this project or null if it cannot be determined.
     *   (ie no configuration for some reason or bad module type, etc.)
     */
    public J2EEBaseVersion getJ2eeModuleVersion() {
        File fileKey = FileUtil.toFile(getPrimaryFile());
        if(fileKey != null) {
            // Find configuration via key derived from primary file.
            if("sun-cmp-mappings.xml".equals(fileKey.getName())) {
                fileKey = new File(fileKey.getParentFile(), "sun-ejb-jar.xml");
            }
        }
        // If we can't locate the configuration (either not there or no valid key)
        // then just return null version.  Nothing else we can do.
        GlassfishConfiguration config = GlassfishConfiguration.getConfiguration(fileKey);
        return (config != null) ? config.getJ2eeVersion() : null;
    }
    
    public RootInterface getDDRoot() {
        return getDDRootImpl(true);
    }
    
    private RootInterface getDDRootImpl(final boolean notify) {
        RootInterface localProxy = null;
        synchronized (proxyMonitor) {
            if (ddRootProxy == null) {
                try {
                    parseDocument();
                } catch (IOException ex) {
                    if(notify) {
                        notifyError(ex);
                    } else {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
            localProxy = ddRootProxy != null ? ddRootProxy.getRootInterface() : null;
        }
        return localProxy;
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new SunDescriptorDataNode(this);
    }
    
    /**
     * gets the Icon Base for node delegate when parser accepts the xml document as valid
     * <p/>
     * PENDING: move into node
     *
     * @return Icon Base for node delegate
     */
    protected String getIconBaseForValidDocument() {
        return Utils.ICON_BASE_DD_VALID;
    }
    
    /**
     * gets the Icon Base for node delegate when parser finds error(s) in xml document
     *
     * @return Icon Base for node delegate
     *         <p/>
     *         PENDING: move into node
     */
    protected String getIconBaseForInvalidDocument() {
        return Utils.ICON_BASE_DD_INVALID; // NOI18N
    }
    
    @Override
    protected DataObject handleCopy(DataFolder f) throws IOException {
        DataObject dataObject = super.handleCopy(f);
        try {
            dataObject.setValid(false);
        } catch (PropertyVetoException e) {
            // should not occur
        }
        return dataObject;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
        
    @Override
    protected void parseDocument() throws IOException {
        DDProvider ddProvider = DDProvider.getDefault();
        SAXParseException saxEx = null;
        synchronized (proxyMonitor) {
            if(ddRootProxy == null || !ddRootProxy.hasOriginal()) {
                try {
                    RootInterfaceImpl newDDRoot = (RootInterfaceImpl) ddProvider.getDDRoot(getPrimaryFile());
                    if(ddRootProxy != null && ddRootChangeListener != null) {
                        ddRootProxy.removePropertyChangeListener(ddRootChangeListener);
                    }
                    ddRootProxy = newDDRoot;
                    if(ddRootProxy != null) {
                        if(ddRootChangeListener == null) {
                            ddRootChangeListener = new SunDDPropertyChangeListener();
                        }
                        ddRootProxy.addPropertyChangeListener(ddRootChangeListener);
                    }
                } catch(IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            } else {
                ddProvider.merge(ddRootProxy.getRootInterface(), createReader());
            }
            saxEx = ddRootProxy != null ? ddRootProxy.getError() : new SAXParseException("No proxy object found created by parser.", null);
        }
                       
        setSaxError(saxEx);
    }
    
    @Override
    protected void validateDocument() throws IOException {
        try {
            RootInterfaceImpl proxyImpl = (RootInterfaceImpl) DDProvider.getDefault().getDDRoot(createReader());
            if (null != proxyImpl) {
                setSaxError(proxyImpl.getError());
            }
        } catch(Schema2BeansException ex) {
            setSaxError(new SAXException(ex));
        } catch(Schema2BeansRuntimeException ex) {
            setSaxError(new SAXException(ex));
        } catch(SAXException ex) {
            setSaxError(ex);
        }
    }
    
    @Override
    protected RootInterface getDDModel() {
        return getDDRootImpl(false);
    }
    
    @Override
    public boolean isDocumentParseable() {
        RootInterface ddRoot = getDDRoot();
        return ddRoot != null ? (ddRoot.getStatus() != RootInterface.STATE_INVALID_UNPARSABLE) : false;
    }
    
    @Override
    protected String getPrefixMark() {
        // Not used anywhere at this time (ever?) so no point in writing the code
        // to figure this out (lookup table, etc.)
        return "<notused";
    }
    
    /** Used to detect if data model has already been created or not.
     * Method is called before switching to the design view from XML view when the document isn't parseable.
     */
    @Override
    protected boolean isModelCreated() {
        boolean result = false;
        synchronized (proxyMonitor) {
            result = ddRootProxy != null && ddRootProxy.hasOriginal();
        }
        return result;
    }
    
    @Override
    public void showElement(final Object element) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                final SectionNodeView sectionView =
                        (SectionNodeView) SunDescriptorDataObject.this.getActiveMVElement().getSectionView();
                final Node root = sectionView.getRoot();
                final SectionNode node = ((SectionNode) root.getChildren().getNodes()[0]).getNodeForElement(element);
                if (node != null) {
                    sectionView.openPanel(node);
                    ((SectionNodeInnerPanel) node.getSectionNodePanel().getInnerPanel()).focusData(element);
                }
            }
        });
    }
    
    
    /** Enable to access Active element
     * 
     * @return toolbar element to use for this editor.
     */
    public ToolBarMultiViewElement getActiveMVElement() {
        return (ToolBarMultiViewElement) super.getActiveMultiViewElement();
    }
        
    private boolean fireEvent(String oldResourceName, String resourceName, int eventType) {
// TODO what should this do?
//        boolean elementFound = false;
//        String resource;
//        int specificEventType = -1;
//        if (eventType == DDChangeEvent.EJB_CHANGED) {
//            resource = oldResourceName;
//        } else {
//            resource = resourceName;
//        }
//        Ejb ejb = getEjbFromEjbClass(resource);
//        
//        if (ejb != null) {
//            if (eventType == DDChangeEvent.EJB_CHANGED) {
//                specificEventType = DDChangeEvent.EJB_CLASS_CHANGED;
//            } else {
//                specificEventType = DDChangeEvent.EJB_CLASS_DELETED;
//            }
//            elementFound = true;
//        }
//        
//        if (!elementFound) {
//            int interfaceType = getBeanInterfaceType(resource);
//            
//            if (interfaceType > 0) {
//                specificEventType =
//                        getSpecificEvent(eventType, interfaceType);
//                elementFound = true;
//            }
//        }
//        if (elementFound) {
//            assert(specificEventType > 0);
//            DDChangeEvent ddEvent =
//                    new DDChangeEvent(this, this, oldResourceName,
//                    resourceName, specificEventType);
//            deploymentChange(ddEvent);
//        }
//        return elementFound;
        return false;
    }
    
    private static class SunDDPropertyChangeListener implements PropertyChangeListener {
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
// TODO what should this do?
//            if (EjbJar.PROPERTY_STATUS.equals(evt.getPropertyName())) {
//                return;
//            }
//            Object source = evt.getSource();
//            if (source instanceof EnterpriseBeans) {
//                Object oldValue = evt.getOldValue();
//                Object newValue = evt.getNewValue();
//                if ((oldValue instanceof Entity || newValue instanceof Entity)) {
//                    entityHelperMap.keySet().retainAll(Arrays.asList(((EnterpriseBeans) source).getEntity()));
//                } else if ((oldValue instanceof Session || newValue instanceof Session)) {
//                    sessionHelperMap.keySet().retainAll(Arrays.asList(((EnterpriseBeans) source).getSession()));
//                }
//            }
        }
    }
    
    @Override
    protected String getEditorMimeType() {
        String mimeTypePrefix = DDType.IPLANET_MIME_TYPE_PREFIX;
        ASDDVersion asDDVersion = getASDDVersion();
        if (ASDDVersion.SUN_APPSERVER_7_0.compareTo(asDDVersion) < 0) {
            mimeTypePrefix = DDType.SUN_MIME_TYPE_PREFIX;
        }
        if (ASDDVersion.SUN_APPSERVER_9_0.compareTo(asDDVersion) < 0 && 
                (descriptorType.equals(DDType.DD_SUN_APP_CLIENT) ||
                descriptorType.equals(DDType.DD_GF_APP_CLIENT))) {
            mimeTypePrefix = DDType.GLASSFISH_MIME_TYPE_PREFIX;
        }
        Logger.getLogger("glassfish-ddui").log(Level.FINE, "{0}{1}", new Object[]{mimeTypePrefix, descriptorType.getDescriptorMimeTypeSuffix()});
        return mimeTypePrefix+descriptorType.getDescriptorMimeTypeSuffix();
    }
    
    // x-web.xml specific MultiViewElement objects
    
    @MultiViewElement.Registration(
        mimeType={ DDType.IPLANET_MIME_TYPE_PREFIX + DDType.WEB_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.WEB_MIME_TYPE_SUFFIX
        },
        iconBase="org/netbeans/modules/j2ee/sun/ddloaders/resources/DDDataIcon.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="multiview_xml_gf_web_over",
        displayName="#CTL_OverviewTabCaption",
        position=1
    )
    public static MultiViewElement createWebOverviewMultiViewElement(Lookup lookup) {
        return new SunWebOverviewMultiViewElement(lookup.lookup(SunDescriptorDataObject.class));
    }
    
    @MultiViewElement.Registration(
        mimeType={ DDType.IPLANET_MIME_TYPE_PREFIX + DDType.WEB_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.WEB_MIME_TYPE_SUFFIX
        },
        iconBase="org/netbeans/modules/j2ee/sun/ddloaders/resources/DDDataIcon.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="multiview_xml_gf_web_servlet",
        displayName="#CTL_ServletsTabCaption",
        position=2
    )
    public static MultiViewElement createServletsMultiViewElement(Lookup lookup) {
        return new ServletMultiViewElement(lookup.lookup(SunDescriptorDataObject.class));
    }
    
    // *-ejb-jar.xml MultiViewElement objects
    
    @MultiViewElement.Registration(
        mimeType={ DDType.IPLANET_MIME_TYPE_PREFIX + DDType.EJB_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.EJB_MIME_TYPE_SUFFIX
        },
        iconBase="org/netbeans/modules/j2ee/sun/ddloaders/resources/DDDataIcon.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="multiview_xml_gf_ejb_overview",
        displayName="#CTL_OverviewTabCaption",
        position=1
    )
    public static MultiViewElement createEjbOveriewMultiViewElement(Lookup lookup) {
        return new SunEjbOverviewMultiViewElement(lookup.lookup(SunDescriptorDataObject.class));
    }

    @MultiViewElement.Registration(
        mimeType={ DDType.IPLANET_MIME_TYPE_PREFIX + DDType.EJB_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.EJB_MIME_TYPE_SUFFIX
        },
        iconBase="org/netbeans/modules/j2ee/sun/ddloaders/resources/DDDataIcon.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="multiview_xml_gf_ejb_ejb",
        displayName="#CTL_EjbTabCaption",
        position=2
    )
    public static MultiViewElement createEjbMultiViewElement(Lookup lookup) {
        return new EjbMultiViewElement(lookup.lookup(SunDescriptorDataObject.class));
    }
    
    // *-app-client.xml MultiViewElement objects

    @MultiViewElement.Registration(
        mimeType=DDType.GLASSFISH_MIME_TYPE_PREFIX + DDType.APP_CLI_MIME_TYPE_SUFFIX,
        iconBase="org/netbeans/modules/j2ee/sun/ddloaders/resources/DDDataIcon.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="multiview_xml_gf_app_cli_overview",
        displayName="#CTL_OverviewTabCaption",
        position=1
    )
    public static MultiViewElement createAppCliOverviewViewElement(Lookup lookup) {
        return new SunAppClientOverviewMultiViewElement(lookup.lookup(SunDescriptorDataObject.class));
    }
    
    // Shared MultiViewElement objects
    
    @MultiViewElement.Registration(
        mimeType={ DDType.IPLANET_MIME_TYPE_PREFIX + DDType.APP_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.APP_MIME_TYPE_SUFFIX,
            DDType.IPLANET_MIME_TYPE_PREFIX + DDType.EJB_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.EJB_MIME_TYPE_SUFFIX,
            DDType.IPLANET_MIME_TYPE_PREFIX + DDType.WEB_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.WEB_MIME_TYPE_SUFFIX
        },
        iconBase="org/netbeans/modules/j2ee/sun/ddloaders/resources/DDDataIcon.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="multiview_xml_gf_shared_security",
        displayName="#CTL_SecurityTabCaption",
        position=3
    )
    public static MultiViewElement createSecurityMultiViewSecurityElement(Lookup lookup) {
        return new SecurityRoleMappingMultiViewElement(lookup.lookup(SunDescriptorDataObject.class));
    }
    
    @MultiViewElement.Registration(
        mimeType={ DDType.SUN_MIME_TYPE_PREFIX + DDType.WEB_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.EJB_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.APP_CLI_MIME_TYPE_SUFFIX,
            DDType.GLASSFISH_MIME_TYPE_PREFIX + DDType.APP_CLI_MIME_TYPE_SUFFIX
        },
        iconBase="org/netbeans/modules/j2ee/sun/ddloaders/resources/DDDataIcon.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="multiview_xml_gf_shared_WS",
        displayName="#CTL_WebServiceTabCaption",
        position=5
    )
    public static MultiViewElement createServicesMultiViewElement(Lookup lookup) {
        return new WebServiceMultiViewElement(lookup.lookup(SunDescriptorDataObject.class));
    }

    @MultiViewElement.Registration(
        mimeType={ DDType.SUN_MIME_TYPE_PREFIX + DDType.WEB_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.EJB_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.APP_CLI_MIME_TYPE_SUFFIX,
            DDType.GLASSFISH_MIME_TYPE_PREFIX + DDType.APP_CLI_MIME_TYPE_SUFFIX
        },
        iconBase="org/netbeans/modules/j2ee/sun/ddloaders/resources/DDDataIcon.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="multiview_xml_gf_shared_jms",
        displayName="#CTL_JmsTabCaption",
        position=10
    )
    public static MultiViewElement createJmsMultiViewElement(Lookup lookup) {
        return new JmsMultiViewElement(lookup.lookup(SunDescriptorDataObject.class));
    }
    
    @MultiViewElement.Registration(
        mimeType={ DDType.IPLANET_MIME_TYPE_PREFIX + DDType.WEB_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.WEB_MIME_TYPE_SUFFIX,
            DDType.IPLANET_MIME_TYPE_PREFIX + DDType.APP_CLI_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.APP_CLI_MIME_TYPE_SUFFIX,
            DDType.GLASSFISH_MIME_TYPE_PREFIX + DDType.APP_CLI_MIME_TYPE_SUFFIX
        },
        iconBase="org/netbeans/modules/j2ee/sun/ddloaders/resources/DDDataIcon.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="multiview_xml_gf_shared_environment",
        displayName="#CTL_EnvTabCaption",
        position=15
    )
    public static MultiViewElement createEnvMultiViewEnvironmentElement(Lookup lookup) {
        return new EnvironmentMultiViewElement(lookup.lookup(SunDescriptorDataObject.class));
    }

    @MultiViewElement.Registration(
        mimeType={ DDType.SUN_MIME_TYPE_PREFIX + DDType.WEB_MIME_TYPE_SUFFIX, 
            DDType.IPLANET_MIME_TYPE_PREFIX + DDType.WEB_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.EJB_MIME_TYPE_SUFFIX,
            DDType.IPLANET_MIME_TYPE_PREFIX + DDType.EJB_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.APP_MIME_TYPE_SUFFIX,
            DDType.IPLANET_MIME_TYPE_PREFIX + DDType.APP_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.APP_CLI_MIME_TYPE_SUFFIX, 
            DDType.IPLANET_MIME_TYPE_PREFIX + DDType.APP_CLI_MIME_TYPE_SUFFIX,
            DDType.GLASSFISH_MIME_TYPE_PREFIX + DDType.APP_CLI_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.CMP_MIME_TYPE_SUFFIX, 
            DDType.IPLANET_MIME_TYPE_PREFIX + DDType.CMP_MIME_TYPE_SUFFIX,
            DDType.SUN_MIME_TYPE_PREFIX + DDType.RSRC_MIME_TYPE_SUFFIX, 
            DDType.IPLANET_MIME_TYPE_PREFIX + DDType.RSRC_MIME_TYPE_SUFFIX
        },
        iconBase="org/netbeans/modules/j2ee/sun/ddloaders/resources/DDDataIcon.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="multiview_xml_xml",
        displayName="#CTL_SourceTabCaption",
        position=20
    )
    public static XmlMultiViewElement createXmlMultiViewElement(Lookup lookup) {
        return new XmlMultiViewElement(lookup.lookup(SunDescriptorDataObject.class));
    }
    
        
        
}
