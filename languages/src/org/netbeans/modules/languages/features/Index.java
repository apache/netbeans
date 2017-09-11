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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.languages.features;

import org.netbeans.api.languages.database.DatabaseDefinition;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.Language;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.database.DatabaseContext;
import org.netbeans.modules.languages.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;


/**
 *
 * @author Jan Jancura
 */
public class Index {
    
    private static Map<FileObject,ProjectCache> projectToCache = new WeakHashMap<FileObject,ProjectCache> ();

    
    public static Map<FileObject,List<DatabaseDefinition>> getGlobalItems (
        FileObject fo, 
        boolean parse
    ) throws FileNotParsedException {
        Map<FileObject,List<DatabaseDefinition>> result = new HashMap<FileObject,List<DatabaseDefinition>> ();
        FileObject projectDir = Utils.getProjectRoot(fo);
        if (projectDir == null) return result;
        getProjectCache (projectDir, parse).add (result, null);
        return result;
    }
    
    public static Map<FileObject,List<DatabaseDefinition>> getGlobalItem (
        FileObject  fo,
        String      name,
        boolean     parse
    ) throws FileNotParsedException {
        Map<FileObject,List<DatabaseDefinition>> result = new HashMap<FileObject,List<DatabaseDefinition>> ();
        FileObject projectDir = Utils.getProjectRoot(fo);
        if (projectDir == null) return result;
        getProjectCache (projectDir, parse).add (result, name);
        return result;
    }
    
    private static ProjectCache getProjectCache (
        FileObject projectDir,
        boolean parse
    ) throws FileNotParsedException {
        ProjectCache cache = projectToCache.get (projectDir);
        if (cache == null)
            cache = readProjectCache (projectDir);
        if (cache == null) {
            if (!parse) throw new FileNotParsedException ();
            cache = new ProjectCache (projectDir);
            projectToCache.put (projectDir, cache);
        }
        return cache;
    }
    
    private static ProjectCache readProjectCache (FileObject projectDir) {
        try {
            File f = getProjectCacheFile (projectDir);
            if (f == null) return null;
            return ProjectCache.load (projectDir, f);
        } catch (IOException e) {
            e.printStackTrace ();
            return null;
        }
    }
    
    private static List<FileObject> roots;
    
    private static File getProjectCacheFile (FileObject projectDir) {
        File cacheFolder = getCacheFolder ();
        if (roots != null) {
            int i = roots.indexOf (projectDir);
            if (i < 0) return null;
            return new File (cacheFolder, "s" + (i + 1));
        }
        roots = new ArrayList<FileObject> ();
        File segments = new File (cacheFolder, "segments");
        if (!segments.exists ()) return null;
        try {
            BufferedReader reader = new BufferedReader (new FileReader (segments));
            try {
                File result = null;
                int i = 1;
                String path = reader.readLine ();
                while (path != null) {
                    File file = new File (path);
                    file = FileUtil.normalizeFile (file);
                    FileObject fo = FileUtil.toFileObject (file);
                    if (fo == null) {
                        System.out.println (Index.class.getName () + " File not found: " + file);
                    } else {
                        roots.add (fo);
                        if (fo.equals (projectDir))
                            result = new File (cacheFolder, "s" + i);
                    }
                    path = reader.readLine ();
                    i++;
                }
                return result;
            } finally {
                reader.close ();
            }
        } catch (IOException ex) {
            ex.printStackTrace ();
            return null;
        }
    }
    
    public static void save () throws IOException {
        File dir = getCacheFolder ();
        File segments = new File (dir, "segments");
        BufferedWriter writer = new BufferedWriter (new FileWriter (segments));
        try {
            int i = 1;
            Iterator<FileObject> it = projectToCache.keySet ().iterator ();
            while (it.hasNext ()) {
                FileObject fo = it.next ();
                ProjectCache cache = projectToCache.get (fo);
                File s = new File (dir, "s" + i);
                cache.save (s);
                writer.write (fo.getPath ());
                writer.newLine ();
            }
        } finally {
            writer.close ();
        }
    }

    private static File cacheFolder;
    
    private static synchronized File getCacheFolder () {
        if (cacheFolder == null) {
            cacheFolder = Places.getCacheSubdirectory("sindex/1.0");
        }
        return cacheFolder;
    }

    
    // innerclasses ............................................................
    
    private static class ProjectCache {
        
        private FileObject                  root;
        private Map<FileObject,FileCache>   cache;
        
        ProjectCache (FileObject root) {
            this.root = root;
        }
        
        ProjectCache (FileObject root, Map<FileObject,FileCache> cache) {
            this.root = root;
            this.cache = cache;
        }

        private synchronized void add (
            Map<FileObject,List<DatabaseDefinition>>    result,
            String                                      name
        ) {
            if (cache == null) {
                cache = new HashMap<FileObject,FileCache> ();
                init (root);
            }
            Iterator<FileCache> it = cache.values ().iterator ();
            while (it.hasNext ()) {
                FileCache fileCache =  it.next();
                fileCache.add (result, name);
            }
        }

        private synchronized void init (FileObject root) {
            FileObject[] ch = root.getChildren ();
            int i, k = ch.length;
            for (i = 0; i < k; i++) {
                FileObject fo = ch[i];
                if (fo.isFolder ()) {
                    init (fo);
                    continue;
                }
                if (!"js".equals (fo.getExt ()))
                    continue;
                FileCache fc = new FileCache (fo);
                cache.put (fo, fc);
            }
        }
        
       private static ProjectCache load (FileObject root, File f) throws IOException {
            DataInputStream is = new DataInputStream (new FileInputStream (f));
            try {
                Map<FileObject,FileCache> cache = new HashMap<FileObject, Index.FileCache> ();
                int i = is.readInt ();
                while (i > 0) {
                    String path = is.readUTF ();
                    File file = new File (path);
                    file = FileUtil.normalizeFile (file);
                    FileObject fo = FileUtil.toFileObject (file);
                    FileCache fc = FileCache.load (fo, is);
                    cache.put (fo, fc);
                    i--;
                }
                return new ProjectCache (root, cache);
            } finally {
                is.close ();
            }
        }
        
        private synchronized void save (File f) throws IOException {
            DataOutputStream os = new DataOutputStream (new FileOutputStream (f));
            try {
                os.writeInt (cache.size ());
                Iterator<FileObject> it = cache.keySet ().iterator ();
                while (it.hasNext ()) {
                    FileObject fileObject =  it.next();
                    os.writeUTF (fileObject.getPath ());
                    FileCache fc = cache.get (fileObject);
                    fc.save (os);
                }
            } finally {
                os.close ();
            }
        }
    }
    
    private static class FileCache {
    
        private FileObject                  fileObject;
        private List<DatabaseDefinition>    definitions;
        
        FileCache (FileObject fileObject) {
            this.fileObject = fileObject;
        }
        
        private FileCache (FileObject fileObject, List<DatabaseDefinition> definitions) {
            this.fileObject = fileObject;
            this.definitions = definitions;
        }
        
        private DatabaseContext getRoot (
            FileObject fo
        ) throws LanguageDefinitionNotFoundException, IOException, ParseException {
            Language l = LanguagesManager.get().getLanguage (fo.getMIMEType ());
            ASTNode root = l.parse (fo.getInputStream ());
            return DatabaseManager.parse (root, null, null);
        }

        private synchronized void add (
            Map<FileObject,List<DatabaseDefinition>>    result,
            String                                      name
        ) {
            if (definitions == null) {
                definitions = new ArrayList<DatabaseDefinition> ();
                try {
                    //long time = System.currentTimeMillis();
                    DatabaseContext r = getRoot (fileObject);
                    //S ystem.out.println ("parse " + fileObject.getNameExt () + " : " + (System.currentTimeMillis () - time));
                    definitions = r.getDefinitions ();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (name == null) {
                if (!definitions.isEmpty ()) {
                    List<DatabaseDefinition> l = result.get (fileObject);
                    if (l == null) {
                        l = new ArrayList<DatabaseDefinition> ();
                        result.put (fileObject, l);
                    }
                    l.addAll (definitions);
                }
            } else {
                List<DatabaseDefinition> l = result.get (fileObject);
                Iterator<DatabaseDefinition> it = definitions.iterator ();
                while (it.hasNext()) {
                    DatabaseDefinition definition =  it.next ();
                    if (definition.getName ().equals (name)) {
                        if (l == null) {
                            l = new ArrayList<DatabaseDefinition> ();
                            result.put (fileObject, l);
                        }
                        l.add (definition);
                    }
                }
            }
        }
        
        static FileCache load (FileObject fo, DataInputStream is) throws IOException {
            List<DatabaseDefinition> definitions = new ArrayList<DatabaseDefinition> ();
            int i = is.readInt ();
            while (i > 0) {
                definitions.add (DatabaseDefinition.load (is));
                i--;
            }
            return new FileCache (fo, definitions);
        }
        
        private synchronized void save (DataOutputStream os) throws IOException {
            os.writeInt (definitions.size ());
            Iterator<DatabaseDefinition> it = definitions.iterator ();
            while (it.hasNext ())
                it.next ().save (os);
        }
    }
}
