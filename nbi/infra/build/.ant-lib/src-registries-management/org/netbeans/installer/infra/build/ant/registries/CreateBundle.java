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

package org.netbeans.installer.infra.build.ant.registries;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;
import org.netbeans.installer.infra.build.ant.utils.Utils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.infra.lib.registries.ManagerException;
import org.netbeans.installer.infra.lib.registries.impl.RegistriesManagerImpl;

/**
 *
 * @author ks152834
 */
public class CreateBundle extends Task {
    private List<Component> componentObjects = new LinkedList<Component>();
    
    private File root;
    private File target;
    private Platform platform;
    private Vector<Property> properties = new Vector <Property> ();
    private Vector<BundleProperty> bundleProperties = new Vector <BundleProperty> ();
    private boolean keepTempBundles = false;
    
    public void setRoot(final File root) {
        this.root = root;
    }
    
    public void setPlatform(final String platform) {
        try {
            this.platform = StringUtils.parsePlatform(platform);
        } catch (ParseException e) {
            log(e.getMessage());
        }
    }
    
    public void setTarget(final File target) {
        this.target = target;
    }
    
    public Component createComponent() {
        final Component component = new Component();
        
        componentObjects.add(component);
        return component;
    }
    
    public void addProperty(Property p) {
        properties.addElement(p);
    }
    public void addBundleProperty(BundleProperty p) {
        bundleProperties.addElement(p);
    }
    public void setKeepTempBundles(boolean keepTempBundles) {
        this.keepTempBundles = keepTempBundles;
    }
    @Override
    public void execute() throws BuildException {
        try {
            final List<String> components = new LinkedList<String>();
            for (Component component: componentObjects) {
                components.add(component.getUid() + "," + component.getVersion());
            }
            
            System.out.println(
                    "Creating bundle: " + platform + ": " + components);
            Properties props = readProperties(properties);
            Properties bundleprops = readProperties(bundleProperties);            
            RegistriesManagerImpl impl = new RegistriesManagerImpl();
            final File bundle = impl.createBundle(
                    root,
                    platform,
                    components.toArray(new String[components.size()]),
                    props,
                    bundleprops);
            
            Utils.copy(bundle, target);
            if(!keepTempBundles) {
                impl.deleteBundles(root);
            }
        } catch (ManagerException e) {
            throw new BuildException(e);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    
    private Properties readProperties(Vector <? extends Property> antProperties) throws IOException {
        Properties props = new Properties();
        for(Property prop : antProperties) {
            if(prop.getName()!=null) {
                if(prop.getValue()!=null) {
                    props.setProperty(prop.getName(), prop.getValue());
                } else if(prop.getLocation()!=null) {
                    props.setProperty(prop.getName(),
                            new File(prop.getLocation().getFileName()).getAbsolutePath());
                }
            } else if(prop.getFile()!=null || prop.getUrl()!=null) {
                InputStream is = null;
                try {
                    is = (prop.getFile()!=null) ?
                        new FileInputStream(prop.getFile()) :
                        prop.getUrl().openStream();
                    
                    Properties loadedProps = new Properties();
                    loadedProps.load(is);
                    is.close();
                    if ( prop.getPrefix() != null ) {
                        for(Object p : loadedProps.keySet()) {
                            props.setProperty(prop.getPrefix() + p,
                                    loadedProps.getProperty(p.toString()));
                        }
                    } else {
                        props.putAll(loadedProps);
                    }
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }
        }
        
        return props;
    }
    public static class Component {
        private String uid;
        private String version;
        
        public void setUid(final String uid) {
            this.uid = uid;
        }
        
        public String getUid() {
            return uid;
        }
        
        public void setVersion(final String version) {
            this.version = version;
        }
        
        public String getVersion() {
            return version;
        }
    }
    public static class BundleProperty extends Property {
        
    }
}
