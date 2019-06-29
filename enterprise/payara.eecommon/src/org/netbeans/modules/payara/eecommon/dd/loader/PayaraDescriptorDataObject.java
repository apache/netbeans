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

package org.netbeans.modules.payara.eecommon.dd.loader;

import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.impl.RootInterfaceImpl;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.appclient.SunAppClientOverviewMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.EnvironmentMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.SecurityRoleMappingMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.EjbMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.SunEjbOverviewMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.JmsMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.ServletMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.SunWebOverviewMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.WebServiceMultiViewElement;
import static org.netbeans.modules.payara.eecommon.dd.loader.PayaraDDType.PAYARA_APP_CLI_MIME_TYPE;
import static org.netbeans.modules.payara.eecommon.dd.loader.PayaraDDType.PAYARA_EJB_MIME_TYPE;
import static org.netbeans.modules.payara.eecommon.dd.loader.PayaraDDType.PAYARA_WEB_MIME_TYPE;
import static org.netbeans.modules.payara.eecommon.dd.loader.PayaraDescriptorDataObject.DD_ACTION_PATH;
import static org.netbeans.modules.payara.eecommon.dd.loader.PayaraDescriptorDataObject.DD_ICON;
import static org.netbeans.modules.payara.eecommon.dd.loader.PayaraDescriptorDataObject.DD_MIME_TYPE;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
import org.netbeans.modules.xml.multiview.XmlMultiViewElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.Lookup;
import static org.openide.windows.TopComponent.PERSISTENCE_ONLY_OPENED;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Represents a DD object in the Repository.
 *
 * @author pfiala
 * @author Peter Williams
 * @author Gaurav Gupta
 */
@MIMEResolver.Registration(
        displayName = "Bundle#PayaraResolver",
        resource = "../../dd/resources/payara-dd-mime-resolver.xml",
        position = 3500
)
@DataObject.Registration(
        displayName = "Bundle#PayaraResolver",
        mimeType = DD_MIME_TYPE,
        iconBase = DD_ICON,
        position = 3600
)
@ActionReferences({
    @ActionReference(
            path = DD_ACTION_PATH,
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100
    ),
    @ActionReference(
            path = DD_ACTION_PATH,
            id = @ActionID(category = "System", id = "org.openide.actions.EditAction"),
            position = 200
    ),
    @ActionReference(
            path = DD_ACTION_PATH,
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 300,
            separatorAfter = 400
    ),
    @ActionReference(
            path = DD_ACTION_PATH,
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 500
    ),
    @ActionReference(
            path = DD_ACTION_PATH,
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 600
    ),
    @ActionReference(
            path = DD_ACTION_PATH,
            id = @ActionID(category = "Edit", id = "org.openide.actions.PasteAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = DD_ACTION_PATH,
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 900
    ),
    @ActionReference(
            path = DD_ACTION_PATH,
            id = @ActionID(category = "Edit", id = "org.openide.actions.RenameAction"),
            position = 950,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = DD_ACTION_PATH,
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = DD_ACTION_PATH,
            id = @ActionID(category = "XML", id = "org.netbeans.modules.xml.tools.actions.CheckAction"),
            position = 1300
    ),
    @ActionReference(
            path = DD_ACTION_PATH,
            id = @ActionID(category = "XML", id = "org.netbeans.modules.xml.tools.actions.ValidateAction"),
            position = 1400,
            separatorAfter = 1500
    ),
    @ActionReference(
            path = DD_ACTION_PATH,
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1600
    ),
    @ActionReference(
            path = DD_ACTION_PATH,
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1700
    )
})
public class PayaraDescriptorDataObject extends SunDescriptorDataObject {

    public static final String DD_ACTION_PATH = "Loaders/text/x-dd-payara/Actions";

    public static final String DD_ICON = "org/netbeans/modules/payara/eecommon/dd/resources/DDDataIcon.gif";

    public static final String DD_MIME_TYPE = "text/x-dd-payara-web+xml";

    @Override
    protected int getXMLMultiViewIndex() {
        if (xmlIndex == 0) {
            return super.getXMLMultiViewIndex();
        }
        return xmlIndex;
    }

    private final Object proxyMonitor = new Object();
    private volatile RootInterfaceImpl ddRootProxy;
    private final PayaraDDType descriptorType;
    private final int xmlIndex;

    public PayaraDescriptorDataObject(FileObject pf, PayaraDescriptorDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        descriptorType = PayaraDDType.getDDType(pf.getNameExt());
        if (descriptorType != null && descriptorType.equals(PayaraDDType.DD_PAYARA_WEB_APP)) {
            xmlIndex = 6;
        } else {
            xmlIndex = 0;
        }
    }

    @Override
    public J2eeModule.Type getModuleType() {
        return descriptorType.getEditorModuleType();
    }

    @Override
    public RootInterface getDDRoot() {
        return getDDRootImpl(true);
    }

    private RootInterface getDDRootImpl(final boolean notify) {
        RootInterface localProxy;
        synchronized (proxyMonitor) {
            if (ddRootProxy == null) {
                try {
                    parseDocument();
                } catch (IOException ex) {
                    if (notify) {
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
    protected void parseDocument() throws IOException {
        PayaraDDProvider ddProvider = PayaraDDProvider.getDefault();
        SAXParseException saxEx;
        synchronized (proxyMonitor) {
            if (ddRootProxy == null || !ddRootProxy.hasOriginal()) {
                try {
                    RootInterfaceImpl newDDRoot = (RootInterfaceImpl) ddProvider.getDDRoot(getPrimaryFile());
                    ddRootProxy = newDDRoot;
                } catch (IOException ex) {
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
            RootInterfaceImpl proxyImpl = (RootInterfaceImpl) PayaraDDProvider.getDefault().getDDRoot(createReader());
            if (null != proxyImpl) {
                setSaxError(proxyImpl.getError());
            }
        } catch (Schema2BeansException | Schema2BeansRuntimeException ex) {
            setSaxError(new SAXException(ex));
        } catch (SAXException ex) {
            setSaxError(ex);
        }
    }

    @Override
    protected RootInterface getDDModel() {
        return getDDRootImpl(false);
    }

    @Override
    protected String getEditorMimeType() {
        return PayaraDDType.PAYARA_MIME_TYPE_PREFIX + descriptorType.getDescriptorMimeTypeSuffix();
    }

    // x-web.xml specific MultiViewElement objects
    @MultiViewElement.Registration(
            mimeType = PAYARA_WEB_MIME_TYPE,
            iconBase = DD_ICON,
            persistenceType = PERSISTENCE_ONLY_OPENED,
            preferredID = "multiview_xml_payara_web_over",
            displayName = "#CTL_OverviewTabCaption",
            position = 1
    )
    public static MultiViewElement createWebOverviewMultiViewElement(Lookup lookup) {
        return new SunWebOverviewMultiViewElement(lookup.lookup(PayaraDescriptorDataObject.class));
    }

    @MultiViewElement.Registration(
            mimeType = PAYARA_WEB_MIME_TYPE,
            iconBase = DD_ICON,
            persistenceType = PERSISTENCE_ONLY_OPENED,
            preferredID = "multiview_xml_payara_web_servlet",
            displayName = "#CTL_ServletsTabCaption",
            position = 2
    )
    public static MultiViewElement createServletsMultiViewElement(Lookup lookup) {
        return new ServletMultiViewElement(lookup.lookup(PayaraDescriptorDataObject.class));
    }

    // *-ejb-jar.xml MultiViewElement objects
    @MultiViewElement.Registration(
            mimeType = PAYARA_EJB_MIME_TYPE,
            iconBase = DD_ICON,
            persistenceType = PERSISTENCE_ONLY_OPENED,
            preferredID = "multiview_xml_payara_ejb_overview",
            displayName = "#CTL_OverviewTabCaption",
            position = 1
    )
    public static MultiViewElement createEjbOveriewMultiViewElement(Lookup lookup) {
        return new SunEjbOverviewMultiViewElement(lookup.lookup(PayaraDescriptorDataObject.class));
    }

    @MultiViewElement.Registration(
            mimeType = PAYARA_EJB_MIME_TYPE,
            iconBase = DD_ICON,
            persistenceType = PERSISTENCE_ONLY_OPENED,
            preferredID = "multiview_xml_payara_ejb_ejb",
            displayName = "#CTL_EjbTabCaption",
            position = 2
    )
    public static MultiViewElement createEjbMultiViewElement(Lookup lookup) {
        return new EjbMultiViewElement(lookup.lookup(PayaraDescriptorDataObject.class));
    }

    // *-app-client.xml MultiViewElement objects
    @MultiViewElement.Registration(
            mimeType = PAYARA_APP_CLI_MIME_TYPE,
            iconBase = DD_ICON,
            persistenceType = PERSISTENCE_ONLY_OPENED,
            preferredID = "multiview_xml_payara_app_cli_overview",
            displayName = "#CTL_OverviewTabCaption",
            position = 1
    )
    public static MultiViewElement createAppCliOverviewViewElement(Lookup lookup) {
        return new SunAppClientOverviewMultiViewElement(lookup.lookup(PayaraDescriptorDataObject.class));
    }

    // Shared MultiViewElement objects
    @MultiViewElement.Registration(
            mimeType = PAYARA_WEB_MIME_TYPE,
            iconBase = DD_ICON,
            persistenceType = PERSISTENCE_ONLY_OPENED,
            preferredID = "multiview_xml_payara_shared_security",
            displayName = "#CTL_SecurityTabCaption",
            position = 3
    )
    public static MultiViewElement createSecurityMultiViewSecurityElement(Lookup lookup) {
        return new SecurityRoleMappingMultiViewElement(lookup.lookup(PayaraDescriptorDataObject.class));
    }

    @MultiViewElement.Registration(
            mimeType = PAYARA_WEB_MIME_TYPE,
            iconBase = DD_ICON,
            persistenceType = PERSISTENCE_ONLY_OPENED,
            preferredID = "multiview_xml_payara_shared_WS",
            displayName = "#CTL_WebServiceTabCaption",
            position = 5
    )
    public static MultiViewElement createServicesMultiViewElement(Lookup lookup) {
        return new WebServiceMultiViewElement(lookup.lookup(PayaraDescriptorDataObject.class));
    }

    @MultiViewElement.Registration(
            mimeType = PAYARA_WEB_MIME_TYPE,
            iconBase = DD_ICON,
            persistenceType = PERSISTENCE_ONLY_OPENED,
            preferredID = "multiview_xml_payara_shared_jms",
            displayName = "#CTL_JmsTabCaption",
            position = 10
    )
    public static MultiViewElement createJmsMultiViewElement(Lookup lookup) {
        return new JmsMultiViewElement(lookup.lookup(PayaraDescriptorDataObject.class));
    }

    @MultiViewElement.Registration(
            mimeType = PAYARA_WEB_MIME_TYPE,
            iconBase = DD_ICON,
            persistenceType = PERSISTENCE_ONLY_OPENED,
            preferredID = "multiview_xml_payara_shared_environment",
            displayName = "#CTL_EnvTabCaption",
            position = 15
    )
    public static MultiViewElement createEnvMultiViewEnvironmentElement(Lookup lookup) {
        return new EnvironmentMultiViewElement(lookup.lookup(PayaraDescriptorDataObject.class));
    }

    @MultiViewElement.Registration(
            mimeType = PAYARA_WEB_MIME_TYPE,
            iconBase = DD_ICON,
            persistenceType = PERSISTENCE_ONLY_OPENED,
            preferredID = "multiview_xml_payara_xml",
            displayName = "#CTL_SourceTabCaption",
            position = 20
    )
    public static XmlMultiViewElement createXmlMultiViewElement(Lookup lookup) {
        return new XmlMultiViewElement(lookup.lookup(PayaraDescriptorDataObject.class));
    }

}
