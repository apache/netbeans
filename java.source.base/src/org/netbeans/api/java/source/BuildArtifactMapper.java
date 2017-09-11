/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.api.java.source;

import java.io.File;
import java.net.URL;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.modules.java.source.usages.BuildArtifactMapperImpl;

/**
 * @since 0.37
 * 
 * @author Jan Lahoda
 */
public class BuildArtifactMapper {

    /**
     * Add an {@link ArtifactsUpdated} listener. The method {@link ArtifactsUpdated#artifactsUpdated(java.lang.Iterable)}
     * will be called each time the files inside the output folder are updated.
     * The output folder computed for the source root using the {@link BinaryForSourceQuery}.
     * The files in the output folder are updated only if file <code>.netbeans_automatic_build</code>
     * exists inside the output folder.
     * 
     * @param sourceRoot the listener will be assigned to this source root
     * @param listener listener to add
     * @since 0.37
     */
    public static void addArtifactsUpdatedListener(@NonNull URL sourceRoot, @NonNull ArtifactsUpdated listener) {
        BuildArtifactMapperImpl.addArtifactsUpdatedListener(sourceRoot, listener);
    }
    
    /**
     * Remove an {@link ArtifactsUpdated} listener.
     *
     * @param sourceRoot the listener will be assigned to this source root
     * @param listener listener to add
     * @since 0.37
     */
    public static void removeArtifactsUpdatedListener(@NonNull URL sourceRoot, @NonNull ArtifactsUpdated listener) {
        BuildArtifactMapperImpl.removeArtifactsUpdatedListener(sourceRoot, listener);
    }

    /**
     * Notify that the files in the output directory has been updated.
     * @since 0.37
     */
    public static interface ArtifactsUpdated {
        /**
         * Notify that the files in the output directory has been updated.
         *
         * @param artifacts the updated files
         * @since 0.37
         */
        public void artifactsUpdated(@NonNull Iterable<File> artifacts);
    }
    
}
