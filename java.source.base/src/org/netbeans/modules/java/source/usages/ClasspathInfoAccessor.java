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
