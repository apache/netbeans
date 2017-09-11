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
        return ClassPathSupport.createClassPath(urls.toArray(new URL[urls.size()]));
    }
    
    
    private ClassPath createProxyClassPath(Set<ClassPath> classPaths) {
        return classPaths.isEmpty() ? ClassPath.EMPTY : ClassPathSupport.createProxyClassPath(classPaths.toArray(new ClassPath[classPaths.size()]));
    }
}
