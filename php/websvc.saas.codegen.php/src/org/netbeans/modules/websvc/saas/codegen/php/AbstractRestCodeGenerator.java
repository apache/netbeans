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
