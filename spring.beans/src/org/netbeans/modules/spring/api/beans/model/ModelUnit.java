/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spring.api.beans.model;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.openide.filesystems.FileObject;

/**
 * Holds compile, boot and source paths of project.
 * 
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ModelUnit {

    private final ClasspathInfo classPathInfo;
    private final ClassPath bootPath;
    private final ClassPath compilePath;
    private final ClassPath sourcePath;

    private ModelUnit(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath) {
        this.bootPath = bootPath;
        this.compilePath = compilePath;
        this.sourcePath = sourcePath;
        this.classPathInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
    }


    public ClassPath getBootPath() {
        return bootPath;
    }

    public ClassPath getCompilePath() {
        return compilePath;
    }

    public ClassPath getSourcePath() {
        return sourcePath;
    }
    
    public ClasspathInfo getClassPathInfo() {
        return classPathInfo;
    }

    public static ModelUnit create(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath) {
        return new ModelUnit(bootPath, compilePath, sourcePath);
    }

    FileObject getSourceFileObject() {
        FileObject[] roots = sourcePath.getRoots();
        if (roots != null && roots.length > 0) {
            return roots[0];
        }
        return null;
    }

    /**
     * Returns hashCode computed from {@link ModelUnit}'s {@link ClassPath}'s entries.
     */
    @Override
    public int hashCode() {       
        return computeModelUnitHash(this);
    }
    
    /**
     * Equivalent state is based on the same {@link ClassPath}s 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModelUnit) {
            ModelUnit unit = (ModelUnit) obj;
            return hasClassPathesIdenticEntries(unit.getBootPath(), bootPath)
                    && hasClassPathesIdenticEntries(unit.compilePath, compilePath)
                    && hasClassPathesIdenticEntries(unit.sourcePath, sourcePath);
        } else {
            return false;
        }
    }
    
    private static boolean hasClassPathesIdenticEntries(ClassPath cp1, ClassPath cp2) {
        if (cp1.entries().size() != cp2.entries().size()) {
            return false;
        }
        for (int i = 0; i < cp1.entries().size(); i++) {
            if (!cp1.entries().get(i).getURL().sameFile(cp2.entries().get(i).getURL())) {
                return false;
            }
        }
        return true;
    }
    
    private static int computeModelUnitHash(ModelUnit unit) {
        return computeClassPathHash(unit.getBootPath()) + computeClassPathHash(unit.getCompilePath()) +
                computeClassPathHash(unit.getSourcePath());
    }
     
    private static int computeClassPathHash(ClassPath classPath) {
        int hashValue = 0;
        for (ClassPath.Entry entry : classPath.entries()) {
            hashValue += entry.getURL().getPath().hashCode();
        }
        return hashValue;
    }
}
