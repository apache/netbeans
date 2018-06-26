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

package org.netbeans.modules.websvc.api.jaxws.client;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.websvc.jaxws.client.JAXWSClientSupportAccessor;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl;

import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.nodes.Node;

/** JAXWSClientSupport should be used to manipulate representations
 *  of JAX-WS service references (WS Clients) in a project.
 * <p>
 * A client may obtain a JAXWSClientSupport instance using
 * <code>JAXWSClientSupport.getJAXWSClientSupport(fileObject)</code> static
 * method, for any FileObject in the project directory structure.
 *
 * @author Peter Williams
 */
public final class JAXWSClientSupport {
    
    private JAXWSClientSupportImpl impl;
    private static final Lookup.Result implementations =
        Lookup.getDefault().lookupResult(WebServicesClientSupportProvider.class);

    static  {
        JAXWSClientSupportAccessor.DEFAULT = new JAXWSClientSupportAccessor() {
            public JAXWSClientSupport createJAXWSClientSupport(JAXWSClientSupportImpl spiWebServicesClientSupport) {
                return new JAXWSClientSupport(spiWebServicesClientSupport);
            }

            public JAXWSClientSupportImpl getJAXWSClientSupportImpl(JAXWSClientSupport wscs) {
                return wscs == null ? null : wscs.impl;
            }
        };
    }

    private JAXWSClientSupport(JAXWSClientSupportImpl impl) {
        if (impl == null)
            throw new IllegalArgumentException ();
        this.impl = impl;
    }

    /** Find the JAXWSClientSupport for given file or null if the file does
     *  not belong to any module supporting JAX-WS service clients.
     */
    public static JAXWSClientSupport getJaxWsClientSupport (FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to JAXWSClientSupport.getJAXWSClientSupport(FileObject)"); // NOI18N
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            WebServicesClientSupportProvider impl = (WebServicesClientSupportProvider)it.next();
            JAXWSClientSupport wscs = impl.findJAXWSClientSupport (f);
            if (wscs != null) {
                return wscs;
            }
        }
        return null;
    }

    // Delegated methods from JAXWSClientSupportImpl

    /** Add JAX-WS Client to project.
     *  <ul>
     *  <li> add client element to jax-ws.xml (Netbeans specific configuration file)
     *  <li> download the wsdl file(s) and all related XML artifacts to the project 
     *  <li> generate JAX-WS artifacts for web service specified by wsdlUrl.
     *  <li> this can be achieved by creating specific target in build-impl.xml, that calls wsimport task.
     *  </ul>
     * @param clientName proposed name for the client (the web service reference node display name)
     * @param wsdlURL URL for web service WSDL file
     * @param isJsr109 flag indicating the need to add JAX-WS libraries to project:
     *        if (isJsr109==false) JAX-WS libraries should be added to the project classpath 
     * @return unique name for WS Client in the project(can be different than requested clientName)
     */
    public String addServiceClient(String serviceName, String wsdlUrl, String packageName, boolean isJsr109) {
        return impl.addServiceClient(serviceName, wsdlUrl, packageName, isJsr109);
    }
    
    
    /** Remove JAX-WS Client from project.
     * <ul>
     *  <li> remove client element from jax-ws.xml (Netbeans specific configuration file)
     *  <li> remove all WSDL/XML artifacts related to this client
     *  <li> remove all JAX-WS java artifacts generated for this client
     * </ul>
     * @param clientName client name (the web service reference node display name)
     */
    public void removeServiceClient(String serviceName) {
        impl.removeServiceClient(serviceName);
    }

    /** Get WSDL folder for the project (folder containing wsdl files)
     *  The folder is used to save remote or local wsdl files to be available within the jar/war files.
     *  it is usually META-INF/wsdl folder (or WEB-INF/wsdl for web application)
     *  @param createFolder if (createFolder==true) the folder will be created (if not created before)
     *  @return the file object (folder) where wsdl files are located in project 
     */
    public FileObject getWsdlFolder(boolean create) throws IOException {
        return impl.getWsdlFolder(create);
    }
    
    /** Get folder for local WSDL and XML artifacts for given client
     * This is the location where wsdl/xml files are downloaded to the project.
     * JAX-WS java artifacts will be generated from these local files instead of remote.
     * @param clientName client name (the web service reference node display name)
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where wsdl files are located in project 
     */
    public FileObject getLocalWsdlFolderForClient(String clientName, boolean createFolder) {
        return impl.getLocalWsdlFolderForClient(clientName,createFolder);
    }
    
    /** Get folder for local jaxb binding (xml) files for given client
     *  This is the location where external jaxb binding files are downloaded to the project.
     *  JAX-WS java artifacts will be generated using these local binding files instead of remote.
     * @param clientName client name (the web service reference node display name)
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where jaxb binding files are located in project 
     */
    public FileObject getBindingsFolderForClient(String clientName, boolean createFolder) {
        return impl.getBindingsFolderForClient(clientName,createFolder);
    }
    
    /** gets the URL of catalog.xml file
     *  (the catalog is used by wsimport to locate local wsdl/xml resources)
     * @return URL url of the car
     */
    public URL getCatalog() {
        return impl.getCatalog();
    }
    
    /** Get list of all JAX-WS Clients in project
     * @param clientName client name (the web service reference node display name)
     */    
    public List/*Client*/ getServiceClients() {
        return impl.getServiceClients();
    }
    
    /** intended to be used to obtain service-ref name for given web service reference
     *  (currently not used in projects)
     */    
     public String getServiceRefName(Node clientNode){
         return impl.getServiceRefName(clientNode);
     }
     
     public AntProjectHelper getAntProjectHelper(){
         return impl.getAntProjectHelper();
     }
}
