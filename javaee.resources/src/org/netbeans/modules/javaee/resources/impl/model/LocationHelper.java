/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javaee.resources.impl.model;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.javaee.resources.api.model.Location;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class LocationHelper {

    private LocationHelper() {
    }

    /**
     * Obtains class location.
     * @param helper helper with classpaths where to search
     * @param fqn FQN name of the searched class
     * @return location of the class
     */
    public static Location getClassLocation(AnnotationModelHelper helper, String fqn) {
        String cpBase = fqn.replace('.', '/'); //NOI18N
        ClassPath sourceCP = helper.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
        FileObject classFO = sourceCP.findResource(cpBase + ".java"); //NOI18N

        if (classFO == null) {
            ClassPath compileCP = helper.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
            classFO = searchForFile(compileCP, cpBase);
        }

        if (classFO != null) {
            return new LocationImpl(classFO);
        }
        return null;
    }

    private static FileObject searchForFile(ClassPath cp, String cpBase) {
        FileObject file = getFileFromClasspath(cp, cpBase + ".java"); //NOI18N
        if (file == null) {
            return getFileFromClasspath(cp, cpBase + ".class"); //NOI18N
        } else {
            return file;
        }
    }

    private static FileObject getFileFromClasspath(ClassPath cp, String classRelativePath) {
        for (ClassPath.Entry entry : cp.entries()) {
            FileObject[] roots;
            if (entry.isValid()) {
                roots = new FileObject[]{entry.getRoot()};
            } else {
                SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots(entry.getURL());
                roots = res.getRoots();
            }
            for (FileObject root : roots) {
                FileObject metaInf = root.getFileObject(classRelativePath);
                if (metaInf != null) {
                    return metaInf;
                }
            }
        }

        return null;
    }

}
