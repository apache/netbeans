/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
