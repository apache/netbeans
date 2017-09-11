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

package org.netbeans.spi.project.libraries;

import java.beans.Customizer;
import org.openide.util.Lookup;

/**
 * SPI interface for provider of library type.
 * The LibraryTypeProvider is responsible for creating new libraries of given type
 * and for supplying the customizers of library's volumes.
 */
public interface LibraryTypeProvider extends Lookup.Provider {

    /**
     * Returns the UI name of the LibraryType.
     * This name is used in the UI while the libraryType is used as a system
     * identifier.
     * @return String the display name
     */
    public String getDisplayName ();

    /**
     * Get a unique identifier for the library type.
     * For example, <code>j2se</code>.
     * @return the unique library type identifier, never null
     */
    public String getLibraryType ();

    /**
     * Get identifiers for the volume types supported by the libraries created by this provider.
     * For example, <code>classpath</code>, <code>javadoc</code>, or <code>src</code>.
     * @return support volume type identifiers, never null, may be an empty array.
     */
    public String[] getSupportedVolumeTypes ();

    /**
     * Creates a new empty library implementation.
     * Generally will use {@link LibrariesSupport#createLibraryImplementation}.
     * This method is <strong>not</strong> used by {@link LibraryManager#createLibrary} except in the case of {@link LibraryManager#getDefault}.
     * @return the created library model, never null
     */
    public LibraryImplementation createLibrary ();


    /**
     * This method is called by the libraries framework when the library was deleted.
     * If the LibraryTypeProvider implementation requires clean of
     * additional settings (e.g. remove properties in the build.properties)
     * it should be done in this method.
     * This method is <strong>not</strong> used by {@link LibraryManager#createLibrary} except in the case of {@link LibraryManager#getDefault}.
     * @param libraryImpl
     */
    public void libraryDeleted (LibraryImplementation libraryImpl);


    /**
     * This method is called by the libraries framework when the library was created
     * and fully initialized (all its properties have to be read).
     * If the LibraryTypeProvider implementation requires initialization of
     * additional settings (e.g. adding properties into the build.properties)
     * it should be done in this method.
     * This method is <strong>not</strong> used by {@link LibraryManager#createLibrary} except in the case of {@link LibraryManager#getDefault}.
     */
    public void libraryCreated (LibraryImplementation libraryImpl);

    /**
     * Returns customizer for given volume's type, or null if the volume is not customizable.
     * The <code>LibraryCustomizerContext</code> instance is passed
     * to the customizer's setObject method.
     * The customized object describes the library created by this
     * provider, but the customizer cannot assume that the customized
     * object is of the same type as the object created by {@link #createLibrary}.
     * @param volumeType a type of volume listed in {@link #getSupportedVolumeTypes}
     * @return a customizer (must extend {@link javax.swing.JComponent}) or null if such
     *  customizer doesn't exist.
     */
    public Customizer getCustomizer (String volumeType);
    
}
