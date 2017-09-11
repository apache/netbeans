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

/*
 * MultiFileLoaderHid.java
 *
 * Created on September 12, 2001, 7:35 PM
 */

package org.openide.loaders;

import org.openide.filesystems.*;
import java.io.*;

/**
 *
 * @author  Vitezslav Stejskal
 */
public class MultiFileLoaderHid extends MultiFileLoader {

    private static final String PRIMARY_EXT = "primary";
    private static final String SECONDARY_EXT = "secondary";

    /** Creates new MultiFileLoaderHid */
    public MultiFileLoaderHid () {
        super ("org.openide.loaders.DataObject");
    }

    /** For a given file finds the primary file.
     * @param fo the (secondary) file
     *
     * @return the primary file for the file or <code>null</code> if the file is not
     *  recognized by this loader
     */
    protected FileObject findPrimaryFile (FileObject fo) {
        if (PRIMARY_EXT.equals (fo.getExt ())) {
            return fo;
        }
        if (SECONDARY_EXT.equals (fo.getExt ())) {
            return FileUtil.findBrother (fo, PRIMARY_EXT);
        }
        return null;
    }

    /** Creates a new secondary entry for a given file.
     * Note that separate entries must be created for every secondary
     * file within a given multi-file data object.
     *
     * @param obj requesting object
     * @param secondaryFile a secondary file
     * @return the entry
     */
    protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
        return new FileEntry (obj, secondaryFile);
    }

    /** Creates the right primary entry for a given primary file.
     *
     * @param obj requesting object
     * @param primaryFile primary file recognized by this loader
     * @return primary entry for that file
     */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        return new FileEntry (obj, primaryFile);
    }

    /** Creates the right data object for a given primary file.
     * It is guaranteed that the provided file will actually be the primary file
     * returned by {@link #findPrimaryFile}.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has a data object
     */
    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new MDO (primaryFile, this);
    }
    
    private static class MDO extends MultiDataObject {
        public MDO (FileObject primaryFile, MultiFileLoader loader) throws DataObjectExistsException {
            super (primaryFile, loader);
        }
    }
}
