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

import java.io.File;
import java.net.URI;
import java.util.logging.Logger;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.BaseUtilities;

// XXX perhaps should be in the Filesystems API instead of here?

/**
 * Determine whether files should be shared (for example in a VCS) or are intended
 * to be unshared.
 * Likely to be of use only to a VCS filesystem.
 * <p>
 * This query can be considered to obsolete {@link org.openide.filesystems.FileObject#setImportant}.
 * Unlike that method, the information is pulled by the VCS filesystem on
 * demand, which may be more reliable than ensuring that the information
 * is pushed by a project type (or other implementor) eagerly.
 * @see SharabilityQueryImplementation2
 * @author Jesse Glick
 */
@SuppressWarnings("deprecation")
public final class SharabilityQuery {
    
    private static final Lookup.Result<org.netbeans.spi.queries.SharabilityQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(org.netbeans.spi.queries.SharabilityQueryImplementation.class);

    private static final Lookup.Result<SharabilityQueryImplementation2> implementations2 =
        Lookup.getDefault().lookupResult(SharabilityQueryImplementation2.class);
    
    private static final Logger LOG = Logger.getLogger(SharabilityQuery.class.getName());

    /**
     * Constant indicating that nothing is known about whether a given
     * file should be considered sharable or not.
     * A client should therefore behave in the safest way it can.
     * @deprecated Use {@link org.netbeans.api.queries.SharabilityQuery.Sharability#UNKNOWN} instead.
     */
    @Deprecated public static final int UNKNOWN = 0;
    
    /**
     * Constant indicating that the file or directory is sharable.
     * In the case of a directory, this means that all files and
     * directories recursively contained in this directory are also
     * sharable.
     * @deprecated Use {@link org.netbeans.api.queries.SharabilityQuery.Sharability#SHARABLE} instead.
     */
    @Deprecated public static final int SHARABLE = 1;
    
    /**
     * Constant indicating that the file or directory is not sharable.
     * In the case of a directory, this means that all files and
     * directories recursively contained in this directory are also
     * not sharable.
     * @deprecated Use {@link org.netbeans.api.queries.SharabilityQuery.Sharability#NOT_SHARABLE} instead.
     */
    @Deprecated public static final int NOT_SHARABLE = 2;
    
    /**
     * Constant indicating that a directory is sharable but files and
     * directories recursively contained in it may or may not be sharable.
     * A client interested in children of this directory should explicitly
     * ask about each in turn.
     * @deprecated Use {@link org.netbeans.api.queries.SharabilityQuery.Sharability#MIXED} instead.
     */
    @Deprecated public static final int MIXED = 3;

    /**
     * Sharability constants.
     * @since 1.27
     */
    public enum Sharability {
        /**
        * Constant indicating that nothing is known about whether a given
        * file should be considered sharable or not.
        * A client should therefore behave in the safest way it can.
        */
        UNKNOWN,

        /**
        * Constant indicating that the file or directory is sharable.
        * In the case of a directory, this means that all files and
        * directories recursively contained in this directory are also
        * sharable.
        */
        SHARABLE,

        /**
        * Constant indicating that the file or directory is not sharable.
        * In the case of a directory, this means that all files and
        * directories recursively contained in this directory are also
        * not sharable.
        */
        NOT_SHARABLE,

        /**
        * Constant indicating that a directory is sharable but files and
        * directories recursively contained in it may or may not be sharable.
        * A client interested in children of this directory should explicitly
        * ask about each in turn.
        */
        MIXED;
    }
    
    private SharabilityQuery() {}
    
    /**
     * Check whether an existing file is sharable.
     * @param file a file or directory (may or may not already exist); should be {@linkplain FileUtil#normalizeFile normalized}
     * @return an answer or {@code UNKNOWN}
     * @deprecated Use {@link #getSharability(java.net.URI)} instead.
     */
    @Deprecated public static int getSharability(File file) {
        Parameters.notNull("file", file);
        boolean asserts = false;
        assert asserts = true;
        if (asserts && !BaseUtilities.isMac()) {
            File normFile = FileUtil.normalizeFile(file);
            if (!file.equals(normFile)) {
                throw new IllegalArgumentException("Must pass a normalized file: " + file + " vs. " + normFile);
            }
        }
        URI uri = null;
        for (SharabilityQueryImplementation2 sqi : implementations2.allInstances()) {
            if (uri == null) {
                uri = BaseUtilities.toURI(file);
            }
            Sharability x = sqi.getSharability(uri);
            if (x != Sharability.UNKNOWN) {
                return x.ordinal();
            }
        }
        for (org.netbeans.spi.queries.SharabilityQueryImplementation sqi : implementations.allInstances()) {
            int x = sqi.getSharability(file);
            if (x != UNKNOWN) {
                return x;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * Check whether an existing file is sharable.
     * @param uri a file or directory (may or may not already exist); should be normalized.
     * @return an answer or {@code UNKNOWN}
     * @since 1.27
     */
    public static Sharability getSharability(URI uri) {
        Parameters.notNull("uri", uri);
        boolean asserts = false;
        assert asserts = true;
        if (asserts) {
            URI normUri = uri.normalize();
            if (!uri.equals(normUri)) {
                throw new IllegalArgumentException("Must pass a normalized URI: " + uri + " vs. " + normUri);
            }
        }
        for (SharabilityQueryImplementation2 sqi : implementations2.allInstances()) {
            Sharability x = sqi.getSharability(uri);
            if (x != Sharability.UNKNOWN) {
                return x;
            }
        }
        if ("file".equals(uri.getScheme())) { // NOI18N
            File file = FileUtil.normalizeFile(BaseUtilities.toFile(uri));
            for (org.netbeans.spi.queries.SharabilityQueryImplementation sqi : implementations.allInstances()) {
                int x = sqi.getSharability(file);
                if (x != UNKNOWN) {
                    return Sharability.values()[x];
                }
            }
        }
        return Sharability.UNKNOWN;
    }
    
    /**
     * Check whether an existing file is sharable.
     * @param fo a file or directory; should be normalized.
     * @return an answer or {@code UNKNOWN}
     * @since 1.27
     */
    public static Sharability getSharability(FileObject fo) {
        return getSharability(fo.toURI());
    }
}
