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
package org.netbeans.modules.websvc.saas.codegen.php;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
abstract class AbstractRestCodeGenerator extends SaasClientCodeGenerator {

    public abstract FileObject getSaasFolder() throws IOException;
    
    protected void addImportsToSaasService() throws IOException {
        List<String> imports = new ArrayList<String>();
        imports.add(REST_CONNECTION_PACKAGE + "->" + REST_CONNECTION);
        imports.add(REST_CONNECTION_PACKAGE + "->" + REST_RESPONSE);
        //addImportsToPhp(getSaasServiceFile(), imports);
    }
    
    protected void createRestConnectionFile(Project project) throws IOException {
        FileObject fileObject = project.getProjectDirectory().getFileObject(
                AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Map<String,Object> map = new HashMap<String, Object>();
        if ( fileObject != null ){
            Properties properties = new Properties();
            InputStream inputStream = fileObject.getInputStream();
            try {
                properties.load(inputStream);
            }
            finally {
                inputStream.close();
            }
            String version = properties.getProperty("php.version");          // NOI18N
            if ( version!= null && version.trim().startsWith("PHP_") ){      // NOI18N
                version = version.trim().substring(4);
                try {
                    int phpVersion = Integer.parseInt(version);
                    if ( phpVersion >5 ){
                        /*
                         *  versions 5.1 , 5.2 are recognized as PHP_5. 
                         *  All other versions are PHP_5X ( and future versions
                         *  will  be PHP_YZ where Y >=6.
                         */
                        map.put("deprecated", Boolean.TRUE);                 // NOI18N
                    }
                }
                catch( NumberFormatException e ){
                    // just ignore
                }
            }
        }
        Util.createDataObjectFromTemplate(SaasClientCodeGenerator.TEMPLATES_SAAS+
                REST_CONNECTION+"."+Constants.PHP_EXT, 
                getSaasFolder(), null, map );
        Util.createDataObjectFromTemplate(SaasClientCodeGenerator.TEMPLATES_SAAS+
                REST_RESPONSE+"."+Constants.PHP_EXT, 
                getSaasFolder(), null, map);
    }
    
    //protected abstract FileObject getSaasServiceFile();
}
