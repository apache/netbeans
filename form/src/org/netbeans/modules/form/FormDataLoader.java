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


package org.netbeans.modules.form;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;

/** Loader for Forms. Recognizes file with extension .form and .java and with extension class if
 * there is their source and form file.
 *
 * @author Ian Formanek
 */
@MIMEResolver.ExtensionRegistration(
    displayName="org/netbeans/modules/form/resources/Bundle#Services/MIMEResolver/FormResolver.xml",
    extension="form",
    mimeType="text/x-form",
    position=137
)
public class FormDataLoader extends MultiFileLoader {
    /** The standard extensions of the recognized files */
    public static final String FORM_EXTENSION = "form"; // NOI18N
    /** The standard extension for Java source files. */
    public static final String JAVA_EXTENSION = "java"; // NOI18N

    static final long serialVersionUID =7259146057404524013L;
    /** Constructs a new FormDataLoader */
    public FormDataLoader() {
        super("org.netbeans.modules.form.FormDataObject"); // NOI18N
    }

    
    /** Gets default display name. Overides superclass method. */
    @Override
    protected String defaultDisplayName() {
        return org.openide.util.NbBundle.getBundle(FormDataLoader.class)
                 .getString("PROP_FormLoader_Name"); // NOI18N
    }

    @Override
    protected String actionsContext () {
        return "Loaders/text/x-java/Actions/"; // NOI18N
    }

    /** For a given file finds a primary file.
     * @param fo the file to find primary file for
     *
     * @return the primary file for the file or null if the file is not
     *   recognized by this loader
     */
    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
	// never recognize folders.
        if (fo.isFolder()) return null;
        String ext = fo.getExt();
        if (ext.equals(FORM_EXTENSION))
            return FileUtil.findBrother(fo, JAVA_EXTENSION);

        FileObject javaFile = findJavaPrimaryFile(fo);
        return javaFile != null
                    && FileUtil.findBrother(javaFile, FORM_EXTENSION) != null ?
            javaFile : null;
    }

    /** Creates the right data object for given primary file.
     * It is guaranteed that the provided file is realy primary file
     * returned from the method findPrimaryFile.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has data object
     */
    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile)
        throws DataObjectExistsException
    {
        return new FormDataObject(FileUtil.findBrother(primaryFile, FORM_EXTENSION),
                                  primaryFile,
                                  this);
    }

    @Override
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        FormServices services = Lookup.getDefault().lookup(FormServices.class);
        MultiDataObject.Entry entry = services.createPrimaryEntry(obj, primaryFile);
        return entry;
    }

    private FileObject findJavaPrimaryFile(FileObject fo) {
        if (fo.getExt().equals(JAVA_EXTENSION))
            return fo;
        return null;
    }

    @Override
    protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj,
                                                         FileObject secondaryFile)
    {
        assert FORM_EXTENSION.equals(secondaryFile.getExt());
        
        FileEntry formEntry = new FormEntry(obj, secondaryFile);
        ((FormDataObject)obj).formEntry = formEntry;
        return formEntry;
    }

    private static class FormEntry extends FileEntry {
        public FormEntry(MultiDataObject mdo, FileObject fo) {
            super(mdo, fo);
        }

        @Override
        public FileObject createFromTemplate(FileObject folder, String name) throws IOException {
            return FileUtil.copyFile(getFile(), folder, name);
        }
    }
}
