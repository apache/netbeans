/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.form.project;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
        List<URL> cp = new ArrayList<URL>();
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
    public static abstract class Entry {
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
