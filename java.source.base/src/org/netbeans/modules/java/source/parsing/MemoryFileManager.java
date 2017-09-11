/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
