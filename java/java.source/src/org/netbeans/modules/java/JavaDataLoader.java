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

package org.netbeans.modules.java;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Data loader which recognizes Java source files.
*
* @author Petr Hamernik
*/
public final class JavaDataLoader extends UniFileLoader {
    
    public static final String JAVA_MIME_TYPE = "text/x-java";  //NOI18N
    
    /** The standard extension for Java source files. */
    public static final String JAVA_EXTENSION = "java"; // NOI18N

    private static final String PACKAGE_INFO = "package-info";  //NOI18N
    
    static final long serialVersionUID =-6286836352608877232L;

    /** Create the loader.
    * Should <em>not</em> be used by subclasses.
    */
    public JavaDataLoader() {
        super("org.netbeans.modules.java.JavaDataObject"); // NOI18N
    }

    @Override
    protected void initialize() {
        super.initialize();
        ExtensionList extensions = new ExtensionList();
        extensions.addExtension(JAVA_EXTENSION);
        extensions.addMimeType(JAVA_MIME_TYPE);
        setExtensions(extensions);
    }

    @Override
    protected String actionsContext () {
        return "Loaders/text/x-java/Actions/"; // NOI18N
    }
    
    protected @Override String defaultDisplayName() {
        return NbBundle.getMessage(JavaDataLoader.class, "PROP_JavaLoader_Name");
    }
    
    /** Create the <code>JavaDataObject</code>.
    * Subclasses should rather create their own data object type.
    *
    * @param primaryFile the primary file
    * @return the data object for this file
    * @exception DataObjectExistsException if the primary file already has a data object
    */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException {
        if (getExtensions().isRegistered(primaryFile))
            return new JavaDataObject(primaryFile, this);
        return null;
    }

    /** For a given file find the primary file.
    * Subclasses should override this, but still look for the {@link #JAVA_EXTENSION},
    * as the Java source file should typically remain the primary file for the data object.
    * @param fo the file to find the primary file for
    *
    * @return the primary file for this file or <code>null</code> if this file is not
    *   recognized by this loader
    */
    protected FileObject findPrimaryFile (FileObject fo) {
	// never recognize folders.
        if (fo.isFolder()) return null;
        return super.findPrimaryFile(fo);
    }

    /** Create the primary file entry.
    * Subclasses may override {@link JavaDataLoader.JavaFileEntry} and return a new instance
    * of the overridden entry type.
    *
    * @param primaryFile primary file recognized by this loader
    * @return primary entry for that file
    */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        if (getExtensions().isRegistered(primaryFile)) {
//            return new JavaFileEntry (obj, primaryFile);
            return JavaDataSupport.createJavaFileEntry(obj, primaryFile);
        }
        else {
            return new FileEntry(obj, primaryFile);
        }
    }
       
    
    /** Create the map of replaceable strings which is used
    * in the <code>JavaFileEntry</code>. This method may be extended in subclasses
    * to provide the appropriate map for other loaders.
    * This implementation gets the map from the Java system option;
    * subclasses may add other key/value pairs which may be created without knowledge of the
    * file itself.
    *
    * @return the map of string which are replaced during instantiation
    *        from template
    */
    static Map<String, String> createStringsMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("USER", System.getProperty("user.name"));
        Date d = new Date();
        map.put("DATE", DateFormat.getDateInstance(DateFormat.LONG).format(d)); // NOI18N
        map.put("TIME", DateFormat.getTimeInstance(DateFormat.SHORT).format(d)); // NOI18N
        return map;
    }
    
    
    /** This entry defines the format for replacing text during
    * instantiation the data object.
    * Used to substitute keys in the source file.
    */
    public static class JavaFileEntry extends IndentFileEntry {
        static final long serialVersionUID =8244159045498569616L;

        /** Creates new entry. */
        public JavaFileEntry(MultiDataObject obj, FileObject file) {
            super(obj, file);
        }

        /** Provide suitable format for substitution of lines.
        * Should not typically be overridden.
        * @param target the target folder of the installation
        * @param n the name the file will have
        * @param e the extension the file will have
        * @return format to use for formating lines
        */
        protected java.text.Format createFormat (FileObject target, String n, String e) {
            Map<String, String> map = createStringsMap();

            modifyMap(map, target, n, e);

            JMapFormat format = new JMapFormat(map);
            format.setLeftBrace("__"); // NOI18N
            format.setRightBrace("__"); // NOI18N
            format.setCondDelimiter("$"); // NOI18N
            format.setExactMatch(false);
            return format;
        }

        /** Modify the replacement map.
        * May be extended in subclasses to provide additional key/value
        * pairs sensitive to the details of instantiation.
        * @param map the map to add to
        * @param target the destination folder for instantiation
        * @param n the new file name
        * @param e the new file extension
        */
        protected void modifyMap(Map<String, String> map, FileObject target, String n, String e) {
            ClassPath cp = ClassPath.getClassPath(target, ClassPath.SOURCE);
            String resourcePath = "";
            if (cp != null) {
                resourcePath = cp.getResourceName(target);
                if (resourcePath == null) {
                    Logger.getLogger(JavaDataLoader.class.getName()).log(Level.WARNING, "{0} is not on its own source path", FileUtil.getFileDisplayName(target));
                    resourcePath = "";
                }
            } else {
                Logger.getLogger(JavaDataLoader.class.getName()).warning("No classpath was found for folder: "+target);
            }
            map.put("NAME", n); // NOI18N
            // Yes, this is package sans filename (target is a folder).
            map.put("PACKAGE", resourcePath.replace('/', '.')); // NOI18N
            map.put("PACKAGE_SLASHES", resourcePath); // NOI18N
	    // Fully-qualified name:
	    if (target.isRoot ()) {
		map.put ("PACKAGE_AND_NAME", n); // NOI18N
		map.put ("PACKAGE_AND_NAME_SLASHES", n); // NOI18N
	    } else {
		map.put ("PACKAGE_AND_NAME", resourcePath.replace('/', '.') + '.' + n); // NOI18N
		map.put ("PACKAGE_AND_NAME_SLASHES", resourcePath + '/' + n); // NOI18N
	    }
            // No longer needed due to #6025. (You can just put in quotes, they will not
            // prevent things from being escaped.) But leave the token here for
            // compatibility with old templates. --jglick 26/08/00
            map.put("QUOTES","\""); // NOI18N
            
            for (CreateFromTemplateAttributesProvider provider
                    : Lookup.getDefault().lookupAll(CreateFromTemplateAttributesProvider.class)) {
                Map<String, ?> attrs = provider.attributesFor(
                        getDataObject(),
                        DataFolder.findFolder(target),
                        n);
                if (attrs == null) //#123006
                    continue;
                Object aName = attrs.get("user"); // NOI18N
                if (aName instanceof String) {
                    map.put("USER", (String) aName); // NOI18N
                    break;
                }
            }
        }


        // XXX below are methods formly placed in JavaDataObject. It is
        // a question if rename and copy should be here at all or they should be
        // part of refactoring stuff only.
        
        @Override
        public FileObject createFromTemplate(FileObject f, String name) throws IOException {
            if (getFile().getAttribute(/* ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR */"javax.script.ScriptEngine") == null) {
                Logger.getLogger(JavaDataLoader.class.getName()).log(Level.WARNING, "Please replace template {0} with the new scripting support. See http://bits.netbeans.org/7.1/javadoc/org-openide-loaders/apichanges.html#scripting", getFile().getPath());
            }
            if (name == null) {
                // special case: name is null (unspecified or from one-parameter createFromTemplate)
                name = FileUtil.findFreeFileName(f, f.getName(), "java"); // NOI18N
            } else if (!PACKAGE_INFO.equals(name) && !Utilities.isJavaIdentifier(name)) {
                throw new IOException(NbBundle.getMessage(JavaDataObject.class, "FMT_Not_Valid_FileName", name));
            }
            
            this.initializeIndentEngine();
            FileObject fo = super.createFromTemplate(f, name);
            
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            String pkgName;
            if (cp != null) {
                pkgName = cp.getResourceName(f, '.', false);
                if (pkgName == null) {
                    Logger.getLogger(JavaDataLoader.class.getName()).log(Level.WARNING, "{0} is not on its own source path", FileUtil.getFileDisplayName(fo));
                    pkgName = "";
                }
            } else {
                pkgName = "";   //NOI18N
            }
            JavaDataObject.renameFO(fo, pkgName, name, getFile().getName());
            
            // unfortunately JavaDataObject.renameFO creates JavaDataObject but it is too soon
            // in this stage. Loaders reusing this FileEntry will create further files.
            destroyDataObject(fo);
            
            return fo;
        }
        
        private void destroyDataObject(FileObject fo) throws IOException {
            DataObject dobj = DataObject.find(fo);
            try {
                dobj.setValid(false);
            } catch (PropertyVetoException ex) {
                throw (IOException) new IOException().initCause(ex);
            }
        }

    }
}
