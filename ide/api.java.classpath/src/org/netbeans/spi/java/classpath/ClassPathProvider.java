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

package org.netbeans.spi.java.classpath;

import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

/**
 * Provider interface for Java classpaths.
 * <p>
 * The <code>org.netbeans.modules.java.project</code> module registers an
 * implementation of this interface to global lookup which looks for the
 * project which owns a file (if any) and checks its lookup for this interface,
 * and if it finds an instance, delegates to it. Therefore it is not normally
 * necessary for a project type provider to register its own instance just to
 * define the classpaths for files it owns, assuming it depends on the Java
 * Project module.
 * </p>
 * <div class="nonnormative">
 * <p>
 * Note that to make editor code completion functionality work for a Java source file the
 * following classpaths must be available for it:
 * </p>
 * <ol>
 * <li>The {@link ClassPath#BOOT} type of classpath
 *     is required or the source file will not be parsable and 
 *     code completion will be disabled. See also
 *     {@link org.netbeans.spi.java.queries.SourceLevelQueryImplementation}.</li>
 * <li>The {@link ClassPath#SOURCE} type of classpath
 *     is required or code completion will be disabled.
 *     Providing this classpath will enable code completion, but only elements
 *     defined on this classpath will be offered if the compile classpath is missing.</li>
 * <li>The {@link ClassPath#COMPILE} type of classpath
 *     is recommended to be provide to make code completion work fully
 *     by suggesting all classes against which the source is developed.</li>
 * </ol>
 * <p>{@link ClassPath#EXECUTE} is also recommended for e.g. I18N functionality to work.
 * This should contain the full run-time classpath of the class, including its build
 * location (bytecode).</p>
 * <p>You should return these classpaths for the package root folder and any
 * files or folders inside it.</p>
 * <p>You should register classpaths for source files of all these types in
 * {@link org.netbeans.api.java.classpath.GlobalPathRegistry}
 * when they are to be exposed in the GUI as available for use (e.g. for the editor's Fast Open dialog),
 * and unregister them when they are no longer to be exposed. Typically this is done as part of
 * <a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/ProjectOpenedHook.html">ProjectOpenedHook</a>.
 * <p>It is also desirable to produce classpath information for compiled class files
 * (bytecode), including their package roots (whether a disk folder or a JAR root).
 * This will enable parsing of the class files, which is sometimes needed (e.g. for
 * expanding the class file node and seeing its members).
 * Compiled classes should have:</p>
 * <ol>
 * <li>{@link ClassPath#BOOT} corresponding to the Java platform to be used with the classes.</li>
 * <li>{@link ClassPath#EXECUTE} containing the bytecode's package root itself, plus any other
 * libraries it needs to resolve against. Should normally be the same as the execute classpath
 * of the corresponding source files.</li>
 * </ol>
 * <p>If no specific class path providers are available for a given source file or bytecode file,
 * i.e. <code>null</code> is returned from all providers, there may be a fallback implementation
 * which would provide reasonable defaults. For source files, this could mean a boot classpath
 * corresponding to the default Java platform (i.e. the JDK being used to run the IDE); empty
 * compile and execute classpaths; and a sourcepath computed based on the package statement in the
 * source file (if this is possible). For class files, this could mean a boot classpath determined
 * as for source files, and an execute classpath containing the package root apparently owning the
 * class file (computed according to the class file's package information, if this is possible).</p>
 * </div>
 * @see ClassPath#getClassPath
 * @see org.netbeans.api.java.classpath.GlobalPathRegistry
 * @author Jesse Glick
 * @since org.netbeans.api.java/1 1.4
 */
public interface ClassPathProvider {
    
    /**
     * Find some kind of a classpath for a given file.
     * @param file a file somewhere, or a source root
     * @param type a classpath type such as {@link ClassPath#COMPILE}
     * @return an appropriate classpath, or null for no answer
     * @see ClassPathFactory
     * @see org.netbeans.spi.java.classpath.support.ClassPathSupport
     */
    ClassPath findClassPath(FileObject file, String type);
    
}
