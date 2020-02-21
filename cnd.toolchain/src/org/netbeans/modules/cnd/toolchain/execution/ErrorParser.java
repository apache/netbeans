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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.toolchain.execution;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Utilities;

public abstract class ErrorParser implements ErrorParserProvider.ErrorParser {

    protected final ExecutionEnvironment execEnv;
    protected final ExecutionEnvironment soucreEnv;
    private final PathMap pathMap;
    private NativeProject nativeProject;
    private NativeFileSearch nativeFileSearch;
    private final MakeContext makeContext;

    public ErrorParser(Project project, ExecutionEnvironment execEnv, FileObject relativeTo) {
        super();
        this.makeContext = new MakeContext(relativeTo);
        this.execEnv = execEnv;
        pathMap = RemoteSyncSupport.getPathMap(execEnv, project);
        soucreEnv = RemoteFileUtil.getProjectSourceExecutionEnvironment(project);
        init(project);
    }

    private void init(Project project) {
        if (project != null) {
            nativeProject = project.getLookup().lookup(NativeProject.class);
            if (nativeProject != null) {
                nativeFileSearch = NativeProjectSupport.getNativeFileSearch(nativeProject);
            }
        }
    }

    /**
     * 
     * @param fileName absolute path
     * @param isDirectory directory resolved in source file system, file can be resolved in remote file system
     * @return 
     */
    protected FileObject resolveFile(String fileName, boolean isDirectory) {
        if (Utilities.isWindows()) {
            //replace /cygdrive/<something> prefix with <something>:/ prefix:
            if (fileName.startsWith("/cygdrive/")) { // NOI18N
                fileName = fileName.substring("/cygdrive/".length()); // NOI18N
                fileName = "" + fileName.charAt(0) + ':' + fileName.substring(1);
            } else if (fileName.length() > 3 && fileName.charAt(0) == '/' && fileName.charAt(2) == '/') { // NOI18N
                // NOI18N
                fileName = "" + fileName.charAt(1) + ':' + fileName.substring(2); // NOI18N
            }
            if (fileName.startsWith("/") || fileName.startsWith("\\")) { // NOI18N
                FileObject relativeTo = makeContext.getLastContext();
                if (relativeTo != null) {
                    String path = relativeTo.getPath();
                    if (path.length()>2 && path.charAt(1)==':') {
                        fileName = path.substring(0,2) + fileName;
                    }
                }
            }
            if (fileName.startsWith("/") || fileName.startsWith(".")) { // NOI18N
                // NOI18N
                return null;
            }
            fileName = fileName.replace('/', '\\'); // NOI18N
        }
        String localFileName = pathMap.getTrueLocalPath(fileName);
        if (localFileName != null) {
            return FileSystemProvider.getFileObject(soucreEnv, FileSystemProvider.normalizeAbsolutePath(localFileName, soucreEnv));
        }
        if (isDirectory) {
            FileObject res = FileSystemProvider.getFileObject(soucreEnv, FileSystemProvider.normalizeAbsolutePath(fileName, soucreEnv));
            if (res == null && !execEnv.equals(soucreEnv)) {
                 res = FileSystemProvider.getFileObject(execEnv, FileSystemProvider.normalizeAbsolutePath(fileName, execEnv));
            }
            return res;
        } else {
            return FileSystemProvider.getFileObject(execEnv, FileSystemProvider.normalizeAbsolutePath(fileName, execEnv));
        }
    }

    protected FileObject resolveRelativePath(FileObject relativeDir, String relativePath) {
        FileObject resolved = _resolveRelativePath(relativeDir, relativePath);
        if (resolved != null && resolved.isValid()) {
            return resolved;
        }
        if (nativeFileSearch != null) {
            relativePath = relativePath.replace('\\', '/'); // NOI18N
            int i = relativePath.lastIndexOf('/'); // NOI18N
            String nameExt;
            if (i >= 0) {
                nameExt = relativePath.substring(i+1);
            } else {
                nameExt = relativePath;
            }
            Collection<FSPath> searchFile = nativeFileSearch.searchFile(nativeProject, nameExt);
            if (searchFile.size() == 1) {
                return _resolveRelativePath(relativeDir, searchFile.iterator().next().getPath());
            }
        }
        return null;
    }

    private FileObject _resolveRelativePath(FileObject relativeDir, String relativePath) {
        if (CndPathUtilities.isAbsolute(relativePath)) {
            // NOI18N
            if (execEnv.isRemote() || Utilities.isWindows()) {
                // See IZ 106841 for details.
                // On Windows the file path for system header files comes in as /usr/lib/abc/def.h
                // but the real path is something like D:/cygwin/lib/abc/def.h (for Cygwin installed
                // on D: drive). We need the exact compiler that produced this output to safely
                // convert the path but the compiler has been lost at this point. To work-around this problem
                // iterate over all defined compiler sets and test whether the file existst in a set.
                // If it does, convert it to a FileObject and return it.
                // FIXUP: pass exact compiler used to this method (would require API changes we
                // don't want to do now). Error/warning regular expressions should also be moved into
                // the compiler(set) and the output should only be scanned for those patterns.
                String absPath1 = relativePath;
                String absPath2 = null;
                if (absPath1.startsWith("/usr/lib")) { // NOI18N
                    absPath2 = absPath1.substring(4);
                }
                if (relativePath.startsWith("/") || relativePath.startsWith("\\")) { // NOI18N
                    String path = relativeDir.getPath();
                    if (path.length()>2 && path.charAt(1)==':') {
                        relativePath = path.substring(0,2) + relativePath;
                    }
                }
                List<CompilerSet> compilerSets = CompilerSetManager.get(execEnv).getCompilerSets();
                for (CompilerSet set : compilerSets) {
                    Tool cCompiler = set.getTool(PredefinedToolKind.CCompiler);
                    if (cCompiler != null) {
                        String includePrefix = cCompiler.getIncludeFilePathPrefix();
                        File file = new File(includePrefix + absPath1);
                        if (!CndFileUtils.exists(file) && absPath2 != null) {
                            file = new File(includePrefix + absPath2);
                        }
                        if (CndFileUtils.exists(file)) {
                            FileObject fo = CndFileUtils.toFileObject(CndFileUtils.normalizeFile(file));
                            return fo;
                        }
                    }
                }
            }
            FileObject myObj = resolveFile(relativePath, false);
            if (myObj != null) {
                return myObj;
            }
            if (relativePath.startsWith(File.separator)) {
                // NOI18N
                relativePath = relativePath.substring(1);
            }
            try {
                FileSystem fs = relativeDir.getFileSystem();
                myObj = fs.findResource(relativePath);
                if (myObj != null) {
                    return myObj;
                }
                myObj = fs.getRoot();
                if (myObj != null) {
                    relativeDir = myObj;
                }
            } catch (FileStateInvalidException ex) {
            }
        }
        FileObject myObj = relativeDir;
        String delims = Utilities.isWindows() ? File.separator + '/' : File.separator; // NOI18N

        // NOI18N
        StringTokenizer st = new StringTokenizer(relativePath, delims);
        while ((myObj != null) && st.hasMoreTokens()) {
            String nameExt = st.nextToken();
            if ("..".equals(nameExt)) { // NOI18N
                myObj = myObj.getParent();
            } else if (".".equals(nameExt)) { // NOI18N
            } else {
                myObj = myObj.getFileObject(nameExt, null);
            }
        }
        return myObj;
    }
    
    protected MakeContext getMakeContext() {
        return makeContext;
    }
    
    private static class MakeContextItem {
        final int level;
        final FileObject path;
        
        private MakeContextItem(int level, FileObject path) {
            this.level = level;
            this.path = path;
        }

        @Override
        public String toString() {
            return "["+level+"] "+path.getPath(); //NOI18N
        }
    }

    protected final static class MakeContext {
        private LinkedList<MakeContextItem> stack = new LinkedList<>();
        
        private MakeContext(FileObject baseDir) {
            stack.push(new MakeContextItem(0, baseDir));
        }
        
        /**
         * Remove from stack paths up to level-1
         * 
         * @param level 
         */
        protected void pop(int level) {
            while(true) {
                if (stack.size() == 1) {
                    return;
                }
                MakeContextItem peek = stack.peek();
                if (peek.level == -1) {
                    stack.pop();
                } else if (peek.level >= level) {
                    stack.pop();
                } else {
                    return;
                }
            }
        }

        /**
         * Add path with level.
         * if level equals -1 it replace top entry if top entry has level -1.
         * 
         * @param level
         * @param path 
         */
        protected void push(int level, FileObject path) {
            MakeContextItem peek = stack.peek();
            if (peek.level == -1) {
                stack.pop();
            }
            stack.push(new MakeContextItem(level, path));
        }
        
        protected FileObject getLastContext() {
            ListIterator<MakeContextItem> it = stack.listIterator(0);
            while(it.hasNext()) {
                MakeContextItem item = it.next();
                if (item.level != -1) {
                    return item.path;
                }
            }
            return null;
        }
        
        protected FileObject getTopContext() {
            return stack.peek().path;
        }
    }
    
}
