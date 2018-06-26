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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponentFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
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

    public JSFVersion getVersion() {
        String namespaceURI = getRootComponent().getPeer().getNamespaceURI();
        JSFVersion version = JSFVersion.JSF_1_1;
        if (JSFConfigQNames.JSF_2_2_NS.equals(namespaceURI)
                && (getRootComponent().getVersion().equals("2.2") //NOI18N
                || checkSchemaLocation(
                    getRootComponent().getPeer(),
                    "http://java.sun.com/xml/ns/javaee/web-facesconfig_2_2.xsd"))) { //NOI18N
            version = JSFVersion.JSF_2_2;
        } else if (JSFConfigQNames.JSF_2_1_NS.equals(namespaceURI)
                && (getRootComponent().getVersion().equals("2.1") //NOI18N
                || checkSchemaLocation(
                    getRootComponent().getPeer(),
                    "http://java.sun.com/xml/ns/javaee/web-facesconfig_2_1.xsd"))) { //NOI18N
            version = JSFVersion.JSF_2_1;
        } else if (JSFConfigQNames.JSF_2_0_NS.equals(namespaceURI)
                && (getRootComponent().getVersion().equals("2.0") //NOI18N
                || checkSchemaLocation(
                    getRootComponent().getPeer(),
                    "http://java.sun.com/xml/ns/javaee/web-facesconfig_2_0.xsd"))) { //NOI18N
            version = JSFVersion.JSF_2_0;
        } else if (JSFConfigQNames.JSF_1_2_NS.equals(namespaceURI)) {
            version = JSFVersion.JSF_1_2;
        }
        return version;
    }

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
