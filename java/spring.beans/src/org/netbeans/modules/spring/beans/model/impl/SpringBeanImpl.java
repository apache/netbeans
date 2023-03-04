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
