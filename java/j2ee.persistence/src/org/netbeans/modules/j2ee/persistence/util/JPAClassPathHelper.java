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

package org.netbeans.modules.j2ee.persistence.util;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.Parameters;

/**
 * A helper class for ensuring that the class path is correctly
 * set for generating classes that require the Java Persistence API, such 
 * as entity classes.
 */
public class JPAClassPathHelper {
    
    private final Set<ClassPath> boot;
    private final Set<ClassPath> compile;
    private final Set<ClassPath> source;

    private Set<ClassPath> moduleBoot;
    private Set<ClassPath> moduleCompile;
    private Set<ClassPath> moduleClass;
    private Set<ClassPath> moduleSource;
    
    /**
     * Creates a new JPAClassPathHelper. 
     * 
     * @param boot the boot class paths. Must not be null.
     * @param compile the compile class paths. Must not be null.
     * @param source the source class paths. Must not be null.
     */ 
    public JPAClassPathHelper(Set<ClassPath> boot, Set<ClassPath> compile, Set<ClassPath> source){
        Parameters.notNull("boot", boot);
        Parameters.notNull("compile", compile);
        Parameters.notNull("source", source);
        this.boot = new HashSet<ClassPath>(boot);
        this.compile = new HashSet<ClassPath>(compile);
        this.source = new HashSet<ClassPath>(source);
    }
    
    public JPAClassPathHelper setModuleBootPaths(Set<ClassPath> moduleBoot) {
        if (moduleBoot == null) {
            moduleBoot = Collections.emptySet();
        }
        this.moduleBoot = moduleBoot;
        return this;
    }

    public JPAClassPathHelper setModuleCompilePaths(Set<ClassPath> moduleCompile) {
        if (moduleCompile == null) {
            moduleCompile = Collections.emptySet();
        }
        this.moduleCompile = moduleCompile;
        return this;
    }

    public JPAClassPathHelper setModuleClassPaths(Set<ClassPath> moduleClass) {
        if (moduleClass == null) {
            moduleClass = Collections.emptySet();
        }
        this.moduleClass = moduleClass;
        return this;
    }

    public JPAClassPathHelper setModuleSourcePaths(Set<ClassPath> moduleSource) {
        if (moduleSource == null) {
            moduleSource = Collections.emptySet();
        }
        this.moduleSource = moduleSource;
        return this;
    }
    /**
     * Creates a ClassPathInfo (based on our class paths) that can be used for generating entities.
     * Ensures that the compile class path has the Java Persistence API present by checking
     * whether JPA is already present in the compile class path and if it isn't, adds
     * an appropriate JPA library to the compile class path. It is the client's responsibility
     * to make sure that the IDE has a library that contains the Java Persistence API. If no
     * appropriate library could be found, an IllegalStateException is thrown. 
     * 
     * @return the ClassPathInfo for generating entities.
     * @throws IllegalStateException if there were no libraries in the IDE that 
     * contain the Java Persistence API.
     */ 
    public ClasspathInfo createClasspathInfo(){
        
        if (!ensureJPA()){
            throw new IllegalStateException("Cannot find a Java Persistence API library"); // NOI18N
        }
        
        return new ClasspathInfo.Builder(createProxyClassPath(boot))
                .setModuleBootPath(createProxyClassPath(moduleBoot))
                .setClassPath(createProxyClassPath(compile))
                .setModuleCompilePath(createProxyClassPath(moduleCompile))
                .setModuleClassPath(createProxyClassPath(moduleClass))
                .setSourcePath(createProxyClassPath(source))
                .setModuleSourcePath(createProxyClassPath(moduleSource))
                .build();
    }
    
    /**
     * Ensure that the compile class path has the Java Persistence API present. Checks
     * whether JPA is already present in the compile class path and if not, tries to 
     * find an appropriate JPA library and add it to the compile class path.
     * 
     * @return true if the compile class path contained or could be made to contain
     * the Java Persistence API.
     */  
    private boolean ensureJPA() {
        for (ClassPath classPath : compile) {
            if (classPath.findResource("javax/persistence/Entity.class") != null) { // NOI18N
                return true;
            }
        }
        ClassPath jpaClassPath = findJPALibrary();
        if (jpaClassPath != null) {
            compile.add(jpaClassPath);
            return true;
        }
        
        return false;
    }

    private ClassPath findJPALibrary() {
        Library library = PersistenceLibrarySupport.getFirstProviderLibrary();
        if (library == null) {
            return null;
        }
        List<URL> urls = library.getContent("classpath"); // NOI18N
        return ClassPathSupport.createClassPath(urls.toArray(new URL[0]));
    }
    
    
    private ClassPath createProxyClassPath(Set<ClassPath> classPaths) {
        return classPaths.isEmpty() ? ClassPath.EMPTY : ClassPathSupport.createProxyClassPath(classPaths.toArray(new ClassPath[0]));
    }
}
