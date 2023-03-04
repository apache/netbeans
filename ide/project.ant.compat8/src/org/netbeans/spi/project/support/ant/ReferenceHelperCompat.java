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
package org.netbeans.spi.project.support.ant;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.project.libraries.*;
import org.openide.modules.PatchFor;
import org.openide.util.*;

/**
 *
 * @author Tomas Zezula
 */
@PatchFor(ReferenceHelper.class)
public abstract class ReferenceHelperCompat {
    // must use reflection to avoid dependency cycle between compat8 > project.ant.ui > project.ant (= compat8)
    private static Method referenceHelperHandler;
    private static Method uriHandler;
    
    private static Method getRefHelperHandler() {
        if (referenceHelperHandler == null) {
            try {
                referenceHelperHandler = Lookup.getDefault().lookup(ClassLoader.class).loadClass("org.netbeans.spi.project.support.ant.ui.CustomizerUtilities").
                        getMethod("getLibraryChooserImportHandler", ReferenceHelper.class);
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return referenceHelperHandler;
    }

    private static Method getURIHandler() {
        if (uriHandler == null) {
            try {
                uriHandler = Lookup.getDefault().lookup(ClassLoader.class).loadClass("org.netbeans.spi.project.support.ant.ui.CustomizerUtilities").
                        getMethod("getLibraryChooserImportHandler", URI.class);
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return uriHandler;
    }

    /**
     * Returns library import handler which imports global library to sharable
     * one. See {@link LibraryChooser#showDialog} for usage of this handler.
     * @return copy handler
     * @since org.netbeans.modules.project.ant/1 1.19
     */
    public LibraryChooser.LibraryImportHandler getLibraryChooserImportHandler() {
        if (!ReferenceHelper.class.isInstance(this)) {
            throw new IllegalStateException("ReferenceHelperCompat can be extended only by ReferenceHelper");
        }
        try {
            return (LibraryChooser.LibraryImportHandler) getRefHelperHandler().invoke(null, ReferenceHelper.class.cast(this));
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Returns library import handler which imports global library to sharable
     * one. See {@link LibraryChooser#showDialog} for usage of this handler.
     * @param librariesLocation the location of the libraries definition file to import the library into
     * @return copy handler
     * @since org.netbeans.modules.project.ant/1 1.41
     */
    public LibraryChooser.LibraryImportHandler getLibraryChooserImportHandler(final URL librariesLocation) {
        try {
            return (LibraryChooser.LibraryImportHandler) getURIHandler().invoke(null, BaseUtilities.toFile(librariesLocation.toURI()));
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
