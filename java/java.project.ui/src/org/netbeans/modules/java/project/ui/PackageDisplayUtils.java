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

package org.netbeans.modules.java.project.ui;

import java.awt.Image;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.queries.AccessibilityQuery;
import org.netbeans.api.java.queries.AccessibilityQuery.Accessibility;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

// XXX needs unit test

/**
 * Provides display name and icon utilities for
 * {@link PackageViewChildren.PackageNode} and {@link PackageListView.PackageItem}.
 * @author Jesse Glick
 */
public final class PackageDisplayUtils {

    private PackageDisplayUtils() {}

    /** whether to turn on #42589 */
    private static final boolean TRUNCATE_PACKAGE_NAMES =
        Boolean.getBoolean("org.netbeans.spi.java.project.support.ui.packageView.TRUNCATE_PACKAGE_NAMES"); // NOI18N

    public static final /* XXX #7116293 @StaticResource */ String PACKAGE = "org/netbeans/spi/java/project/support/ui/package.gif";
    private static final @StaticResource String PACKAGE_EMPTY = "org/netbeans/spi/java/project/support/ui/packageEmpty.gif";
    private static final @StaticResource String PACKAGE_PRIVATE = "org/netbeans/spi/java/project/support/ui/packagePrivate.gif";
    private static final @StaticResource String PACKAGE_PUBLIC = "org/netbeans/spi/java/project/support/ui/packagePublic.gif";

    /**
     * Find the proper display label for a package.
     * @param pkg the actual folder
     * @param pkgname the dot-separated package name (<code>""</code> for default package)
     * @return an appropriate display label for it
     */
    public static String getDisplayLabel(String pkgname) {
        return computePackageName(pkgname, TRUNCATE_PACKAGE_NAMES);
    }
    
    /**
     * Find the proper tool tip for a package.
     * May have more info than the display label.
     * @param pkg the actual folder
     * @param pkgname the dot-separated package name (<code>""</code> for default package)
     * @return an appropriate display label for it
     */
    public static String getToolTip(FileObject pkg, String pkgname) {
        String pkglabel = computePackageName(pkgname, false);
        Boolean b = AccessibilityQuery.isPubliclyAccessible(pkg);
        if (b != null) {
            if (b.booleanValue()) {
                return NbBundle.getMessage(PackageDisplayUtils.class, "LBL_public_package", pkglabel);
            } else {
                return NbBundle.getMessage(PackageDisplayUtils.class, "LBL_private_package", pkglabel);
            }
        } else {
            return NbBundle.getMessage(PackageDisplayUtils.class, "LBL_package", pkglabel);
        }
    }
    
    /**
     * Get package name.
     * Handles default package specially.
     * @param truncate if true, show a truncated version to save display space
     */
    private static String computePackageName(String pkgname, boolean truncate) {
        if (pkgname.length() == 0) {
            return NbBundle.getMessage(PackageDisplayUtils.class, "LBL_DefaultPackage"); // NOI18N
        } else {
            if (truncate) {
                // #42589: keep only first letter of first package component, up to three of others
                return pkgname.replaceFirst("^([^.])[^.]+\\.", "$1.").replaceAll("([^.]{3})[^.]+\\.", "$1."); // NOI18N
            } else {
                return pkgname;
            }
        }
    }

    /**
     * Find the proper display icon for a package.
     * @param pkg the actual folder
     * @param empty the performance optimization if the isEmpty status is already known
     * @return an appropriate display icon for it
     */
    public static Image getIcon(
            @NonNull final FileObject pkg,
            final boolean empty) {
        return getIcon(pkg,
                empty,
                () -> AccessibilityQuery.isPubliclyAccessible2(pkg).getAccessibility());
    }

    public static Image getIcon(
            @NonNull final FileObject pkg,
            final boolean empty,
            @NonNull Callable<Accessibility> accessibilityProvider) {
        if ( empty ) {
            return ImageUtilities.loadImage(PACKAGE_EMPTY);
        } else {
            Accessibility a;
            try {
                a = pkg.isValid() ?  accessibilityProvider.call() : Accessibility.UNKNOWN;
            } catch (Exception e) {
                a = Accessibility.UNKNOWN;
            }
            switch (a) {
                case EXPORTED:
                    return ImageUtilities.loadImage(PACKAGE_PUBLIC);
                case PRIVATE:
                    return ImageUtilities.loadImage(PACKAGE_PRIVATE);
                case UNKNOWN:
                    return ImageUtilities.loadImage(PACKAGE);
                default:
                    throw new IllegalStateException(String.valueOf(a));
            }
        }
    }
    
    
    /**
     * Check whether a package is empty (devoid of files except for subpackages).
     */
    public static boolean isEmpty( FileObject fo ) {    
        return isEmpty (fo, true );
    }

    /**
     * Check whether a package is empty (devoid of files except for subpackages).
     * @param recurse specifies whether to check if subpackages are empty too.
     */
    public static boolean isEmpty( FileObject fo, boolean recurse ) {            
        FileObject[] kids = fo.getChildren();
        for( int i = 0; i < kids.length; i++ ) {
            // XXX consider using group.contains() here
            if ( !kids[i].isFolder() && VisibilityQuery.getDefault().isVisible( kids[i] ) ) {
                return false;
            }  
            else if (recurse && VisibilityQuery.getDefault().isVisible( kids[i] ) && !isEmpty(kids[i])) {
                    return false;
            }
        }
        return true;
    }
    
    /**
     * Check whether a package should be displayed.
     * It should be displayed if {@link VisibilityQuery} says it should be,
     * and it is either completely empty, or contains files (as opposed to
     * containing some subpackages but no files).
     */
    public static boolean isSignificant(FileObject pkg) throws IllegalArgumentException {
        if (!pkg.isFolder()) {
            throw new IllegalArgumentException("Not a folder"); // NOI18N
        }
        // XXX consider using group.contains() here
        if (!VisibilityQuery.getDefault().isVisible(pkg)) {
            return false;
        }
        FileObject[] kids = pkg.getChildren();
        boolean subpackages = false;
        for (int i = 0; i < kids.length; i++) {
            if (!VisibilityQuery.getDefault().isVisible(kids[i])) {
                continue;
            }
            if (kids[i].isData()) {
                return true;
            } else {
                subpackages = true;
            }
        }
        return !subpackages;
    }
    
}
