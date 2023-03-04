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

package org.netbeans.api.java.platform;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

/**
 * JavaPlatform describes a java platform in a way that the IDE tools may utilize. It may serve as
 * description of the platform a java project targets, or it may provide access to tools from the
 * particular SDK installation. It also provides information about individual platforms, for example
 * the Java platform version implemented, vendor name or implementation version. It is also possible
 * to enumerate services that the IDE supports, which are implemented as a part of the Platform.
 *
 * @author Radko Najman, Svata Dedic, Tomas Zezula
 */
public abstract class JavaPlatform {

    //List of names of mutable properties
    /**
     * Property name for displayName
     */
    public static final String PROP_DISPLAY_NAME = "displayName";           //NOI18N
    
    /**
     * Property name for sourceFolders
     */
    public static final String PROP_SOURCE_FOLDER = "sourceFolders";         //NOI18N
    
    /**
     * Property name for javadocFolders
     */
    public static final String PROP_JAVADOC_FOLDER ="javadocFolders";        //NOI18N
    
    /**
     * Property name for systemProperties
     */
    public static final String PROP_SYSTEM_PROPERTIES = "systemProperties";  //NOI18N

    private Map<String,String> sysproperties = Collections.emptyMap();
    private PropertyChangeSupport supp;
    
    /** Creates a new instance of JavaPlatform */
    protected JavaPlatform() {
    }

    /**
     * @return  a descriptive, human-readable name of the platform
     */
    public abstract String getDisplayName();

    /**
     * Registers a listener to be notified when some of the platform's properties
     * change
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (supp == null)
                supp = new PropertyChangeSupport(this);
        }
        supp.addPropertyChangeListener(l);
    }
    
    /**
     * Removes a listener registered previously
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        if (supp != null)
            supp.removePropertyChangeListener(l);
    }
    
    /**
     * Gets some ad-hoc properties defined for this platform.
     * The precise set of properties is not specified.
     * <p class="nonnormative">
     * Implementations are however advised to include the key
     * <code>platform.ant.name</code> if they wish to be used in Ant builds;
     * the value <code>default_platform</code> is conventionally associated
     * with the default platform.
     * </p>
     * @return some properties
     */
    public abstract Map<String,String> getProperties();
    
    /** Gets the java platform system properties.
     * @return the java platform system properties
     */
    public final Map<String,String> getSystemProperties() {
        return sysproperties;
    }

    /**
     * Returns a ClassPath, which represents bootstrap libraries for the
     * runtime environment. The Bootstrap libraries include libraries in 
     * JRE's extension directory, if there are any.
     * @return ClassPath representing the bootstrap libs
     */
    public abstract ClassPath getBootstrapLibraries();
    
    /**
     * Returns libraries recognized by default by the platform. Usually
     * it corresponds to contents of CLASSPATH environment variable.
     */
    public abstract ClassPath getStandardLibraries();

    /**
     * Returns the vendor of the Java SDK
     * @return String
     */
    public abstract String getVendor ();

    /**
     * Returns specification of the Java SDK
     * @return Specification
     */
    public abstract Specification getSpecification ();

    /**
     * Retrieves a collection of {@link FileObject}s of one or more folders
     * where the Platform is installed. Typically it returns one folder, but
     * in some cases there can be more of them.
     * @return collection of installation folders (should be nonempty unless platform is broken)
     */
    public abstract Collection<FileObject> getInstallFolders();

    /**
     * Gets the platform tool executable.
     * @param toolName the tool platform independent tool name.
     * @return file representing the tool executable, or null if the tool can not be found
     */
    public abstract FileObject findTool (String toolName);


    /**
     * Returns the locations of the source of platform
     * or empty collection when the location is not set or is invalid
     * @return ClassPath never returns null
     */
    public abstract ClassPath getSourceFolders ();

    /**
     * Returns the locations of the Javadoc for this platform
     * or empty collection if the location is not set or invalid
     * @return (non-null) list of locations
     */
    public abstract List<URL> getJavadocFolders ();


    /**
     * Checks if the {@link JavaPlatform} is valid.
     * @return true if the {@link JavaPlatform} is valid,
     * false when broken.
     * @since 1.43
     */
    public boolean isValid() {
        return !getInstallFolders().isEmpty();
    }


    /**
     * Get the "default platform", meaning the JDK on which NetBeans itself is running.
     * @return the default platform, if it can be found, or null
     * @see JavaPlatformManager#getDefaultPlatform
     */
    public static JavaPlatform getDefault() {
        return JavaPlatformManager.getDefault().getDefaultPlatform();
    }


    //SPI methods

    /** Fires PropertyChange to all registered PropertyChangeListeners
     * @param propName
     * @param oldValue
     * @param newValue
     */
    protected final void firePropertyChange(String propName, Object oldValue, Object newValue) {
        if (supp != null)
            supp.firePropertyChange(propName, oldValue, newValue);
    }

    /** Sets the system properties of java platform.
     * @param sysproperties the java platform system properties
     */
    protected final void setSystemProperties(Map<String,String> sysproperties) {
        this.sysproperties = Collections.unmodifiableMap(sysproperties);
        firePropertyChange(PROP_SYSTEM_PROPERTIES, null, null); // NOI18N
    }

 }
