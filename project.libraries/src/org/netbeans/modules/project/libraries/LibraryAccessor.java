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

package org.netbeans.modules.project.libraries;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.libraries.ArealLibraryProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation2;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.openide.util.Exceptions;

public abstract class LibraryAccessor {
    
    private static volatile LibraryAccessor instance;
    
    public static synchronized LibraryAccessor getInstance () {
        if (instance == null) {
            try {
                Object o = Class.forName("org.netbeans.api.project.libraries.Library",true,LibraryAccessor.class.getClassLoader());
            } catch (ClassNotFoundException cnf) {
                Exceptions.printStackTrace(cnf);
            }
        }
        assert instance != null;
        return instance;
    }
    
    public static void setInstance (final LibraryAccessor _instance) {
        assert _instance != null;
        instance = _instance;
    }
    
    public abstract Library createLibrary (LibraryImplementation libraryImplementation);

    @NonNull
    public abstract LibraryStorageArea getArea(@NonNull LibraryManager manager);

    @NonNull
    public abstract LibraryImplementation getLibraryImplementation(@NonNull Library library);

    // RADIKAL GENERIC HAX!

    /**
     * Type-safe accessor for {@link ArealLibraryProvider#remove}.
     * @throws ClassCastException if the runtime types do not match
     */
    public static void remove(ArealLibraryProvider alp, LibraryImplementation2 lib) throws IOException {
        remove0((ArealLibraryProvider<?,?>) alp, lib);
    }
    private static <L extends LibraryImplementation2> void remove0(ArealLibraryProvider<?,L> alp, LibraryImplementation2 lib) throws IOException {
        alp.remove(alp.libraryType().cast(lib));
    }

    /**
     * Type-safe accessor for {@link ArealLibraryProvider#getOpenAreas}.
     */
    public static Set<? extends LibraryStorageArea> getOpenAreas(ArealLibraryProvider alp) {
        return ((ArealLibraryProvider<?,?>) alp).getOpenAreas();
    }

    /**
     * Type-safe accessor for {@link ArealLibraryProvider#createLibrary}.
     * @throws ClassCastException if the runtime types do not match
     */
    public static LibraryImplementation2 createLibrary(ArealLibraryProvider alp, String type, String name, LibraryStorageArea area, Map<String,List<URI>> contents) throws IOException {
        return createLibrary0(((ArealLibraryProvider<?,?>) alp), type, name, area, contents);
    }
    private static <A extends LibraryStorageArea> LibraryImplementation2 createLibrary0(ArealLibraryProvider<A,?> alp, String type, String name, LibraryStorageArea area, Map<String,List<URI>> contents) throws IOException {
        return alp.createLibrary(type, name, alp.areaType().cast(area), contents);
    }

    /**
     * Type-safe accessor for {@link ArealLibraryProvider#getLibraries}.
     * @throws ClassCastException if the runtime types do not match
     */
    public static LibraryProvider getLibraries(ArealLibraryProvider alp, LibraryStorageArea area) {
        return getLibraries0((ArealLibraryProvider<?,?>) alp, area);
    }
    private static <A extends LibraryStorageArea> LibraryProvider getLibraries0(ArealLibraryProvider<A,?> alp, LibraryStorageArea area) {
        return alp.getLibraries(alp.areaType().cast(area));
    }

}
