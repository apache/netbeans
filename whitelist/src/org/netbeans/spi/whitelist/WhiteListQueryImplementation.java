/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.spi.whitelist;

import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.openide.filesystems.FileObject;

/**
 * Permits projects to provide wite list used to emit errors for usages of non allowed
 * types or methods. The project types supporting a runtime with class usage
 * restriction implement this interface to provide a white list of allowed types (methods).
 * @author Tomas Zezula
 * @author David Konecny
 */
public interface WhiteListQueryImplementation {

    /**
     * Returns a white list for given file.
     * @param file to return white list for.
     * @return the {@link WhiteListImplementation} for given file or null if no white list
     * is associated with given file.
     */
    @CheckForNull
    WhiteListImplementation getWhiteList(
          @NonNull FileObject file);

    /**
     * All white lists which should be displayed in UI for user should implement
     * this interface. And on the other hand non-visible implementations like
     * for example merging WhiteListQueryImplementation should not implement it.
     */
    public interface UserSelectable extends WhiteListQueryImplementation {
        String getDisplayName();
        String getId();
    }

    /**
     * The white list implementation used to emit errors for usages of non allowed
     * types or methods.
     */
    public interface WhiteListImplementation {

        /**
         * Checks if given method (type) can be invoked (accessed).
         * @param element to check
         * @param operation the operation which should be tested
         * @return a {@link Result} holding the details.
         */
        @NonNull
        WhiteListQuery.Result check(@NonNull ElementHandle<?> element, @NonNull WhiteListQuery.Operation operation);

        /**
         * Adds {@link ChangeListener} to white list.
         * @param listener to be added
         */
        void addChangeListener(@NonNull final ChangeListener listener);

        /**
         * Removes {@link ChangeListener} from white list.
         * @param listener to be removed
         */
        void removeChangeListener(@NonNull final ChangeListener listener);
    }

}
