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
/*
 * ResourceConfigurationInterface.java
 *
 * Created on August 13, 2005, 8:38 AM
 */
package org.netbeans.modules.j2ee.sun.api;

import java.io.File;
import java.util.HashSet;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;

/**
 *
 * @author Nitya Doraisamy
 */
public interface ResourceConfiguratorInterface {

    public boolean isJMSResourceDefined(String jndiName, File dir);

    public void createJMSResource(String jndiName, String msgDstnType, String msgDstnName, String ejbName, File dir, String resourcesXmlName);
    
    public MessageDestination createJMSResource(String jndiName, MessageDestination.Type type, String ejbName, File dir, String resourcesXmlName);

    public void createJDBCDataSourceFromRef(String refName, String databaseInfo, File dir);

    public String createJDBCDataSourceForCmp(String beanName, String databaseInfo, File dir);
    
    public Datasource createDataSource(String jndiName, String url, String username, String password, String driver, File dir, String resourcesXmlName) throws DatasourceAlreadyExistsException;
    
    public HashSet getServerDataSources();  
    
    public HashSet getServerDestinations();  
    
    public HashSet getResources(File dir);   
    
    public HashSet getMessageDestinations(File dir);   
    
}
