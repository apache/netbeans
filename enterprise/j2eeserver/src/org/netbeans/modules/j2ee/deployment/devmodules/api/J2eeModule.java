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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.j2ee.deployment.config.J2eeModuleAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import javax.enterprise.deploy.shared.ModuleType;
import org.openide.filesystems.FileObject;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleBase;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.util.Parameters;

/** 
 * Abstraction of J2EE module. Provides access to basic server-neutral properties 
 * of the modules: J2EE version, module type, deployment descriptor.
 * <p>
 * It is not possible to instantiate this class directly. Implementators have to
 * implement the {@link J2eeModuleImplementation} first and then use the
 * {@link org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory}
 * to create a J2eeModule instance.
 * 
 * @author  Pavel Buzek
 */
public class J2eeModule {

    /** MIME type for ContentDescriptor of build targets that have J2eeModule in lookup.
     * This can be used to search implementations of targets providing J2eeModule 
     * in project's ContainersList.
     */
    public static final String MIME_J2EE_MODULE_TARGET = "MIME-org-nb-j2eeserver-J2eeModule-BuildTarget"; //NOI18N
    
    /**
     * The module is an EAR archive.
     * @deprecated use {@link Type#EAR}
     */
    @Deprecated
    public static final Object EAR = ModuleType.EAR;
    
    /**
     * The module is an Web Application archive.
     * @deprecated use {@link Type#WAR}
     */
    @Deprecated
    public static final Object WAR = ModuleType.WAR;

    /**
     * The module is an Enterprise Java Bean archive.
     * @deprecated use {@link Type#EJB}
     */
    @Deprecated
    public static final Object EJB = ModuleType.EJB;

    /**
     * The module is an Connector archive.
     * @deprecated use {@link Type#RAR}
     */
    @Deprecated
    public static final Object CONN = ModuleType.RAR;

    /**
     * The module is an Client Application archive.
     * @deprecated use {@link Type#CAR}
     */
    @Deprecated
    public static final Object CLIENT = ModuleType.CAR;
    
    /**
     * J2EE specification version 1.3
     * @since 1.5
     * @deprecated use {@link org.netbeans.api.j2ee.core.Profile#J2EE_13}
     */
    @Deprecated
    public static final String J2EE_13 = "1.3"; //NOI18N
    /**
     * J2EE specification version 1.4
     * @since 1.5
     * @deprecated use {@link org.netbeans.api.j2ee.core.Profile#J2EE_14}
     */
    @Deprecated
    public static final String J2EE_14 = "1.4"; //NOI18N
    /**
     *
     * JAVA EE 5 specification version
     *
     * @since 1.6
     * @deprecated use {@link org.netbeans.api.j2ee.core.Profile#JAVA_EE_5}
     */
    @Deprecated
    public static final String JAVA_EE_5 = "1.5"; // NOI18N
    
    public static final String APP_XML = "META-INF/application.xml";
    public static final String WEB_XML = "WEB-INF/web.xml";
    public static final String WEBSERVICES_XML = "WEB-INF/webservices.xml";
    public static final String EJBJAR_XML = "META-INF/ejb-jar.xml";
    public static final String EJBSERVICES_XML = "META-INF/webservices.xml";
    public static final String CONNECTOR_XML = "META-INF/ra.xml";
    public static final String CLIENT_XML = "META-INF/application-client.xml";
    
    
    /**
     * Enterprise resorce directory property
     */
    public static final String PROP_RESOURCE_DIRECTORY = "resourceDir"; // NOI18N
    
    /**
     * Module version property
     */
    public static final String PROP_MODULE_VERSION = "moduleVersion"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(J2eeModule.class.getName());

    static {
        J2eeModuleAccessor.setDefault(new J2eeModuleAccessor() {
            public J2eeModule createJ2eeModule(J2eeModuleImplementation impl) {
                return new J2eeModule(impl);
            }

            @Override
            public J2eeModule createJ2eeModule(J2eeModuleImplementation2 impl) {
                return new J2eeModule(impl);
            }

            public J2eeModuleProvider getJ2eeModuleProvider(J2eeModule j2eeModule) {
               return j2eeModule.getJ2eeModuleProvider();
            }

            public void setJ2eeModuleProvider(J2eeModule j2eeModule, J2eeModuleProvider j2eeModuleProvider) {
                j2eeModule.setJ2eeModuleProvider(j2eeModuleProvider);
            }

            @Override
            public ModuleType getJsrModuleType(Type type) {
                return type.getJsrType();
            }

        });
    }
    
    private J2eeModuleProvider j2eeModuleProvider;
    
    private final J2eeModuleBase impl;
    
    J2eeModule(J2eeModuleBase impl) {
        this.impl = impl;
    }

    /** 
     * Returns a Java/Jakarta EE module specification version, version of a web application 
     * for example.
     * <p>
     * Do not confuse with the Java/Jakarta EE platform specification version.
     *
     * @return module specification version.
     */
    @NonNull
    public String getModuleVersion() {
        return impl.getModuleVersion();
    }
    
    /** 
     * Returns module type.
     * 
     * @return module type.
     * @deprecated use {@link #getType()}
     */
    @NonNull
    @Deprecated
    public Object getModuleType() {
        if (impl instanceof J2eeModuleImplementation2) {
            return ((J2eeModuleImplementation2) impl).getModuleType().getJsrType();
        } else {
            return ((J2eeModuleImplementation) impl).getModuleType();
        }
    }

    /**
     * Returns module type.
     *
     * @return module type
     * @since 1.59
     */
    @NonNull
    public Type getType() {
        if (impl instanceof J2eeModuleImplementation2) {
            return ((J2eeModuleImplementation2) impl).getModuleType();
        } else {
            Type type = Type.fromJsrType(((J2eeModuleImplementation) impl).getModuleType());
            assert type != null;
            return type;
        }
    }
    
    /** 
     * Returns the location of the module within the application archive.
     * 
     * @return location of the module within the application archive.
     */
    public String getUrl() {
        return impl.getUrl();
    }
    
    /** Returns the archive file for the module of null if the archive file 
     * does not exist (for example, has not been compiled yet). 
     */
    public FileObject getArchive() throws java.io.IOException {
        return impl.getArchive();
    }
    
    /** Returns the contents of the archive, in copyable form.
     *  Used for incremental deployment.
     *  Currently uses its own {@link RootedEntry} interface.
     *  If the J2eeModule instance describes a
     *  j2ee application, the result should not contain module archives.
     *  @return entries
     */
    public Iterator<J2eeModule.RootedEntry> getArchiveContents() throws java.io.IOException {
        return impl.getArchiveContents();
    }

    /** This call is used in in-place deployment. 
     *  Returns the directory staging the contents of the archive
     *  This directory is the one from which the content entries returned
     *  by {@link #getArchiveContents} came from.
     *  @return FileObject for the content directory, return null if the 
     *     module doesn't have a build directory, like an binary archive project
     */
    public FileObject getContentDirectory() throws java.io.IOException {
        return impl.getContentDirectory();
    }
    
    /**
     * Returns a metadata model of a deployment descriptor specified by the 
     * <code>type</code> parameter.
     * 
     * <p>
     * As an example, passing <code>org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata.class</code>
     * as a type parameter will return a metadata model of the web module deployment 
     * descriptor - web.xml.
     * </p>
     * 
     * @param type metadata model type class for which a <code>MetadataModel</code>
     *        instance will be returned.
     * 
     * @return metadata model of a deployment descriptor specified by the <code>type</code>
     *         parameter.
     * 
     * @throws NullPointerException if the <code>type</code> parameter is <code>null</code>.
     */
    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        Parameters.notNull("type", type); // NOI18N
        return impl.getMetadataModel(type);
    }
    
    /**
     * Returns the module resource directory or null if the module has no resource
     * directory.
     * 
     * @return the module resource directory or null if the module has no resource
     *         directory.
     */
    public File getResourceDirectory() {
        return impl.getResourceDirectory();
    }
    
    /**
     * Returns source deployment configuration file path for the given deployment 
     * configuration file path name, for example (but not only) WEB-INF/sun-web.xml
     * or META-INF/context.xml.
     *
     * @param name file name of the deployment configuration file, WEB-INF/sun-web.xml
     *        for example.
     * 
     * @return absolute path to the deployment configuration file, or null if the
     *         specified file name is not known to this J2eeModule.
     */
    public File getDeploymentConfigurationFile(String name) {
        return impl.getDeploymentConfigurationFile(name);
    }
    
    /**
     * Add a PropertyChangeListener to the listener list.
     * 
     * @param listener PropertyChangeListener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        impl.addPropertyChangeListener(listener);   
    }
    
    /**
     * Remove a PropertyChangeListener from the listener list.
     * 
     * @param listener PropertyChangeListener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        impl.removePropertyChangeListener(listener);
    }
    
    synchronized J2eeModuleProvider getJ2eeModuleProvider() {
        return j2eeModuleProvider;
    }
    
    synchronized void setJ2eeModuleProvider(J2eeModuleProvider j2eeModuleProvider) {
        this.j2eeModuleProvider = j2eeModuleProvider;
    }

    public interface RootedEntry {

        FileObject getFileObject ();

        String getRelativePath ();

    }

    public static final class Type {

        public static final Type WAR = new Type(ModuleType.WAR);

        public static final Type EJB = new Type(ModuleType.EJB);

        public static final Type EAR = new Type(ModuleType.EAR);

        public static final Type CAR = new Type(ModuleType.CAR);

        public static final Type RAR = new Type(ModuleType.RAR);

        private final ModuleType jsrType;

        private Type(ModuleType jsrType) {
            this.jsrType = jsrType;
        }

        private ModuleType getJsrType() {
            return jsrType;
        }

        @SuppressWarnings("deprecation")
        public static Type fromJsrType(Object type) {
            // be defensive
            if (type instanceof Type) {
                boolean assertsEnabled = false;
                assert assertsEnabled = true;
                if (assertsEnabled) {
                    LOGGER.log(Level.WARNING, "Redundant call to conversion method", new Exception());
                }
                return (Type) type;
            }
            assert type instanceof ModuleType;

            if (J2eeModule.WAR.equals(type)) {
                return WAR;
            } else if (J2eeModule.EJB.equals(type)) {
                return EJB;
            } else if (J2eeModule.EAR.equals(type)) {
                return EAR;
            } else if (J2eeModule.CLIENT.equals(type)) {
                return CAR;
            } else if (J2eeModule.CONN.equals(type)) {
                return RAR;
            }
            return null;
        }
    }

}
