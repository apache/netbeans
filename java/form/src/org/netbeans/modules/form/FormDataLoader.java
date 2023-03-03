/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    
    /** Gets default display name. Overrides superclass method. */
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
