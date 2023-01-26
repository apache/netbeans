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

package org.netbeans.modules.form.project;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Describes a source (i.e. classpath) of a component class to be used in form
 * editor.
 *
 * @author Tomas Pavek, Jesse Glick
 */
public final class ClassSource {
    private final String className;
    private final String typeParameters;
    private final Collection<? extends Entry> entries;

    /**
     * @param className name of the class, can be null
     */
    public ClassSource(String className, Entry... entries) {
        this(className, Arrays.asList(entries));
    }
    public ClassSource(String className, Collection<? extends Entry> entries) {
        this(className, entries, null);
    }
    public ClassSource(String className, Collection<? extends Entry> entries, String typeParameters) {
        if (entries.contains(null)) {
            throw new IllegalArgumentException("entries contains null entry: "+entries);
        }
        this.className = className;
        this.entries = entries;
        this.typeParameters = typeParameters;
    }

    public String getClassName() {
        return className;
    }

    public String getTypeParameters() {
        return typeParameters;
    }

    public Collection<? extends Entry> getEntries() {
        return entries;
    }

    public boolean hasEntries() {
        return !entries.isEmpty();
    }

    /** Union of {@link ClassSource.Entry#getClasspath}. */
    public List<URL> getClasspath() {
        List<URL> cp = new ArrayList<>();
        for (Entry entry : entries) {
            cp.addAll(entry.getClasspath());
        }
        for (URL u : cp) {
            assert u.toExternalForm().endsWith("/") : u;
        }
        return cp;
    }

    /** Calls all {@link ClassSource.Entry#addToProjectClassPath} in turn. */
    public boolean addToProjectClassPath(FileObject projectArtifact, String classPathType) throws IOException, UnsupportedOperationException {
        for (Entry entry : entries) {
            if (!entry.addToProjectClassPath(projectArtifact, classPathType)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reconstruct from serialized form.
     * Used by {@link PaletteItemDataObject} for storing *.palette_item files.
     * @param type as in {@link Entry#getPicklingType}
     * @param name as in {@link Entry#getPicklingName}
     */
    public static Entry unpickle(String type, String name) {
        Resolver resolver = Lookup.getDefault().lookup(Resolver.class);
        return resolver != null ? resolver.resolve(type, name) : null;
    }

    /**
     * One logical component of the classpath.
     */
    public abstract static class Entry {
        /** List of folder URLs (dirs or roots of JARs) making up the classpath. */
        public abstract List<URL> getClasspath();
        /** Tries to add the classpath entries to a project, as with {@link ProjectClassPathModifier}. 
         * @return null if operation was aborted or true if classpath was modified or false if it was not
         */
        public abstract Boolean addToProjectClassPath(FileObject projectArtifact, String classPathType) throws IOException, UnsupportedOperationException;
        /** A label suitable for display. */
        public abstract String getDisplayName();
        /** @see #unpickle */
        public abstract String getPicklingType();
        /** @see #unpickle */
        public abstract String getPicklingName();
        public final @Override int hashCode() {
            return getClasspath().hashCode();
        }
        public final @Override boolean equals(Object obj) {
            return obj instanceof Entry && getClasspath().equals(((Entry) obj).getClasspath());
        }
        public final @Override String toString() {
            return super.toString() + getClasspath();
        }
    }
    
    public static interface Resolver {
        /**
         * Reconstruct from serialized form.
         * Used by {@link PaletteItemDataObject} for storing *.palette_item files.
         * @param type as in {@link Entry#getPicklingType}
         * @param name as in {@link Entry#getPicklingName}
         */
        Entry resolve(String type, String name);        
    }

}
