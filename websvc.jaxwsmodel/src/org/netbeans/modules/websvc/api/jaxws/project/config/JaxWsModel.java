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

package org.netbeans.modules.websvc.api.jaxws.project.config;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;

/** Provides information about web services and clients in a project
 * Provides information used for build-impl generation
 * Working over nbproject/jax-ws.xml file
 */
public interface JaxWsModel {
    
    public Service[] getServices();
    
    public void setJsr109(Boolean jsr109);
    
    public Boolean getJsr109();
    
    public Service findServiceByName(String name);
    
    public Service findServiceByImplementationClass(String wsClassName);

    
    public boolean removeService(String name);
    
    public boolean removeServiceByClassName(String webserviceClassName);
    
    public Service addService(String name, String implementationClass)
    throws ServiceAlreadyExistsExeption;
    
    public Service addService(String name, String implementationClass, String wsdlUrl, String serviceName, String portName, String packageName)
    throws ServiceAlreadyExistsExeption;
    
    public Client[] getClients();
    
    public Client findClientByName(String name);
    
    public Client findClientByWsdlUrl(String wsdlUrl);

    public boolean removeClient(String name) ;
    
    public Client addClient(String name, String wsdlUrl, String packageName)
    throws ClientAlreadyExistsExeption;
    
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    public void removePropertyChangeListener(PropertyChangeListener l);
    
    public void merge(JaxWsModel newJaxWs);
    
    public void write(OutputStream os) throws IOException;
    
    public FileObject getJaxWsFile();
    
    public void setJaxWsFile(FileObject fo);
    
    public void write() throws IOException;
    
    public void addServiceListener(ServiceListener listener);
    
    public void removeServiceListener(ServiceListener listener);
    
    public static interface ServiceListener {
        
        public void serviceAdded(String name, String implementationClass);
        
        public void serviceRemoved(String name);
        
    }
    
    /** Registers ChangeListener for JaxWsModel object.
     *  The listener fires the ChangeEvent when FileObject is set for JaxWsModel
     *  (For projects, this occurs when jax-ws.xml is physicaly created in nbproject directory)   
     * 
     * @param listener ChangeListener instance
     */
    public void addChangeListener(ChangeListener listener);
    
    /** Unregisters ChangeListener from JaxWsModel object.
     * 
     * @param listener ChangeListener instance
     */
    public void removeChangeListener(ChangeListener listener);
    
}
