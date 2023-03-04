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
package org.openide.modules;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Set;

/** General information about a module.
 * Immutable from an API perspective, serves as
 * a source of information only.
 * All instances may be gotten via lookup.
 * It is forbidden for module code to register instances of this class.
 * @author Jesse Glick
 * @since 1.24
 */
public abstract class ModuleInfo {
    /** Property name fired when enabled or disabled.
     * For changes in other attributes, property name
     * can match manifest attribute name, for example
     * OpenIDE-Module-Specification-Version after upgrade.
     */
    public static final String PROP_ENABLED = "enabled"; // NOI18N
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /** Do-nothing constructor. */
    protected ModuleInfo() {
    }

    /** The code name of the module, sans release version. */
    public abstract String getCodeNameBase();

    /** The release version (-1 if undefined). */
    public abstract int getCodeNameRelease();

    /** The full code name, with release version after slash if defined. */
    public abstract String getCodeName();

    /** Get a localized display name, if available.
     * As a fallback provides the code name (base).
     * Convenience method only.
     */
    public String getDisplayName() {
        String dn = (String) getLocalizedAttribute("OpenIDE-Module-Name"); // NOI18N

        if (dn != null) {
            return dn;
        }

        return getCodeNameBase();
    }

    /** The specification version, or null. */
    public abstract SpecificationVersion getSpecificationVersion();

    /** The implementation version, or null.
     * Convenience method only.
     */
    public String getImplementationVersion() {
        return (String) getAttribute("OpenIDE-Module-Implementation-Version"); // NOI18N
    }

    /** The identification of the build version. Usually build number.
     * If no specific build version is provided then this method delegates to {@link #getImplementationVersion}.
     *
     * @return textual identification of build version or the value for implementation version
     * @since 4.18
     */
    public String getBuildVersion() {
        String bld = (String) getAttribute("OpenIDE-Module-Build-Version"); // NOI18N

        return (bld == null) ? getImplementationVersion() : bld;
    }

    /** Whether the module is currently enabled. */
    public abstract boolean isEnabled();

    /** Get some attribute, for example OpenIDE-Module-Name.
     * Not all manifest attributes need be supported here.
     * Attributes not present in the manifest may be available.
     */
    public abstract Object getAttribute(String attr);

    /** Get an attribute with localization.
     * That is, if there is a suitable locale variant of the attribute
     * name, return its value rather than the value of the base attribute.
     */
    public abstract Object getLocalizedAttribute(String attr);

    /** Add a change listener. */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        if (l == null) {
            throw new NullPointerException(
                "If you see this stack trace, please attach to: http://www.netbeans.org/issues/show_bug.cgi?id=22379"
            ); // NOI18N
        }

        changeSupport.addPropertyChangeListener(l);
    }

    /** Remove a change listener. */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    /** Indicate that something changed, as a subclass.
     * Changes are fired synchronously (but this method need not be called synchronously).
     */
    protected final void firePropertyChange(String prop, Object old, Object nue) {
        changeSupport.firePropertyChange(prop, old, nue);
    }

    /** Get a list of all dependencies this module has. */
    public abstract Set<Dependency> getDependencies();

    /** Determine if the provided class
     * was loaded as a part of this module, and thus will only be
     * loadable later if this module is enabled.
     * If in doubt, return <code>false</code>.
     * @see Modules#ownerOf
     * @since 1.28
     */
    public abstract boolean owns(Class<?> clazz);

    /**
     * Get a class loader associated with this module that can load
     * classes defined in the module.
     * <p>
     * You can only call this method on an enabled module, and the
     * result may change if the module is disabled and re&euml;nabled.
     * <p>
     * The class loader may or may not be shared with any other
     * module, or be the application's startup class loader, etc.
     * <p>
     * For reasons of backward compatibility, this method is not abstract
     * but will throw {@link UnsupportedOperationException} if not
     * overridden. The instances obtainable from default lookup will
     * override the method to return a real value.
     * @return a module class loader
     * @throws IllegalArgumentException if this module is disabled
     * @since 4.21
     */
    public ClassLoader getClassLoader() throws IllegalArgumentException {
        throw new UnsupportedOperationException("Must be overridden"); // NOI18N
    }

    /** Get a set of capabilities which this module provides to others that may
     * require it.
     * The default implementation returns an empty array.
     * @return an array of tokens, possibly empty but not null
     * @since 2.3
     */
    public String[] getProvides() {
        return new String[] {  };
    }
}
