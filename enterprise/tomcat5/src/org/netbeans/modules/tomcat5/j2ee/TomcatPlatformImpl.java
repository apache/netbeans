/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.tomcat5.j2ee;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl2;
import org.netbeans.modules.j2ee.deployment.plugins.spi.support.LookupProviderSupport;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Tomcat's implementation of the J2eePlatformImpl.
 *
 * @author Stepan Herold
 */
public class TomcatPlatformImpl extends J2eePlatformImpl2 {
    
    private static final String WSCOMPILE_LIBS[] = new String[] {
        "jaxrpc/lib/jaxrpc-api.jar",        // NOI18N
        "jaxrpc/lib/jaxrpc-impl.jar",       // NOI18N
        "jaxrpc/lib/jaxrpc-spi.jar",        // NOI18N
        "saaj/lib/saaj-api.jar",            // NOI18N
        "saaj/lib/saaj-impl.jar",           // NOI18N
        "jwsdp-shared/lib/mail.jar",        // NOI18N
        "jwsdp-shared/lib/activation.jar"   // NOI18N
    };

    private static final String JWSDP_LIBS[] = new String[] {
        "fastinfoset/lib/FastInfoset.jar",              // NOI18N
        "jaxb/lib/jaxb1-impl.jar",                      // NOI18N
        "jaxb/lib/jaxb-impl.jar",                       // NOI18N
        "jaxb/lib/jaxb-api.jar",                        // NOI18N
        "jaxb/lib/jaxb-xjc.jar",                        // NOI18N
        "jaxws/lib/jaxws-api.jar",                      // NOI18N
        "jaxws/lib/jaxws-rt.jar",                       // NOI18N
        "jaxws/lib/jaxws-tools.jar",                    // NOI18N
        "jaxws/lib/jsr181-api.jar",                     // NOI18N
        "jaxws/lib/jsr250-api.jar",                     // NOI18N
        "saaj/lib/saaj-api.jar",                        // NOI18N
        "saaj/lib/saaj-impl.jar",                       // NOI18N
        "sjsxp/lib/sjsxp.jar",                          // NOI18N
        "sjsxp/lib/jsr173_api.jar",                     // NOI18N
        "jwsdp-shared/lib/activation.jar",              // NOI18N
        "jwsdp-shared/lib/jaas.jar",                    // NOI18N
        "jwsdp-shared/lib/jta-spec1_0_1.jar",           // NOI18N
        "jwsdp-shared/lib/mail.jar",                    // NOI18N
        //"jwsdp-shared/lib/PackageFormat.jar",           // NOI18N
        "jwsdp-shared/lib/relaxngDatatype.jar",         // NOI18N
        "jwsdp-shared/lib/resolver.jar",                // NOI18N
        "jwsdp-shared/lib/xmlsec.jar",                  // NOI18N
        "jwsdp-shared/lib/xsdlib.jar"                  // NOI18N
    };

    private static final String WSIT_LIBS[] = new String[] {
        "shared/lib/webservices-rt.jar",              // NOI18N
        "shared/lib/webservices-tools.jar"     // NOI18N
    };
    
    private static final String JWSDP_WSGEN_LIBS[] = new String[] {
        "jaxws/lib/jaxws-tools.jar",                // NOI18N
        "jaxws/lib/jaxws-rt.jar",                   // NOI18N
        "sjsxp/lib/sjsxp.jar",                      // NOI18N
        "jaxb/lib/jaxb-xjc.jar",                    // NOI18N
        "saaj/lib/saaj-impl.jar",                    // NOI18N
        "saaj/lib/saaj-api.jar",                    // NOI18N
        "jwsdp-shared/lib/relaxngDatatype.jar",     // NOI18N
        "jwsdp-shared/lib/resolver.jar"             // NOI18N
    };

    private static final String JWSDP_WSIMPORT_LIBS[] = new String[] {
        "jaxws/lib/jaxws-tools.jar",                // NOI18N
        "jaxws/lib/jaxws-rt.jar",                   // NOI18N
        "sjsxp/lib/sjsxp.jar",                      // NOI18N
        "jaxb/lib/jaxb-xjc.jar",                    // NOI18N
        "jwsdp-shared/lib/relaxngDatatype.jar",     // NOI18N
        "jwsdp-shared/lib/resolver.jar"             // NOI18N
    };

    private static final String WSIT_WSIMPORT_LIBS[] = new String[] {
        "shared/lib/webservices-rt.jar",               // NOI18N
        "shared/lib/webservices-tools.jar"         // NOI18N
    };

    private static final String WSIT_WSGEN_LIBS[] = new String[] {
        "shared/lib/webservices-rt.jar",               // NOI18N
        "shared/lib/webservices-tools.jar"         // NOI18N
    };

    private static final String[] KEYSTORE_LOCATION = new String[] {
        "certs/server-keystore.jks"  //NOI18N
    };
    
    private static final String[] TRUSTSTORE_LOCATION = new String[] {
        "certs/server-truststore.jks"  //NOI18N
    };
    
    private static final String[] KEYSTORE_CLIENT_LOCATION = new String[] {
        "certs/client-keystore.jks"  //NOI18N
    };
    
    private static final String[] TRUSTSTORE_CLIENT_LOCATION = new String[] {
        "certs/client-truststore.jks"  //NOI18N
    };
    
    private static final String ICON = "org/netbeans/modules/tomcat5/resources/tomcat5instance.png"; // NOI18N
    
    private final String displayName;
    private final TomcatProperties tp;
    private final TomcatManager manager;

    /* GuardedBy("this") */
    private LibraryImplementation[] libraries;
    
    /** Creates a new instance of TomcatInstallation */
    public TomcatPlatformImpl(TomcatManager manager) {
        this.manager = manager;
        this.tp = manager.getTomcatProperties();
        displayName = tp.getDisplayName();
    }
    
    public void notifyLibrariesChanged() {
        synchronized (this) {
            libraries = null;
        }
        firePropertyChange(PROP_LIBRARIES, null, getLibraries());
    }
    
    @Override
    public synchronized LibraryImplementation[] getLibraries() {
        if (libraries == null) {
            J2eeLibraryTypeProvider libProvider = new J2eeLibraryTypeProvider();
            LibraryImplementation lib = libProvider.createLibrary();
            lib.setName(NbBundle.getMessage(TomcatPlatformImpl.class, "LBL_lib_name", displayName));
            loadLibraries(lib);
            libraries = new LibraryImplementation[1];
            libraries[0] = lib;
        }
        return libraries;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage(ICON);
    }
    
    @Override
    public File[] getPlatformRoots() {
        if (tp.getCatalinaBase() != null) {
            return new File[] {tp.getCatalinaHome(), tp.getCatalinaBase()};
        } else {
            return new File[] {tp.getCatalinaHome()};
        }
    }

    @Override
    public File getServerHome() {
        return tp.getCatalinaHome();
    }
    
    @Override
    public File getDomainHome() {
        return tp.getCatalinaBase();
    }

    @Override
    public File getMiddlewareHome() {
        return null;
    }
    
    @Override
    public File[] getToolClasspathEntries(String toolName) {
        // wscompile support
        if (J2eePlatform.TOOL_WSCOMPILE.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_WSCOMPILE)) {
                File[] retValue = new File[WSCOMPILE_LIBS.length];
                File homeDir = tp.getCatalinaHome();
                for (int i = 0; i < WSCOMPILE_LIBS.length; i++) {
                    retValue[i] = new File(homeDir, WSCOMPILE_LIBS[i]);
                }
                return retValue;
            }
        }
        // wsgen support
        if (J2eePlatform.TOOL_WSGEN.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_WSGEN)) {
                if (isToolSupported(J2eePlatform.TOOL_WSIT)) {
                    File[] retValue = new File[WSIT_WSGEN_LIBS.length];
                    File homeDir = tp.getCatalinaHome();
                    for (int i = 0; i < WSIT_WSGEN_LIBS.length; i++) {
                        retValue[i] = new File(homeDir, WSIT_WSGEN_LIBS[i]);
                    }
                    return retValue;
                } else {
                    File[] retValue = new File[JWSDP_WSGEN_LIBS.length];
                    File homeDir = tp.getCatalinaHome();
                    for (int i = 0; i < JWSDP_WSGEN_LIBS.length; i++) {
                        retValue[i] = new File(homeDir, JWSDP_WSGEN_LIBS[i]);
                    }
                    return retValue;
                }
            }
        }
        // wsimport support
        if (J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_WSIMPORT)) {
                if (isToolSupported(J2eePlatform.TOOL_WSIT)) {
                    File[] retValue = new File[WSIT_WSIMPORT_LIBS.length];
                    File homeDir = tp.getCatalinaHome();
                    for (int i = 0; i < WSIT_WSIMPORT_LIBS.length; i++) {
                        retValue[i] = new File(homeDir, WSIT_WSIMPORT_LIBS[i]);
                    }
                    return retValue;
                } else {
                    File[] retValue = new File[JWSDP_WSIMPORT_LIBS.length];
                    File homeDir = tp.getCatalinaHome();
                    for (int i = 0; i < JWSDP_WSIMPORT_LIBS.length; i++) {
                        retValue[i] = new File(homeDir, JWSDP_WSIMPORT_LIBS[i]);
                    }
                    return retValue;
                }
            }
        }
        // jwsdp support
        if (J2eePlatform.TOOL_JWSDP.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_WSIT)) {
                return getToolClasspathEntries(J2eePlatform.TOOL_WSIT);
            } else {
                if (isToolSupported(J2eePlatform.TOOL_JWSDP)) {
                    File[] retValue = new File[JWSDP_LIBS.length];
                    File homeDir = tp.getCatalinaHome();
                    for (int i = 0; i < JWSDP_LIBS.length; i++) {
                        retValue[i] = new File(homeDir, JWSDP_LIBS[i]);
                    }
                    return retValue;
                }
            }
        }
        // keystore support
        if (J2eePlatform.TOOL_KEYSTORE.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_KEYSTORE)) {
                File[] retValue = new File[KEYSTORE_LOCATION.length];
                File homeDir = tp.getCatalinaHome();
                for (int i = 0; i < KEYSTORE_LOCATION.length; i++) {
                    retValue[i] = new File(homeDir, KEYSTORE_LOCATION[i]);
                }
                return retValue;
            }
        }
        // truststore support
        if (J2eePlatform.TOOL_TRUSTSTORE.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_TRUSTSTORE)) {
                File[] retValue = new File[TRUSTSTORE_LOCATION.length];
                File homeDir = tp.getCatalinaHome();
                for (int i = 0; i < TRUSTSTORE_LOCATION.length; i++) {
                    retValue[i] = new File(homeDir, TRUSTSTORE_LOCATION[i]);
                }
                return retValue;
            }
        }
        if (J2eePlatform.TOOL_KEYSTORE_CLIENT.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_KEYSTORE_CLIENT)) {
                File[] retValue = new File[KEYSTORE_CLIENT_LOCATION.length];
                File homeDir = tp.getCatalinaHome();
                for (int i = 0; i < KEYSTORE_CLIENT_LOCATION.length; i++) {
                    retValue[i] = new File(homeDir, KEYSTORE_CLIENT_LOCATION[i]);
                }
                return retValue;
            }
        }
        // truststore support
        if (J2eePlatform.TOOL_TRUSTSTORE_CLIENT.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_TRUSTSTORE_CLIENT)) {
                File[] retValue = new File[TRUSTSTORE_CLIENT_LOCATION.length];
                File homeDir = tp.getCatalinaHome();
                for (int i = 0; i < TRUSTSTORE_CLIENT_LOCATION.length; i++) {
                    retValue[i] = new File(homeDir, TRUSTSTORE_CLIENT_LOCATION[i]);
                }
                return retValue;
            }
        }
        // wsit support
        if (J2eePlatform.TOOL_WSIT.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_WSIT)) {
                File[] retValue = new File[WSIT_LIBS.length];
                File homeDir = tp.getCatalinaHome();
                for (int i = 0; i < WSIT_LIBS.length; i++) {
                    retValue[i] = new File(homeDir, WSIT_LIBS[i]);
                }
                return retValue;
            }
        }
        return null;
    }
    
    @Override
    public boolean isToolSupported(String toolName) {
        // jwsdp support
        if (J2eePlatform.TOOL_WSCOMPILE.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < WSCOMPILE_LIBS.length; i++) {
                if (!new File(homeDir, WSCOMPILE_LIBS[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_WSGEN.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            boolean wsit = isToolSupported(J2eePlatform.TOOL_WSIT);
            if (wsit) {
                for (int i = 0; i < WSIT_WSGEN_LIBS.length; i++) {
                    if (!new File(homeDir, WSIT_WSGEN_LIBS[i]).exists()) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < JWSDP_WSGEN_LIBS.length; i++) {
                    if (!new File(homeDir, JWSDP_WSGEN_LIBS[i]).exists()) {
                        return false;
                    }
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            boolean wsit = isToolSupported(J2eePlatform.TOOL_WSIT);
            
            if (wsit) {
                for (int i = 0; i < WSIT_WSIMPORT_LIBS.length; i++) {
                    if (!new File(homeDir, WSIT_WSIMPORT_LIBS[i]).exists()) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < JWSDP_WSIMPORT_LIBS.length; i++) {
                    if (!new File(homeDir, JWSDP_WSIMPORT_LIBS[i]).exists()) {
                        return false;
                    }
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_JWSDP.equals(toolName)) {
            
            if (isToolSupported(J2eePlatform.TOOL_WSIT)) {
                return true;
            }
            
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < JWSDP_LIBS.length; i++) {
                if (!new File(homeDir, JWSDP_LIBS[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_KEYSTORE.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < KEYSTORE_LOCATION.length; i++) {
                if (!new File(homeDir, KEYSTORE_LOCATION[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_TRUSTSTORE.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < TRUSTSTORE_LOCATION.length; i++) {
                if (!new File(homeDir, TRUSTSTORE_LOCATION[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_KEYSTORE_CLIENT.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < KEYSTORE_CLIENT_LOCATION.length; i++) {
                if (!new File(homeDir, KEYSTORE_CLIENT_LOCATION[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_TRUSTSTORE_CLIENT.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < TRUSTSTORE_CLIENT_LOCATION.length; i++) {
                if (!new File(homeDir, TRUSTSTORE_CLIENT_LOCATION[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_WSIT.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < WSIT_LIBS.length; i++) {
                if (!new File(homeDir, WSIT_LIBS[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_JSR109.equals(toolName)) {
            return false;
        }
        // Test if server has the JAX-WS Tester capability
        
        return "jaxws-tester".equals(toolName);
    }

    @Override
    public Set<J2eeModule.Type> getSupportedTypes() {
        return Collections.singleton(J2eeModule.Type.WAR);
    }

    @Override
    public Set<Profile> getSupportedProfiles() {
        Set<Profile> profiles = new HashSet<>(5);
        //if (!manager.isTomEE()) {
            // TomEE is new and it actually does not support older specs (classloading separation etc).
            // we will see if that's a problem for anybody
            profiles.add(Profile.J2EE_13);
            profiles.add(Profile.J2EE_14);
            if (manager.isTomcat60() || manager.isAboveTomcat70()) {
                profiles.add(Profile.JAVA_EE_5);
            }
        //}
        if (manager.isAboveTomcat70()) {
            profiles.add(Profile.JAVA_EE_6_WEB);
        }
        if (manager.isTomcat80() || manager.isTomcat90()) {
            profiles.add(Profile.JAVA_EE_7_WEB);
        }
        return profiles;
    }
    
    @Override
    public Set<String> getSupportedJavaPlatformVersions() {
        Set<String> versions = new HashSet<>(6);

        if (!manager.isTomcat90()) {
            if (!manager.isTomcat80()) {
                if (!manager.isTomcat70()) {
                    if (!manager.isTomcat60()) {
                        versions.add("1.4"); // NOI18N
                        versions.add("1.5"); // NOI18N
                    }
                    versions.add("1.5"); // NOI18N
                }
                versions.add("1.6"); // NOI18N
            }
            versions.add("1.7"); // NOI18N
        }
        versions.add("1.8"); // NOI18N
        return versions;
    }
    
    @Override
    public JavaPlatform getJavaPlatform() {
        return tp.getJavaPlatform();
    }
    
    @Override
    public Lookup getLookup() {
        List content = new ArrayList();
        WSStack<JaxWs> wsStack = WSStackFactory.createWSStack(JaxWs.class ,
                new JaxWsStack(tp.getCatalinaHome()), WSStack.Source.SERVER);
        Collections.addAll(content, tp.getCatalinaHome(),
                new EjbSupportImpl(manager), wsStack);
        if (manager.isTomEE()) {
            content.add(new JpaSupportImpl());
        }
        if (manager.isTomEEJaxRS()) {
            content.add(new JaxRsStackSupportImpl(this));
        }

        Lookup baseLookup = Lookups.fixed(content.toArray());
        return LookupProviderSupport.createCompositeLookup(baseLookup, 
                "J2EE/DeploymentPlugins/Tomcat5/Lookup"); //NOI18N
    }
    
    // private helper methods -------------------------------------------------
    
    private void loadLibraries(LibraryImplementation lib) {
        lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, tp.getClasses());
        lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, tp.getJavadocs());
        lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC, tp.getSources());        
    }
}
