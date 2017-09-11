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

package org.openide.loaders;

import java.io.IOException;

import org.openide.filesystems.*;
import org.openide.util.io.SafeException;

/** Support class for loader handling one file at a time.
 * This is used for many file types, e.g. HTML, images, etc.
 * File extensions recognized by the loader may be set.
*
* @author Petr Hamernik, Jaroslav Tulach
*/
public abstract class UniFileLoader extends MultiFileLoader {
    /** SUID */
    private static final long serialVersionUID=-6190649471408985837L;

    /** name of property with extensions */
    public static final String PROP_EXTENSIONS = "extensions"; // NOI18N
    
    /** Constructor.
    * @param representationClass class that is produced by this loader
     * @deprecated Use UniFileLoader#UniFileLoader(String) instead.
    */
    @Deprecated
    protected UniFileLoader(Class<? extends DataObject> representationClass) {
        super (representationClass);
    }

    /** Constructor.
    * @param representationClassName the fully qualified name of the
    * representation class.
    *
    * @since 1.10
    */
    protected UniFileLoader (String representationClassName) {
        super (representationClassName);
    }

    /** Get the primary file.
    * By default, only accepts files, not folders.
    * @param fo the file to find the primary file for
    *
    * @return the primary file, or <code>null</code> if its extension is not {@link #getExtensions recognized}
    */
    protected FileObject findPrimaryFile (FileObject fo) {
        // never recognize folders
        if (fo.isFolder()) return null;
        
        return getExtensions().isRegistered(fo) ? fo : null;
    }

    /* Creates the right data object for given primary file.
    * It is guaranteed that the provided file is realy primary file
    * returned from the method findPrimaryFile.
    *
    * @param primaryFile the primary file
    * @return the data object for this file
    * @exception DataObjectExistsException if the primary file already has data object
    */
    protected abstract MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException;

    /* Creates the right primary entry for given primary file.
    *
    * @param obj requesting object
    * @param primaryFile primary file recognized by this loader
    * @return primary entry for that file
    */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        return new FileEntry (obj, primaryFile);
    }

    /** Do not create a seconday entry.
    *
    * @param obj ignored
    * @param secondaryFile ignored
    * @return never returns
    * @exception UnsupportedOperationException because this loader supports only a primary file object
    */
    protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {

        // Debug messages for #17014. Please remove, when the bug is fixed.
        StringBuilder buf = new StringBuilder("Error in data system. Please reopen the bug #17014 with the following message: "); //NOI18N
        buf.append("\n  DataLoader:"); //NOI18N
        buf.append(getClass().getName());
        buf.append("\n  DataObject:"); //NOI18N
        buf.append(obj);
        buf.append("\n  PrimaryEntry:"); //NOI18N
        buf.append(obj.getPrimaryEntry());
        buf.append("\n  PrimaryFile:"); //NOI18N
        buf.append(obj.getPrimaryFile());
        buf.append("\n  SecondaryFile:"); //NOI18N
        buf.append(secondaryFile);
        buf.append("\n");
        
        throw new UnsupportedOperationException (buf.toString()); //NOI18N
    }

    /** Called when there is a collision between a data object that 
    * this loader tries to create and already existing one.
    * 
    * @param obj existing data object
    * @param file the original file that has been recognized by this loader
    *    as bellonging to obj data object
    * @return null 
    */
    @Override
    final DataObject checkCollision (DataObject obj, FileObject file) {
        return null;
    }
    
    /** Does nothing because this loader works only with objects
    * with one file => primary file so it is not necessary to search
    * for anything else.
    * 
    * @param obj the object to test
    */
    @Override
    final void checkConsistency (MultiDataObject obj) {
    }
    
    /** Does nothing because this loader works only with objects
    * with one file => primary file so it is not necessary to search
    * for anything else.
    * 
    * @param obj the object to test
    */
    @Override
    final void checkFiles (MultiDataObject obj) {
    }

    /** Set the extension list for this data loader.
    * @param ext new list of extensions
    */
    public void setExtensions(ExtensionList ext) {
        putProperty (PROP_EXTENSIONS, ext, true);
    }

    /** Get the extension list for this data loader.
    * @return list of extensions
    */
    public ExtensionList getExtensions() {
        ExtensionList l = (ExtensionList)getProperty (PROP_EXTENSIONS);
        if (l == null) {
            l = new ExtensionList ();
            putProperty (PROP_EXTENSIONS, l, false);
        }
        return l;
    }

    /** Writes extensions to the stream.
    * @param oo ignored
    */
    @Override
    public void writeExternal (java.io.ObjectOutput oo) throws IOException {
        super.writeExternal (oo);

        oo.writeObject (getProperty (PROP_EXTENSIONS));
    }

    /** Reads nothing from the stream.
    * @param oi ignored
    */
    @Override
    public void readExternal (java.io.ObjectInput oi)
    throws IOException, ClassNotFoundException {
        SafeException se;
        try {
            super.readExternal (oi);
            se = null;
        } catch (SafeException se2) {
            se = se2;
        }

        setExtensions ((ExtensionList)oi.readObject ());
        if (se != null) throw se;
    }

}
