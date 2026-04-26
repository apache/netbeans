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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.web.jsf.JSFCatalog;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponentFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Petr Pisl, ads
 */
public class JSFConfigModelImpl extends AbstractDocumentModel<JSFConfigComponent> implements JSFConfigModel {

    private static final Logger LOGGER = Logger.getLogger(JSFConfigModelImpl.class.getName());

    private FacesConfig facesConfig;
    private final JSFConfigComponentFactory componentFactory;
    private JsfModel myMetaModel;

    /** Creates a new instance of JSFConfigModelImpl */
    public JSFConfigModelImpl(ModelSource source) {
        super(source);
        componentFactory = new JSFConfigComponentFactoryImpl(this);
    }

    /** Creates a new instance of JSFConfigModelImpl */
    public JSFConfigModelImpl(ModelSource source, JsfModel model ) {
        this( source );
        myMetaModel = model;
    }

    public JSFConfigComponent createRootComponent(Element root) {
        FacesConfig newFacesConfig = (FacesConfig) getFactory().create(root, null);
        if (newFacesConfig != null) {
            facesConfig = newFacesConfig;
        }
        return newFacesConfig;
    }

    protected ComponentUpdater<JSFConfigComponent> getComponentUpdater() {
        return new SyncUpdateVisitor();
    }

    public FacesConfig getRootComponent() {
        LOGGER.fine("getRootComponent()");
        return facesConfig;
    }

    public JSFConfigComponent createComponent(JSFConfigComponent parent, Element element) {
        return getFactory().create(element, parent);
    }

    public JSFConfigComponentFactory getFactory() {
        return componentFactory;
    }

    @Override
    public JsfVersion getVersion() {
        String namespaceURI = getRootComponent().getPeer().getNamespaceURI();
        Element element = getRootComponent().getPeer();
        String componentVersion = getRootComponent().getVersion();
        JsfVersion jsfVersion = JsfVersion.JSF_1_1;
        if (JSFConfigQNames.JSF_JAKARTA_NS.equals(namespaceURI)) {
            if (JsfVersion.JSF_5_0.getVersion().equals(componentVersion) || checkSchemaLocation(element, JSFCatalog.JSF_5_0)) {
                jsfVersion = JsfVersion.JSF_5_0;
            } else if (JsfVersion.JSF_4_1.getVersion().equals(componentVersion) || checkSchemaLocation(element, JSFCatalog.JSF_4_1)) {
                jsfVersion = JsfVersion.JSF_4_1;
            } else if (JsfVersion.JSF_4_0.getVersion().equals(componentVersion) || checkSchemaLocation(element, JSFCatalog.JSF_4_0)) {
                jsfVersion = JsfVersion.JSF_4_0;
            } else if (JsfVersion.JSF_3_0.getVersion().equals(componentVersion) || checkSchemaLocation(element, JSFCatalog.JSF_3_0)) {
                jsfVersion = JsfVersion.JSF_3_0;
            }
        } else if (JSFConfigQNames.JSF_ORACLE_NS.equals(namespaceURI)) {
            if (JsfVersion.JSF_2_3.getVersion().equals(componentVersion) || checkSchemaLocation(element, JSFCatalog.JSF_2_3)) {
                jsfVersion = JsfVersion.JSF_2_3;
            } else if (JsfVersion.JSF_2_2.getVersion().equals(componentVersion) || checkSchemaLocation(element, JSFCatalog.JSF_2_2)) {
                jsfVersion = JsfVersion.JSF_2_2;
            }
        } else if (JSFConfigQNames.JSF_SUN_NS.equals(namespaceURI)) {
            if (JsfVersion.JSF_2_1.getVersion().equals(componentVersion) || checkSchemaLocation(element, JSFCatalog.JSF_2_1)) {
                jsfVersion = JsfVersion.JSF_2_1;
            } else if (JsfVersion.JSF_2_0.getVersion().equals(componentVersion) || checkSchemaLocation(element, JSFCatalog.JSF_2_0)) {
                jsfVersion = JsfVersion.JSF_2_0;
            } else if (JsfVersion.JSF_1_2.getVersion().equals(componentVersion) || checkSchemaLocation(element, JSFCatalog.JSF_1_2)) {
                jsfVersion = JsfVersion.JSF_1_2;
            }
        }
        return jsfVersion;
    }

    @Override
    public Set<QName> getQNames() {
        return JSFConfigQNames.getMappedQNames(getVersion());
    }

    private boolean checkSchemaLocation(Element rootElement, String schemaLocation) {
        NamedNodeMap map = rootElement.getAttributes();
        String prefix = null;
        for (int i = 0; i < map.getLength(); i++) {
            Node node = map.item(i);
            Attr attr = (Attr) node;
            String value = attr.getValue();
            if (value.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
                String prefixedAttr = attr.getName();
                if (prefixedAttr.indexOf(":") != -1) { //NOI18N
                    prefix = prefixedAttr.substring(prefixedAttr.indexOf(":") + 1); //NOI18N
                }
                break;
            }
        }
        String schemaLocationAttr = "schemaLocation"; //NOI18N
        if (prefix != null && prefix.length() > 0) {
            schemaLocationAttr = prefix + ":" + schemaLocationAttr; //NOI18N
        }
        Attr attr = (Attr) map.getNamedItem(schemaLocationAttr);
        if (attr == null) {
            return false;
        }
        String value = attr.getValue();
        StringTokenizer tokenizer = new StringTokenizer(value);
        String location = null;
        while (tokenizer.hasMoreTokens()) {
            location = tokenizer.nextToken();
        }
        return schemaLocation.equals(location);
    }

}
