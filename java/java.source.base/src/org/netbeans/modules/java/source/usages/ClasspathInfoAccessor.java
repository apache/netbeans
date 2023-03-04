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

package org.netbeans.modules.java.source.usages;

import java.util.function.Function;
import javax.tools.JavaFileManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.parsing.InferableJavaFileObject;
import org.openide.filesystems.FileObject;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.source.parsing.FileManagerTransaction;

/**
 *
 * @author Tomas Zezula
 */
public abstract class ClasspathInfoAccessor {
    private static Logger log = Logger.getLogger(ClasspathInfoAccessor.class.getName());
    public static synchronized ClasspathInfoAccessor getINSTANCE() {
        if (INSTANCE == null) {
            try {
                Class.forName(ClasspathInfo.class.getName(), true, ClasspathInfo.class.getClassLoader());
            } catch (ClassNotFoundException cnfe) {
                if (log.isLoggable(Level.SEVERE))
                    log.log(Level.SEVERE, cnfe.getMessage(), cnfe);
            }
        }
        
        return INSTANCE;
    }

    public static void setINSTANCE(ClasspathInfoAccessor aINSTANCE) {
        INSTANCE = aINSTANCE;
    }

    private static volatile ClasspathInfoAccessor INSTANCE;

    @NonNull
    public abstract JavaFileManager createFileManager(@NonNull ClasspathInfo cpInfo, @NullAllowed String sourceLevel);
    
    @NonNull
    public abstract FileManagerTransaction getFileManagerTransaction(@NonNull ClasspathInfo cpInfo);
    
    public abstract ClassPath getCachedClassPath (ClasspathInfo cpInfo, ClasspathInfo.PathKind kind);
        
    public abstract ClasspathInfo create (
            @NonNull final ClassPath bootPath,
            @NonNull final ClassPath moduleBootPath,
            @NonNull final ClassPath compilePath,
            @NonNull final ClassPath moduleCompilePath,
            @NonNull ClassPath moduleClassPath,
            @NullAllowed ClassPath sourcePath,
            @NullAllowed ClassPath moduleSourcePath,
            @NullAllowed JavaFileFilterImplementation filter,
            boolean backgroundCompilation,
            boolean ignoreExcludes,
            boolean hasMemoryFileManager,
            boolean useModifiedFiles,
            boolean requiresSourceRoots,
            @NullAllowed Function<JavaFileManager.Location, JavaFileManager> jfmProvider);

    public abstract ClasspathInfo create (FileObject fo,
            JavaFileFilterImplementation filter,
            boolean backgroundCompilation,
            boolean ignoreExcludes,
            boolean hasMemoryFileManager,
            boolean useModifiedFiles);
    
    /**
     * Registers virtual java source into the memory {@link JavacFileManager}
     * @param cpInfo {@link ClasspathInfo}
     * @param fqn under which the source will be bind
     * @param content of the source
     * @return true when the binding replaced already bound virtual source
     * @throws java.lang.UnsupportedOperationException when the {@link ClasspathInfo} doesn't support memory {@link JavacFileManager}
     */
    public abstract boolean registerVirtualSource (ClasspathInfo cpInfo, InferableJavaFileObject jfo) throws UnsupportedOperationException;
    
    /**
     * Unregisters virtual java source from memory {@link JavacFileManager}
     * @param cpInfo {@link ClasspathInfo}
     * @param fqn which should be unbind
     * @return true when the binding was removed
     * @throws java.lang.UnsupportedOperationException when the {@link ClasspathInfo} doesn't support memory {@link JavacFileManager}
     */
    public abstract boolean unregisterVirtualSource (ClasspathInfo cpnfo, String fqn) throws UnsupportedOperationException;
    
    
}
