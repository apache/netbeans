/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.dd.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.schema2beans.AttrProp;
import org.netbeans.modules.schema2beans.NullEntityResolver;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Petr Hejl
 */
public final class WebApplicationModel extends BaseDescriptorModel {

    private static final Pattern SCHEMA_1031 = Pattern.compile("http://xmlns\\.oracle\\.com/weblogic/weblogic-web-app/1\\.[0-3]/weblogic-web-app\\.xsd"); // NOI18N

    private static final Pattern SCHEMA_1211 = Pattern.compile("http://xmlns\\.oracle\\.com/weblogic/weblogic-web-app/1\\.[4-7]/weblogic-web-app\\.xsd"); // NOI18N

    private static final Pattern SCHEMA_1221 = Pattern.compile("http://xmlns\\.oracle\\.com/weblogic/weblogic-web-app/1\\.[8]/weblogic-web-app\\.xsd"); // NOI18N

    private final WeblogicWebApp bean;

    private WebApplicationModel(WeblogicWebApp bean) {
        super(bean);
        this.bean = bean;
    }
    
    public static WebApplicationModel forFile(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        try {
            return forInputStream(is);
        } finally {
            is.close();
        }
    }
    
    public static WebApplicationModel forInputStream(InputStream is) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        
        Document doc;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(NullEntityResolver.newInstance());
            doc = builder.parse(is);
        } catch (SAXException ex) {
            throw new RuntimeException(NbBundle.getMessage(WebApplicationModel.class, "MSG_CantCreateXMLDOMDocument"), ex);
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(NbBundle.getMessage(WebApplicationModel.class, "MSG_CantCreateXMLDOMDocument"), ex);
        }        

        String ns = doc.getDocumentElement().getNamespaceURI();
        if ("http://xmlns.oracle.com/weblogic/weblogic-web-app".equals(ns)) { // NOI18N
            String value = doc.getDocumentElement().getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"); // NOI18N
            if (SCHEMA_1031.matcher(value).matches()) {
                return new WebApplicationModel(org.netbeans.modules.j2ee.weblogic9.dd.web1031.WeblogicWebApp.createGraph(doc));
            } else if (SCHEMA_1211.matcher(value).matches()) {
                return new WebApplicationModel(org.netbeans.modules.j2ee.weblogic9.dd.web1211.WeblogicWebApp.createGraph(doc));
            } else if (SCHEMA_1221.matcher(value).matches()) {
                return new WebApplicationModel(org.netbeans.modules.j2ee.weblogic9.dd.web1221.WeblogicWebApp.createGraph(doc));
            } else {
                return new WebApplicationModel(org.netbeans.modules.j2ee.weblogic9.dd.web1221.WeblogicWebApp.createGraph(doc));
            }
        } else if ("http://www.bea.com/ns/weblogic/weblogic-web-app".equals(ns)) { // NOI18N
            return new WebApplicationModel(org.netbeans.modules.j2ee.weblogic9.dd.web1030.WeblogicWebApp.createGraph(doc));
        }
        return new WebApplicationModel(org.netbeans.modules.j2ee.weblogic9.dd.web90.WeblogicWebApp.createGraph(doc));  
    }
    
    public static WebApplicationModel generate(@NullAllowed Version serverVersion) {
        if (serverVersion != null) {
            if (serverVersion.isAboveOrEqual(VERSION_12_2_1)) {
                return generate1221();
            } else if (serverVersion.isAboveOrEqual(VERSION_12_1_1)) {
                return generate1211();
            } else if (serverVersion.isAboveOrEqual(VERSION_10_3_1)) {
                return generate1031();
            } else if (serverVersion.isAboveOrEqual(VERSION_10_3_0)) {
                return generate1030();
            }
        }
        return generate90();
    }

    private static WebApplicationModel generate90() {
        org.netbeans.modules.j2ee.weblogic9.dd.web90.WeblogicWebApp webLogicWebApp = new org.netbeans.modules.j2ee.weblogic9.dd.web90.WeblogicWebApp();
        webLogicWebApp.createAttribute("xmlns:j2ee", "xmlns:j2ee", AttrProp.CDATA | AttrProp.IMPLIED, null, null); // NOI18N
        webLogicWebApp.setAttributeValue("xmlns:j2ee", "http://java.sun.com/xml/ns/j2ee"); // NOI18N
        webLogicWebApp.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicWebApp.setAttributeValue("xsi:schemaLocation", "http://www.bea.com/ns/weblogic/90 http://www.bea.com/ns/weblogic/90/weblogic-web-app.xsd"); // NOI18N
        return new WebApplicationModel(webLogicWebApp);
    }
    
    private static WebApplicationModel generate1030() {
        org.netbeans.modules.j2ee.weblogic9.dd.web1030.WeblogicWebApp webLogicWebApp = new org.netbeans.modules.j2ee.weblogic9.dd.web1030.WeblogicWebApp();
        webLogicWebApp.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicWebApp.setAttributeValue("xsi:schemaLocation", "http://www.bea.com/ns/weblogic/weblogic-web-app http://www.bea.com/ns/weblogic/weblogic-web-app/1.0/weblogic-web-app.xsd"); // NOI18N
        return new WebApplicationModel(webLogicWebApp);
    }

    private static WebApplicationModel generate1031() {
        org.netbeans.modules.j2ee.weblogic9.dd.web1031.WeblogicWebApp webLogicWebApp = new org.netbeans.modules.j2ee.weblogic9.dd.web1031.WeblogicWebApp();
        webLogicWebApp.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicWebApp.setAttributeValue("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd http://xmlns.oracle.com/weblogic/weblogic-web-app http://xmlns.oracle.com/weblogic/weblogic-web-app/1.0/weblogic-web-app.xsd"); // NOI18N
        return new WebApplicationModel(webLogicWebApp);
    }

    private static WebApplicationModel generate1211() {
        org.netbeans.modules.j2ee.weblogic9.dd.web1211.WeblogicWebApp webLogicWebApp = new org.netbeans.modules.j2ee.weblogic9.dd.web1211.WeblogicWebApp();
        webLogicWebApp.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicWebApp.setAttributeValue("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd http://xmlns.oracle.com/weblogic/weblogic-web-app http://xmlns.oracle.com/weblogic/weblogic-web-app/1.4/weblogic-web-app.xsd"); // NOI18N
        return new WebApplicationModel(webLogicWebApp);
    }

    private static WebApplicationModel generate1221() {
        org.netbeans.modules.j2ee.weblogic9.dd.web1221.WeblogicWebApp webLogicWebApp = new org.netbeans.modules.j2ee.weblogic9.dd.web1221.WeblogicWebApp();
        webLogicWebApp.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicWebApp.setAttributeValue("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd http://xmlns.oracle.com/weblogic/weblogic-web-app http://xmlns.oracle.com/weblogic/weblogic-web-app/1.8/weblogic-web-app.xsd"); // NOI18N
        return new WebApplicationModel(webLogicWebApp);
    }
    
    @CheckForNull
    public String getContextRoot() {
        String[] roots = bean.getContextRoot();
        if (roots == null || roots.length < 1) {
            return null;
        }
        return roots[0];
    }
    
    public void setContextRoot(String root) {
        bean.setContextRoot(new String [] {root});
    }
    
    public void setReference(String referenceName, String jndiName) {
        for (ResourceDescriptionType type : bean.getResourceDescription()) {
            String refName = type.getResRefName();
            if (referenceName.equals(refName)) {
                type.setJndiName(jndiName);
                return;
            }
        }
        
        ResourceDescriptionType type = bean.addResourceDescription();
        type.setResRefName(referenceName);
        type.setJndiName(jndiName);
    }
    
    public String getReferenceJndiName(String referenceName) {
        for (ResourceDescriptionType type : bean.getResourceDescription()) {
            String refName = type.getResRefName();
            if (referenceName.equals(refName)) {
                return type.getJndiName();
            }
        }
        return null;
    }
      
    public Set<ServerLibraryDependency> getLibraries() {
        Set<ServerLibraryDependency> ranges = new HashSet<ServerLibraryDependency>();
        for (LibraryRefType libRef : bean.getLibraryRef()) {
            ranges.add(getLibrary(libRef));
        }

        return ranges;
    }

    public void addLibrary(ServerLibraryDependency library) {
        LibraryRefType[] current = bean.getLibraryRef();
        for (LibraryRefType libRef : current) {
            ServerLibraryDependency lib = getLibrary(libRef);
            if (library.equals(lib)) {
                return;
            }
        }
        
        LibraryRefType libRef = bean.addLibraryRef();
        libRef.setLibraryName(library.getName());
        if (library.isExactMatch()) {
            libRef.setExactMatch(library.isExactMatch());
        }
        if (library.getSpecificationVersion() != null) {
            libRef.setSpecificationVersion(library.getSpecificationVersion().toString());
        }
        if (library.getImplementationVersion() != null) {
            libRef.setImplementationVersion(library.getImplementationVersion().toString());
        }
    }
    
    public void setKeepJspGenerated(boolean keep) {
        JspDescriptorType[] desc = getJspDescriptor();
        for (JspDescriptorType type : desc) {
            type.setKeepgenerated(keep);
        }
    }
    
    public void setDebug(boolean debug) {
        JspDescriptorType[] desc = getJspDescriptor();
        for (JspDescriptorType type : desc) {
            type.setDebug(debug);
        }
    }

    public void setFastSwap(boolean fast) {
        if (bean instanceof org.netbeans.modules.j2ee.weblogic9.dd.web1031.WeblogicWebApp) {
            org.netbeans.modules.j2ee.weblogic9.dd.web1031.FastSwapType fastSwap = new org.netbeans.modules.j2ee.weblogic9.dd.web1031.FastSwapType();
            fastSwap.setEnabled(fast);
            ((org.netbeans.modules.j2ee.weblogic9.dd.web1031.WeblogicWebApp) bean).setFastSwap(fastSwap);
        } else if (bean instanceof org.netbeans.modules.j2ee.weblogic9.dd.web1211.WeblogicWebApp) {
            org.netbeans.modules.j2ee.weblogic9.dd.web1211.FastSwapType fastSwap = new org.netbeans.modules.j2ee.weblogic9.dd.web1211.FastSwapType();
            fastSwap.setEnabled(fast);
            ((org.netbeans.modules.j2ee.weblogic9.dd.web1211.WeblogicWebApp) bean).setFastSwap(fastSwap);
        }
    }
  
    private JspDescriptorType[] getJspDescriptor() {
        JspDescriptorType[] jspDescriptor = bean.getJspDescriptor();
        if (jspDescriptor != null && jspDescriptor.length > 0) {
            return jspDescriptor;
        }
        
        return new JspDescriptorType[] {bean.addJspDescriptor()};
    }
    
    private ServerLibraryDependency getLibrary(LibraryRefType libRef) {
        String name = libRef.getLibraryName();
        String specVersionString = libRef.getSpecificationVersion();
        String implVersionString = libRef.getImplementationVersion();
        boolean exactMatch = libRef.isExactMatch();

        Version spec = specVersionString == null ? null : Version.fromJsr277NotationWithFallback(specVersionString);
        Version impl = implVersionString == null ? null : Version.fromJsr277NotationWithFallback(implVersionString);
        if (exactMatch) {
            return ServerLibraryDependency.exactVersion(name, spec, impl);
        } else {
            return ServerLibraryDependency.minimalVersion(name, spec, impl);
        }
    }  
}
