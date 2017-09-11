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

package org.netbeans.api.queries;

import javax.swing.event.ChangeListener;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

// XXX is the Status interface scalable enough for efficient use even from e.g. a Look?
// May need to be revisited, perhaps with an optional more efficient implementation...

/**
 * Test whether a file can be considered to be built (up to date).
 * @see FileBuiltQueryImplementation
 * @author Jesse Glick
 */
public final class FileBuiltQuery {
    
    private static final Lookup.Result<FileBuiltQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(FileBuiltQueryImplementation.class);
    
    private FileBuiltQuery() {}
    
    /**
     * Check whether a (source) file has been <em>somehow</em> built
     * or processed.
     * <div class="nonnormative">
     * <p>
     * This would typically mean that at least its syntax has been
     * validated by a build system, some conventional output file exists
     * and is at least as new as the source file, etc.
     * For example, for a <samp>Foo.java</samp> source file, this could
     * check whether <samp>Foo.class</samp> exists (in the appropriate
     * build directory) with at least as new a timestamp.
     * </p>
     * <p>
     * <strong>Implementation note:</strong> the current implementation of this
     * method does not react to changes in lookup results for
     * {@link FileBuiltQueryImplementation}. For example, if there is initially
     * no provider for a given file, the return value may be null, and a client
     * will not be see the change if a provider is later installed dynamically.
     * Similarly, removal of a provider will not automatically invalidate an
     * existing {@link Status} object; and a change in the provider responsible
     * for a given file will not produce updates in an existing {@link Status}.
     * A future implementation may however be enhanced to return proxy statuses
     * which react to changes in the provider responsible for the file and always
     * delegate to the current provider, if there is one.
     * </p>
     * </div>
     * @param file a source file which can be built to a direct product
     * @return a status object that can be listened to, or null for no answer
     */
    public static Status getStatus(FileObject file) {
        if (!file.isValid()) {
            // Probably a race condition of some kind, abort gracefully.
            return null;
        }
        for (FileBuiltQueryImplementation fbqi : implementations.allInstances()) {
            Status s = fbqi.getStatus(file);
            if (s != null) {
                return s;
            }
        }
        return null;
    }
    
    /**
     * Result of getting built status for a file.
     * Besides encoding the actual result, it permits listening to changes.
     * @see #getStatus
     */
    public interface Status {
        
        /**
         * Check whether the file is currently built.
         * @return true if it is up-to-date, false if it may still need to be built
         */
        boolean isBuilt();
        
        /**
         * Add a listener to changes.
         * @param l a listener to add
         */
        void addChangeListener(ChangeListener l);
        
        /**
         * Stop listening to changes.
         * @param l a listener to remove
         */
        void removeChangeListener(ChangeListener l);
        
    }
    
}
