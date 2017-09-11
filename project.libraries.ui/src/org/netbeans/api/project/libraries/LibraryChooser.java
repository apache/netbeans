/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.api.project.libraries;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Set;

/**
 * Visual picker for libraries.
 * @since org.netbeans.modules.project.libraries/1 1.16
 */
public final class LibraryChooser {

    private LibraryChooser() {
    }
    
    /**
     * Create a chooser showing libraries from given library manager and let the
     * user to pick some.
     * @param manager manager; can be null in which case global libraries are listed
     * @param filter optional libraries filter; null for no filtering
     * @param handler handler to perform library importing; can be null in which case
     *  import will not be allowed in UI
     * @return a nonempty set of libraries that were selected, or null if the dialog was cancelled
     */
    public static Set<Library> showDialog(LibraryManager manager, Filter filter, LibraryImportHandler handler) {
        return LibraryChooserGUI.showChooser(manager, filter, handler, true);
    }

    /**
     * Create a picker as an embeddable panel.
     * Might be used in a wizard, for example.
     * @param manager library manager to use or null for global libraries
     * @param filter optional libraries filter; null for no filtering
     * @return a panel controller
     */
    public static Panel createPanel(LibraryManager manager, Filter filter) {
        return LibraryChooserGUI.createPanel(manager, filter);
    }

    /**
     * Filter for use by {@link LibraryChooser#createPanel()} or 
     * {@link LibraryChooser#showDialog()}.
     */
    public interface Filter {

        /**
         * Accepts or rejects a library.
         * @param library a library found in one of the managers
         * @return true to display, false to hide
         */
        boolean accept(Library library);

    }

    /**
     * Represents operations permitted by {@link #createPanel}.
     * Not to be implemented by foreign code (methods may be added in the future).
     */
    public interface Panel {

        /**
         * Produces the actual component you can display.
         * @return an embeddable GUI component
         */
        Component getVisualComponent();

        /**
         * Gets the set of libraries which are currently selected.
         * @return a (possibly empty) set of libraries
         */
        Set<Library> getSelectedLibraries();

        /**
         * Property fired when {@link #getSelectedLibraries} changes.
         * Do not expect the old and new values to be non-null.
         */
        String PROP_SELECTED_LIBRARIES = "selectedLibraries"; // NOI18N

        /**
         * Add a listener for {@link #PROP_SELECTED_LIBRARIES}.
         * @param listener the listener to add
         */
        void addPropertyChangeListener(PropertyChangeListener listener);

        /**
         * Remove a listener.
         * @param listener the listener to remove
         */
        void removePropertyChangeListener(PropertyChangeListener listener);

    }

    /**
     * Handler for library importing. The handler is used from library chooser
     * UI in order to import global library to sharable libraries location. A 
     * library is only imported if there is no library with the same library
     * name in destination library manager.
     */
    public interface LibraryImportHandler {
        
        /**
         * Implementation is expected to copy given global library to 
         * sharable libraries location, that is to library manager the library
         * chooser was created for.
         * 
         * @param library library to copy
         * @return newly created library
         * @throws java.io.IOException any IO failure
         * @throws IllegalArgumentException if there already exists library 
         *  with this name
         */
        Library importLibrary(Library library) throws IOException;
    }
    
}
