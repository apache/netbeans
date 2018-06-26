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

package org.netbeans.modules.websvc.jaxws.light.spi;

import java.net.URL;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.openide.filesystems.FileObject;

/** SPI for JAXWSSupport.
 *
 * @author Milan Kuchtiak
 */

public interface JAXWSLightSupportImpl {

    /** Add JAX-WS service/client to project.
     *
     * @param service service or client
     */
    void addService(JaxWsService service);

    /** Returns the list of JAX-WS services and clients.
     *
     * @return list of web services
     */
    List<JaxWsService> getServices();

    /** Remove JAX-WS service from project.
     *
     * @param service service
     */
    void removeService(JaxWsService service);

    /** Get deployment descriptor folder for the project (folder containing configuration files, like web.xml).
     *
     *  @return the folder where xml configuration files are located
     */
    FileObject getDeploymentDescriptorFolder();

    /** Get folder for local WSDL and XML artifacts for given service.
     *
     * This is the location where wsdl/xml files are downloaded to the project.
     * JAX-WS java artifacts will be generated from these local files instead of remote.
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where wsdl files are located in project
     */
    FileObject getWsdlFolder(boolean createFolder);

    /** Get folder for local jaxb binding (xml) files for given service.
     *
     *  This is the location where external jaxb binding files are downloaded to the project.
     *  JAX-WS java artifacts will be generated using these local binding files instead of remote.
     *
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where jaxb binding files are located in project
     */
    FileObject getBindingsFolder(boolean createFolder);

    /** Get EntityCatalog for local copies of wsdl and schema files.
     * @return URL for catalog file
     */
    URL getCatalog();

    /** Get metadata model of a webservices deployment descriptor.
     *
     * @return metadata model of a webservices deployment descriptor
     */
    MetadataModel<WebservicesMetadata> getWebservicesMetadataModel();

}
