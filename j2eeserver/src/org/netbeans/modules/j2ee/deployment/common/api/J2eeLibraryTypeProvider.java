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

package org.netbeans.modules.j2ee.deployment.common.api;

import java.beans.Customizer;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * The J2eeLibraryTypeProvider defines a j2ee library type and is responsible for
 * creating new libraries of this type.
 *
 * @author Stepan Herold
 * @since 1.5
 */
public final class J2eeLibraryTypeProvider implements LibraryTypeProvider {
    
    /** J2ee library type */
    public static final String LIBRARY_TYPE = "j2ee";                   //NOI18N
    
    /** Classpath volume type */
    public static final String VOLUME_TYPE_CLASSPATH = "classpath";     //NOI18N
    /** Source volume type */
    public static final String VOLUME_TYPE_SRC = "src";                 //NOI18N
    /** Javadoc volume type */
    public static final String VOLUME_TYPE_JAVADOC = "javadoc";         //NOI18N
    
    static final String[] VOLUME_TYPES = new String[] {
        VOLUME_TYPE_CLASSPATH,
        VOLUME_TYPE_SRC,
        VOLUME_TYPE_JAVADOC
    };

    
    /** Creates a new instance of J2eeLibraryTypeProvider */
    public J2eeLibraryTypeProvider() {
    }
    
    /**
     * Returns the UI name of the LibraryType.
     * This name is used in the UI while the libraryType is used as a system
     * identifier.
     * @return String the display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage(J2eeLibraryTypeProvider.class,"TXT_J2eeLibraryType"); //NOI18N
    }

    /**
     * Get a j2ee library type identifier.
     *
     * @return the j2ee library type identifier.
     */
    public String getLibraryType() {
        return LIBRARY_TYPE;
    }

    /**
     * Return supported volume types: <code>classpath</code>, <code>javadoc</code>, 
     * <code>src</code>.
     *
     * @return support volume types.
     */
    public String[] getSupportedVolumeTypes() {
        return VOLUME_TYPES;
    }

    /**
     * Creates a new implementation of j2ee library type.
     *
     * @return the created library model, never null
     */
    public LibraryImplementation createLibrary() {
        return LibrariesSupport.createLibraryImplementation(LIBRARY_TYPE, VOLUME_TYPES);
    }

    /**
     * Does nothing now.
     */
    public void libraryDeleted(LibraryImplementation libraryImpl) {
    }

    /**
     * Does nothing now.
     */
    public void libraryCreated(LibraryImplementation libraryImpl) {
    }

    /**
     * Currently returns <code>null</code>.
     */
    public Customizer getCustomizer(String volumeType) {
        return null;
    }
    
    /**
     * Returns empty lookup.
     * @return empty lookup.
     */
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }
}
