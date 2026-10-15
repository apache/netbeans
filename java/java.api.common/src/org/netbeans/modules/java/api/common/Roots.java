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

package org.netbeans.modules.java.api.common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.api.common.impl.RootsAccessor;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.util.Parameters;

/**
 * Represents a list of root properties used by {@link org.netbeans.modules.java.api.common.queries.QuerySupport#createSources}
 * @since 1.21
 * @author Tomas Zezula
 */
public abstract class Roots {

    private final PropertyChangeSupport support;
    private final boolean isSourceRoot;
    private final boolean supportIncludes;
    private final String type;
    private final String hint;

    static {
        RootsAccessor.setInstance(new MyAccessor());
    }

    Roots (final boolean isSourceRoot,
           final boolean supportIncludes,
           final @NullAllowed String type,
           final @NullAllowed String hint) {
        this.isSourceRoot = isSourceRoot;
        this.supportIncludes = supportIncludes;
        this.type = type;
        this.hint = hint;
        this.support = new PropertyChangeSupport(this);
    }

    /**
     * Returns root's display names
     * @return an array of String
     */
    public abstract @NonNull String[] getRootDisplayNames();

    /**
     * Returns names of Ant properties in the <i>project.properties</i> file holding the roots.
     * @return an array of String.
     */
    public abstract @NonNull String[] getRootProperties();

    /**
     * Adds {@link PropertyChangeListener}, see class description for more information
     * about listening to the source roots changes.
     * @param listener a listener to add.
     */
    public final void addPropertyChangeListener(final @NonNull PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.support.addPropertyChangeListener(listener);
    }

    /**
     * Removes {@link PropertyChangeListener}, see class description for more information
     * about listening to the source roots changes.
     * @param listener a listener to remove.
     */
    public final void removePropertyChangeListener(final @NonNull PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.support.removePropertyChangeListener(listener);
    }

    final void firePropertyChange(final @NonNull String propName, final @NullAllowed Object oldValue, final @NonNull Object newValue) {
        Parameters.notNull("propName", propName);   //NOI18N
        this.support.firePropertyChange(propName, oldValue, newValue);
    }

    /**
     * Creates roots list which should be registered as non source roots.
     * @see SourcesHelper#addNonSourceRoot(java.lang.String) for details
     * @param rootPropNames Ant properties in the <i>project.properties</i> file holding the roots
     * @return the Roots
     */
    public static Roots nonSourceRoots(final @NonNull String... rootPropNames) {
        Parameters.notNull("rootPropNames", rootPropNames); //NOI18N
        return new NonSourceRoots(rootPropNames);
    }

    /**
     * Creates a source roots list which should be registered as principal and
     * possibly typed roots.
     * @see SourcesHelper for details.
     * @param properties Ant properties in the <i>project.properties</i> file holding the roots
     * @param displayNames the display names of the roots
     * @param supportIncludes when true the roots list supports includes/excludes
     * @param type of the roots, when null the roots are registered as principal roots only
     * @param hint optional hint for {@link org.netbeans.api.project.SourceGroupModifier}
     * @return the Roots
     */
    public static Roots propertyBased(
            final @NonNull String[] properties,
            final @NonNull String[] displayNames,
            final boolean supportIncludes,
            final @NullAllowed String type,
            final @NullAllowed String hint) {
        Parameters.notNull("properties", properties);
        Parameters.notNull("displayNames", displayNames);
        if (properties.length != displayNames.length) {
            throw new IllegalArgumentException();
        }
        return new PropSourceRoots(properties, displayNames, supportIncludes, type, hint);
    }

    private static class NonSourceRoots extends Roots {

        private final Set<String> rootPropNames;


        private NonSourceRoots(final String... rootPropNames) {
            super(false,false, null, null);
            this.rootPropNames = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(rootPropNames)));
        }

        @Override
        public String[] getRootDisplayNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getRootProperties() {
            return rootPropNames.toArray(new String[0]);
        }
    }

    private static class PropSourceRoots extends Roots {

        private final String[] props;
        private final String[] names;

        private PropSourceRoots (final String[] props, final String[] names,
                final boolean supportIncludes, final String type, final String hint) {
            super (true,supportIncludes,type,hint);
            this.props = Arrays.copyOf(props, props.length);
            this.names = Arrays.copyOf(names, names.length);
        }

        @Override
        public String[] getRootDisplayNames() {
            return Arrays.copyOf(names, names.length);
        }

        @Override
        public String[] getRootProperties() {
            return Arrays.copyOf(props, props.length);
        }
    }

    private static class MyAccessor extends RootsAccessor {
        @Override
        public boolean isSourceRoot(final @NonNull Roots roots) {
            return roots.isSourceRoot;
        }

        @Override
        public boolean supportIncludes(final @NonNull Roots roots) {
            return roots.supportIncludes;
        }

        @Override
        public String getHint(final @NonNull Roots roots) {
            return roots.hint;
        }

        @Override
        public String getType(final @NonNull Roots roots) {
            return roots.type;
        }

        @Override
        public String[] getRootPathProperties(final @NonNull Roots roots) {
            return roots instanceof SourceRoots ? ((SourceRoots)roots).getRootPathProperties() : new String[roots.getRootProperties().length];
        }
    }

}
