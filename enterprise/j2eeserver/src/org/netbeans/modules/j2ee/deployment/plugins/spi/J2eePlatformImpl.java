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

package org.netbeans.modules.j2ee.deployment.plugins.spi;


import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.config.J2eeModuleAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.Lookup;

/**
 * Base SPI interface for J2eePlatform. The J2eePlatform describes the target
 * environment J2EE applications are build against and subsequently deployed to.
 * Each server instance defines its own J2EE platform.
 *
 * @author Stepan Herold
 * @since 1.5
 */
public abstract class J2eePlatformImpl {
    
    /** Display name property */
    public static final String PROP_DISPLAY_NAME = "displayName";       //NOI18N

    /** Libraries property */
    public static final String PROP_LIBRARIES = "libraries"; //NOI18N
    public static final String PROP_SERVER_LIBRARIES = "serverLibraries"; //NOI18N

    private PropertyChangeSupport supp;
    
    /**
     * Return platform's libraries.
     *
     * @return platform's libraries.
     */
    public abstract LibraryImplementation[] getLibraries();

    /**
     * Return platform's libraries including the libraries required by passed
     * dependencies. In any case the returned array should be superset or
     * equal (containing same elements) to the return value of {@link #getLibraries()}.
     * <p>
     * This default implementation return the same libraries as {@link #getLibraries()}.
     * It should be overridden to handle passed dependencies as well. It is
     * also implementors responsibility to return libraries in correct order
     * in which these will be used on classpath.
     *
     * @param libraries the dependencies required
     * @return platform's libraries including the libraries required by passed
     *             dependencies
     */
    public LibraryImplementation[] getLibraries(Set<ServerLibraryDependency> libraries) {
        return getLibraries();
    }

    /**
     * Return platform's display name.
     *
     * @return platform's display name.
     */
    public abstract String getDisplayName();
    
    /**
     * Return an icon describing the platform. This will be mostly the icon
     * used for server instance nodes 
     * 
     * @return an icon describing the platform
     * @since 1.6
     */
    public abstract Image getIcon();
    
    /**
     * Return platform's root directories. This will be mostly server's installation
     * directory.
     *
     * @return platform's root directories.
     */
    public abstract File[] getPlatformRoots();
    
    /**
     * Return classpath for the specified tool. Use the tool constants declared 
     * in the {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform}.
     *
     * @param  toolName tool name, for example {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#TOOL_APP_CLIENT_RUNTIME}.
     * @return classpath for the specified tool.
     */
    public abstract File[] getToolClasspathEntries(String toolName);
    
    /**
     * Specifies whether a tool of the given name is supported by this platform.
     * Use the tool constants declared in the {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform}.
     *
     * @param  toolName tool name, for example {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#TOOL_APP_CLIENT_RUNTIME}
     * .
     * @return <code>true</code> if platform supports tool of the given name, 
     *         <code>false</code> otherwise.
     * @deprecated {@link #getLookup()} should be used to obtain tool specifics
     */
    @Deprecated
    public abstract boolean isToolSupported(String toolName);
    
    /**
     * Return a list of supported J2EE specification versions. Use J2EE specification 
     * versions defined in the {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * class.
     *
     * @return list of supported J2EE specification versions.
     * @deprecated override {@link #getSupportedProfiles()} and {@link #getSupportedProfiles(Type)}
     */
    @Deprecated
    public Set<String> getSupportedSpecVersions() {
        return Collections.emptySet();
    }
    
    /**
     * Return a list of supported J2EE specification versions for
     * a given module type.
     *
     * Implement this method if the server supports different versions
     * of spec for different types of modules.
     * If this method is not implemented by the plugin the IDE
     * will use the non parametrized version of
     * getSupportedSpecVersions.
     *
     * @param moduleType one of the constants defined in 
     *   {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * @return list of supported J2EE specification versions.
     * @deprecated override {@link #getSupportedProfiles()} and {@link #getSupportedProfiles(Type)}
     */
    @Deprecated
    public Set <String> getSupportedSpecVersions(Object moduleType) {
        return getSupportedSpecVersions();
    }

    /**
     * Returns a set of supported profiles. By default method converts
     * specification version returned by {@link #getSupportedSpecVersions()}
     * to profiles.
     *
     * @return set of supported profiles
     * @see Profile
     * @since 1.58
     */
    public Set<Profile> getSupportedProfiles() {
        Set<Profile> set = new HashSet<Profile>();
        for (String spec : getSupportedSpecVersions()) {
            Profile profile = Profile.fromPropertiesString(spec);
            if (profile != null) {
                set.add(profile);
            }
        }
        return set;
    }

    /**
     * Returns a set of supported profiles for the given module type
     * (one of {@link J2eeModule#EAR}, {@link J2eeModule#EJB},
     * {@link J2eeModule#WAR}, {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type#RAR} and {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type#CAR}).
     * By default method converts specification version returned by
     * {@link #getSupportedSpecVersions(java.lang.Object)} to profiles.
     *
     * @param moduleType type of the module
     * @return set of supported profiles
     * @see Profile
     * @since 1.59
     */
    public Set<Profile> getSupportedProfiles(J2eeModule.Type moduleType) {
        Set<Profile> set = new HashSet<Profile>();
        for (String spec : getSupportedSpecVersions(J2eeModuleAccessor.getDefault().getJsrModuleType(moduleType))) {
            Profile profile = Profile.fromPropertiesString(spec);
            if (profile != null) {
                set.add(profile);
            }
        }
        for (Profile profile : getSupportedProfiles()) {
            set.add(profile);
        }
        return set;
    }

    /**
     * Return a list of supported J2EE module types. Use module types defined in the 
     * {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * class.
     *
     * @return list of supported J2EE module types.
     * @deprecated override {@link #getSupportedTypes()}
     */
    @Deprecated
    public Set getSupportedModuleTypes() {
        return Collections.emptySet();
    }

    /**
     *
     * @return
     * @since 1.59
     */
    public Set<J2eeModule.Type> getSupportedTypes() {
        Set<J2eeModule.Type> result = new HashSet<J2eeModule.Type>();
        for (Object obj : getSupportedModuleTypes()) {
            J2eeModule.Type type = J2eeModule.Type.fromJsrType(obj);
            if (type != null) {
                result.add(type);
            }
        }
        return result;
    }
    
    /**
     * Return a set of J2SE platform versions this J2EE platform can run with.
     * Versions should be specified as strings i.g. ("1.3", "1.4", etc.)
     *
     * @since 1.9
     */
    public abstract Set/*<String>*/ getSupportedJavaPlatformVersions();
    
    /**
     * Return server J2SE platform or null if the platform is unknown, not 
     * registered in the IDE.
     *
     * @return server J2SE platform or null if the platform is unknown, not 
     *         registered in the IDE.
     *
     * @since 1.9
     */
    public abstract JavaPlatform getJavaPlatform();
    
    /**
     * Register a listener which will be notified when some of the platform's properties
     * change.
     * 
     * @param l listener which should be added.
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (supp == null)
                supp = new PropertyChangeSupport(this);
        }
        supp.addPropertyChangeListener(l);
    }
    
    /**
     * Remove a listener registered previously.
     *
     * @param l listener which should be removed.
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        if (supp != null)
            supp.removePropertyChangeListener(l);
    }
    

    /** 
     * Fire PropertyChange to all registered PropertyChangeListeners.
     *
     * @param propName property name.
     * @param oldValue old value.
     * @param newValue new value.
     */
    public final void firePropertyChange(String propName, Object oldValue, Object newValue) {
        if (supp != null)
            supp.firePropertyChange(propName, oldValue, newValue);
    }

    /**
     * Returns the property value for the specified tool.
     * <p>
     * The property value uses Ant property format and therefore may contain 
     * references to another properties defined either by the client of this API 
     * or by the tool itself.
     * <p>
     * The properties the client may be requited to define are as follows
     * {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#CLIENT_PROP_DIST_ARCHIVE}
     * 
     * @param toolName tool name, for example {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#TOOL_APP_CLIENT_RUNTIME}.
     * @param propertyName tool property name, for example {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#TOOL_PROP_MAIN_CLASS}.
     *
     * @return property value or null, if the property is not defined for the 
     *         specified tool.
     *         
     * @since 1.16
     * @deprecated {@link #getLookup()} should be used to obtain tool specifics
     */
    @Deprecated
    public String getToolProperty(String toolName, String propertyName) {
        return null;
    }
    
    /**
     * Lookup providing a way to find non mandatory technologies supported
     * by the platform.
     * <div class="nonnormative">
     * The typical example of such support is a webservice stack.
     * </div>
     *
     * @return Lookup providing way to find other supported technologies
     * @since 1.44
     */
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

}
