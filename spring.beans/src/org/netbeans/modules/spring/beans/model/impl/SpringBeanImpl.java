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
package org.netbeans.modules.spring.beans.model.impl;

import java.util.*;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.spring.api.beans.SpringAnnotations;
import org.netbeans.modules.spring.api.beans.model.Location;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeanProperty;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martinf Fousek <marfous@netbeans.org>
 */
public class SpringBeanImpl extends PersistentObject implements SpringBean, Refreshable {

    private String id;
    private List<String> names = new LinkedList<String>();
    private String className;
    private String parent;
    private String factoryBean;
    private String factoryMethod;
    private Set<SpringBeanProperty> properties;
    private Location location;

    protected SpringBeanImpl(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getParent() {
        return parent;
    }

    @Override
    public String getFactoryBean() {
        return factoryBean;
    }

    @Override
    public String getFactoryMethod() {
        return factoryMethod;
    }

    @Override
    public Set<SpringBeanProperty> getProperties() {
        return properties;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public final boolean refresh(TypeElement type) {
        Map<String, ? extends AnnotationMirror> types = getHelper().getAnnotationsByType(
                getHelper().getCompilationController().getElements().getAllAnnotationMirrors(type));
        AnnotationMirror annotationMirror = getAnnotationMirror(types);
        if (annotationMirror == null) {
            return false;
        }
        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectString("value", null); //NOI18N
        ParseResult parseResult = parser.parse(annotationMirror);
        className = ElementUtilities.getBinaryName(type);
        names.clear();
        if (parseResult.get("value", String.class) == null) { //NOI18N
            names.add(getConvertedClassName(className));
        } else {
            names.add(parseResult.get("value", String.class)); //NOI18N
        }

        refreshLocation(className);

        return true;
    }

    private void refreshLocation(String fqn) {
        String cpBase = fqn.replace('.', '/'); //NOI18N
        ClassPath sourceCP = getHelper().getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
        FileObject classFO = sourceCP.findResource(cpBase + ".java"); //NOI18N

        if (classFO == null) {
            ClassPath compileCP = getHelper().getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
            classFO = searchForFile(compileCP, cpBase);
        }

        if (classFO != null) {
            location = new SpringAnnotatedBeanLocation(classFO);
        }
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
            FileObject roots[];
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

    private static AnnotationMirror getAnnotationMirror(Map<String, ? extends AnnotationMirror> types) {
        Iterator<String> it = SpringAnnotations.SPRING_COMPONENTS.iterator();
        while (it.hasNext()) {
            AnnotationMirror annotationMirror = types.get(it.next());
            if (annotationMirror != null) {
                return annotationMirror;
            }
        }
        return null;
    }

    private static String getConvertedClassName(String className) {
        String result = className.substring(className.lastIndexOf(".") + 1); //NOI18N
        if (result.length() > 1) {
            StringBuilder builder = new StringBuilder();
            builder.append(
                    Character.toLowerCase(result.charAt(0)));
            builder.append(result.substring(1));
            result = builder.toString();
        }
        return result;
    }
}
