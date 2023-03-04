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

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
//@NotThreadSafe    //Currently not thread save - caller should hold java.source lock
public class MemoryFileManager implements JavaFileManager {
    
    //Todo: Can be mem optimzed by converting to packed UTF.
    private final Map<String,List<Integer>> packages = new HashMap<String, List<Integer>>();
    private final Map<Integer, InferableJavaFileObject> content = new HashMap<Integer, InferableJavaFileObject>();
    private final AtomicInteger currentId = new AtomicInteger ();

    public MemoryFileManager () {        
    }

    public ClassLoader getClassLoader(Location location) {
        //Don't support class loading
        throw new UnsupportedOperationException ();
    }

    //Covariant return type - used by unit test
    public List<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
        if (recurse) {
            throw new UnsupportedEncodingException();
        }
        final List<JavaFileObject> result = new LinkedList<JavaFileObject> ();        
        if (location == StandardLocation.SOURCE_PATH) {
            final List<Integer> pkglst = packages.get(packageName);
            if (pkglst != null) {
                for (Integer foid : pkglst) {
                    InferableJavaFileObject jfo = content.get(foid);
                    assert jfo != null;
                    if (kinds.contains(jfo.getKind())) {
                        result.add(jfo);
                    }
                }
            }
        }
        return result;
    }

    public String inferBinaryName(Location location, JavaFileObject file) {
        if (location == StandardLocation.SOURCE_PATH) {
            if (file instanceof InferableJavaFileObject) {
                return ((InferableJavaFileObject)file).inferBinaryName();
            }
        }
        return null;
    }

    public boolean isSameFile(FileObject a, FileObject b) {
        return a == null ? b == null : (b == null ? false : a.toUri().equals(b.toUri()));
    }

    public boolean handleOption(String current, Iterator<String> remaining) {
        return false;
    }

    public boolean hasLocation(Location location) {
        return location == StandardLocation.SOURCE_PATH;
    }

    public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
        if (location == StandardLocation.SOURCE_PATH) {
            final String[] namePair = FileObjects.getPackageAndName (className);
            final List<Integer> pkglst = this.packages.get(namePair[0]);
            if (pkglst != null) {
                for (Integer id : pkglst) {
                    final InferableJavaFileObject jfo = this.content.get (id);
                    assert jfo != null;
                    if (className.equals(jfo.inferBinaryName())) {
                        return jfo;
                    }
                }
            }
        }
        return null;
    }

    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
        throw new UnsupportedOperationException();
    }

    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        if (location == StandardLocation.SOURCE_PATH) {                   
            final List<Integer> pkglst = packages.get(packageName);
            if (pkglst != null) {
                for (Integer id : pkglst) {
                    final InferableJavaFileObject jfo = this.content.get (id);
                    assert jfo != null;
                    if (relativeName.equals(jfo.getName())) {   //Todo: Rely on the file instanceof FileObjects.Base 
                        return jfo;
                    }
                }
            }
        }
        return null;
    }

    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        throw new UnsupportedOperationException("");
    }

    public void flush() throws IOException {        
    }

    public void close() throws IOException {
        this.packages.clear();
        this.content.clear();
    }

    public int isSupportedOption(String option) {
        return -1;
    }
    
    
    public boolean register (final InferableJavaFileObject jfo) {
        Parameters.notNull("jfo", jfo);
        final String inferedName = jfo.inferBinaryName();
        final String[] pkgName = FileObjects.getPackageAndName (inferedName);
        List<Integer> ids = this.packages.get(pkgName[0]);
        if (ids == null) {
            ids = new LinkedList<Integer>();
            this.packages.put(pkgName[0], ids);
        }
        //Check for duplicate
        for (Iterator<Integer> it = ids.iterator(); it.hasNext();) {
            final Integer id = it.next();
            final InferableJavaFileObject rfo = this.content.get(id);
            assert rfo != null;
            if (inferedName.equals(rfo.inferBinaryName())) {
                this.content.put(id, jfo);
                return true;
            }
        }        
        //Todo: add
        final Integer id = currentId.getAndIncrement();
        this.content.put(id, jfo);
        ids.add(id);
        return false;
    }
    
    public boolean unregister (final String fqn) {
        Parameters.notNull("fqn", fqn);
        final String[] pkgName = FileObjects.getPackageAndName (fqn);
        final List<Integer> ids = this.packages.get(pkgName[0]);
        for (Iterator<Integer> it = ids.iterator(); it.hasNext();) {
            final Integer id = it.next();
            final InferableJavaFileObject jfo = this.content.get(id);
            assert jfo != null;
            if (fqn.equals(jfo.inferBinaryName())) {
                it.remove();
                this.content.remove(id);
                return true;
            }
        }
        return false;
    }
    
}
