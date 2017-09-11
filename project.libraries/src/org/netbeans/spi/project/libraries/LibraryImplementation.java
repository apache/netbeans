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

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;

/**
 * Base SPI interface for library. This SPI class is used as a model by the libraries framework.
 * The {@link LibraryTypeProvider} implementor should rather use
 * {@link org.netbeans.spi.project.libraries.support.LibrariesSupport#createLibraryImplementation}
 * factory method to create default LibraryImplementation than to implement this interface.
 */
public interface LibraryImplementation {

    String PROP_NAME = "name";                  //NOI18N
    String PROP_DESCRIPTION = "description";    //NOI18N
    String PROP_CONTENT = "content";            //NOI18N

    /**
     * Returns type of library, the LibraryTypeProvider creates libraries
     * of given unique type.
     * @return String unique identifier, never returns null.
     */
    String getType();

    /**
     * Returns name of the library
     * @return String unique name of the library, never returns null.
     */
    String getName();

    /**
     * Get a description of the library.
     * The description provides more detailed information about the library.
     * @return String the description or null if the description is not available
     */
    String getDescription();


    /**
     * Returns the resource name of the bundle which is used for localizing
     * the name and description. The bundle is located using the system ClassLoader.
     * @return String, the resource name of the bundle. If null in case when the
     * name and description is not localized.
     */
    String getLocalizingBundle();

    /**
     * Returns List of resources contained in the given volume.
     * The returned list is unmodifiable. To change the content of
     * the given volume use setContent method.
     * @param volumeType the type of volume for which the content should be returned.
     * @return list of resource URLs (never null)
     * @throws IllegalArgumentException if the library does not support given type of volume
     */
    List<URL> getContent(String volumeType) throws IllegalArgumentException;

    /**
     * Sets the name of the library, called by LibrariesStorage while reading the library
     * @param name -  the unique name of the library, can't be null.
     */
    void setName(String name);

    /**
     * Sets the description of the library, called by LibrariesStorage while reading the library
     * The description is more detailed information about the library.
     * @param text - the description of the library, may be null.
     */
    void setDescription(String text);

    /**
     * Sets the localizing bundle. The bundle is used for localizing the name and description.
     * The bundle is located using the system ClassLoader.
     * Called by LibrariesStorage while reading the library.
     * @param resourceName of the bundle without extension, may be null.
     */
    void setLocalizingBundle(String resourceName);

    /**
     * Adds PropertyChangeListener
     * @param l - the PropertyChangeListener
     */
    void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Removes PropertyChangeListener
     * @param l - - the PropertyChangeListener
     */
    void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Sets content of given volume
     * @param volumeType the type of volume for which the content should be set
     * @param path the list of resource URLs
     * @throws IllegalArgumentException if the library does not support given volumeType
     */
    void setContent(String volumeType, List<URL> path) throws IllegalArgumentException;

}
