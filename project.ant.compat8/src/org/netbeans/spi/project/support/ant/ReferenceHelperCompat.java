/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
