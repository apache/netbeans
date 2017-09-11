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
package org.openide.filesystems;

import java.awt.Image;
import java.util.Set;

/**
 * Allows a filesystem to annotate a group of files (typically comprising a data
 * object) with additional markers.
 * <p>
 * This could be useful, for example, for a filesystem supporting version
 * control. It could annotate names and icons of data nodes according to whether
 * the files were current, locked, etc.
 * <p/>
 * Formerly part of openide.filesystems module, replaced by {@link StatusDecorator}
 */
public interface FileSystem$Status {
    /**
     * Annotate the name of a file cluster.
     *
     * @param name the name suggested by default
     * @param files an immutable set of {@link FileObject}s belonging to this
     * filesystem
     * @return the annotated name (may be the same as the passed-in name)
     * @exception ClassCastException if the files in the set are not of valid
     * types
     */
    public String annotateName(String name, Set<? extends FileObject> files);

    /**
     * Annotate the icon of a file cluster.
     * <p>
     * Please do <em>not</em> modify the original; create a derivative icon
     * image, using a weak-reference cache if necessary.
     *
     * @param icon the icon suggested by default
     * @param iconType an icon type from {@link java.beans.BeanInfo}
     * @param files an immutable set of {@link FileObject}s belonging to this
     * filesystem
     * @return the annotated icon (may be the same as the passed-in icon)
     * @exception ClassCastException if the files in the set are not of valid
     * types
     */
    public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files);
}
