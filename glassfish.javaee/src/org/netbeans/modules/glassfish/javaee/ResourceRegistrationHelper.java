/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.glassfish.javaee;

import org.netbeans.modules.glassfish.tooling.admin.CommandSetProperty;
import org.netbeans.modules.glassfish.tooling.admin.ResultString;
import org.netbeans.modules.glassfish.tooling.admin.CommandAddResources;
import org.netbeans.modules.glassfish.tooling.admin.CommandGetProperty;
import org.netbeans.modules.glassfish.tooling.admin.ResultMap;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.common.parser.TreeParser;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author vkraemer
 */
public class ResourceRegistrationHelper {
    private static final int TIMEOUT = 2000;

    private ResourceRegistrationHelper() {
    }

    public static void deployResources(File root, Hk2DeploymentManager dm) {
        Set<File> resourceDirs = getResourceDirs(root);
        deployResources(resourceDirs,dm);
    }

    private static void deployResources(Set<File> resourceDirs, Hk2DeploymentManager dm)  {
        for(File resourceDir: resourceDirs) {
            try {
                boolean usedNewName = registerResourceDir(resourceDir,dm,dm.getCommonServerSupport().getResourcesXmlName());
                if (!usedNewName) {
                    // try to use sun-resources.xml
                    registerResourceDir(resourceDir,dm,"sun-resources"); // NOI18N
                }
            } catch (ConfigurationException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, "some data sources may not be deployed", ex);
            }
        }
    }

    private static Set<File> getResourceDirs(File file){
        Set<File> retVal = new TreeSet<File>();
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        SourceFileMap sourceFileMap = SourceFileMap.findSourceMap(fo);
        if (sourceFileMap != null) {
            File[] erds = sourceFileMap.getEnterpriseResourceDirs();

            if (null != erds) {
                for (File f : erds) {
                    if (null != f && f.getPath() != null) {
                        retVal.add(f);
                    }
                }
            }
        }
        return retVal;
    }

    private static boolean registerResourceDir(File resourceDir, Hk2DeploymentManager dm, String baseName) throws ConfigurationException {
        boolean succeeded = false;
        File sunResourcesXml = new File(resourceDir, baseName+".xml"); //NOI18N
        if(sunResourcesXml.exists()) {
            checkUpdateServerResources(sunResourcesXml, dm);
            GlassfishModule commonSupport = dm.getCommonServerSupport();
            String uri = 
                    commonSupport.getInstanceProperties().get(GlassfishModule.URL_ATTR);
            String target = Hk2DeploymentManager.getTargetFromUri(uri);
            try {
                ResultString result = CommandAddResources.addResource(
                        commonSupport.getInstance(), sunResourcesXml, target);
                return result.getState() == TaskState.COMPLETED;
            } catch (GlassFishIdeException gfie) {
                Logger.getLogger("glassfish-javaee")
                        .log(Level.INFO, gfie.getLocalizedMessage(), gfie);
                throw new ConfigurationException(gfie.getLocalizedMessage(), gfie);
            }
        }
        return succeeded;
    }

    private static void checkUpdateServerResources(File sunResourcesXml, Hk2DeploymentManager dm){
          Map<String, String> changedData = new HashMap<String, String>();
          List<TreeParser.Path> pathList = new ArrayList<TreeParser.Path>();
          ResourceFinder cpFinder = new ResourceFinder("name"); // NOI18N
          pathList.add(new TreeParser.Path("/resources/jdbc-connection-pool", cpFinder)); // NOI18N
          ResourceFinder jdbcFinder = new ResourceFinder("jndi-name"); // NOI18N
          pathList.add(new TreeParser.Path("/resources/jdbc-resource", jdbcFinder)); // NOI18N
          ResourceFinder connectorPoolFinder = new ResourceFinder("name"); // NOI18N
          pathList.add(new TreeParser.Path("/resources/connector-connection-pool", connectorPoolFinder)); // NOI18N
          ResourceFinder connectorFinder = new ResourceFinder("jndi-name"); // NOI18N
          pathList.add(new TreeParser.Path("/resources/connector-resource", connectorFinder)); // NOI18N
          ResourceFinder aoFinder = new ResourceFinder("jndi-name"); // NOI18N
          pathList.add(new TreeParser.Path("/resources/admin-object-resource", aoFinder)); // NOI18N
          ResourceFinder mailFinder = new ResourceFinder("jndi-name"); // NOI18N
          pathList.add(new TreeParser.Path("/resources/mail-resource", mailFinder)); // NOI18N
                    
          try {
            TreeParser.readXml(sunResourcesXml, pathList);
          } catch (IllegalStateException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
          }
          Map<String, String> allRemoteData = getResourceData("resources.*", dm); // NOI18N
          changedData = checkResources(cpFinder, "resources.jdbc-connection-pool.", allRemoteData, changedData, dm); // NOI18N
          changedData = checkResources(jdbcFinder, "resources.jdbc-resource.", allRemoteData, changedData, dm); // NOI18N
          changedData = checkResources(connectorPoolFinder, "resources.connector-connection-pool.", allRemoteData, changedData, dm); // NOI18N
          changedData = checkResources(connectorFinder, "resources.connector-resource.", allRemoteData, changedData, dm); // NOI18N
          changedData = checkResources(aoFinder, "resources.admin-object-resource.", allRemoteData, changedData, dm); // NOI18N
          changedData = checkResources(mailFinder, "resources.mail-resource.", allRemoteData, changedData, dm); // NOI18N

          if(changedData.size() > 0) {
            putResourceData(changedData, dm);
          }
    }

    private static Map<String, String> checkResources(ResourceFinder resourceFinder, String prefix, Map<String, String> allRemoteData, Map<String, String> changedData, Hk2DeploymentManager dm) {
        List<String> resources = resourceFinder.getResourceNames();
        for (int i = 0; i < resources.size(); i++) {
            String jndiName = resources.get(i);
            Map<String, String> localData = resourceFinder.getResourceData().get(jndiName);
            String remoteKey = prefix + jndiName + "."; // NOI18N
            Map<String, String> remoteData = new HashMap<String, String>();
            Iterator itr = allRemoteData.keySet().iterator();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                if(key.startsWith(remoteKey)){
                    remoteData.put(key, allRemoteData.get(key));
                }
            }
            if (remoteData.size() > 0) {
                changedData = getChangedData(remoteData, localData, changedData, remoteKey);
            }
        }
        return changedData;
    }
    
    private static Map<String, String> getChangedData(Map<String, String> remoteData, Map<String, String> localData, Map<String, String> changedData, String resourceKey) {
        List<String> props = new ArrayList<String>();
        Iterator<String> keys = remoteData.keySet().iterator();
        Set<String> localKeySet = localData.keySet();
        while (keys.hasNext()) {
            String remoteDataKey = keys.next();
            String remoteValue = remoteData.get(remoteDataKey);
            String[] split = remoteDataKey.split(resourceKey);
            String key = split[1];
            if (key.indexOf("property.") != -1) { // NOI18N
                props.add(key);
            }
            String localValue = (String) localData.get(key);
            if (localValue != null) {
                if (remoteValue == null || !localValue.equals(remoteValue)) {
                    changedData.put(remoteDataKey, localValue);
                }
            } else {
                if (localKeySet.contains(key)) {
                    if (remoteValue != null) {
                        changedData.put(remoteDataKey, localValue);
                    }
                }
            }
        }
        keys = localData.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.indexOf("property.") != -1) { // NOI18N
                if (!props.contains(key)) {
                    String remoteKey = resourceKey + key;
                    changedData.put(remoteKey, localData.get(key));
                }
            }
        }
        return changedData;
    }

    public static Map<String, String> getResourceData(String query, Hk2DeploymentManager dm) {
        try {
            ResultMap<String, String> result = CommandGetProperty.getProperties(
                    dm.getCommonServerSupport().getInstance(), query);
            if (result.getState() == TaskState.COMPLETED) {
                Map<String,String> values = result.getValue();
                if (values.isEmpty())
                    Logger.getLogger("glassfish-javaee").log(Level.INFO, null,
                            new IllegalStateException(query+" has no data"));
                return values;
                
            }
        } catch (GlassFishIdeException gfie) {
            Logger.getLogger("glassfish-javaee").log(Level.INFO,
                    "Could not retrieve property from server.", gfie);
        }
        return new HashMap<String,String>();
    }

    public static void putResourceData(Map<String, String> data, Hk2DeploymentManager dm) {
        Set<String> keys = data.keySet();
        for (String k : keys) {
            String name = k;
            String value = data.get(k);
            try {
                GlassfishModule support = dm.getCommonServerSupport();
                CommandSetProperty command = support.getCommandFactory()
                        .getSetPropertyCommand(name, value);
                CommandSetProperty.setProperty(support.getInstance(), command);
            } catch (GlassFishIdeException gfie) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, gfie.getMessage(), gfie);  // NOI18N
            }
        }
    }

    public static class ResourceFinder extends TreeParser.NodeReader {

        private Map<String, String> properties = null;
        private Map<String, Map<String, String>> resourceData = new HashMap<String, Map<String, String>>();

        private final String nameKey;

        public ResourceFinder(String in_nameKey) {
            nameKey = in_nameKey;
        }
        
        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            properties = new HashMap<String, String>();

            String resourceName = attributes.getValue(nameKey);
            properties.put(nameKey, resourceName);  //NOI18N

            int attrLen = attributes.getLength();
            for (int i = 0; i < attrLen; i++) {
                String name = attributes.getLocalName(i);
                String value = attributes.getValue(i);
                if (name != null && name.length() > 0 && value != null && value.length() > 0) {
                    properties.put(name, value);
                }
            }
        }

        @Override
        public void readChildren(String qname, Attributes attributes) throws SAXException {
            if (null != properties && null != attributes) {
                String propName = qname + "." + attributes.getValue("name"); // NO18N
                properties.put(propName, attributes.getValue("value"));  //NOI18N
            }
        }

        @Override
        public void endNode(String qname) throws SAXException {
            String poolName = properties.get(nameKey);  //NOI18N
            resourceData.put(poolName, properties);
        }

        public List<String> getResourceNames() {
            return new ArrayList<String>(resourceData.keySet());
        }

        public Map<String, Map<String, String>> getResourceData() {
            return Collections.unmodifiableMap(resourceData);
        }
    }
    
}
