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

package org.netbeans.modules.spring.beans.refactoring;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.modules.spring.java.PropertyType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class SpringRefactorings {

    private static final Logger LOGGER = Logger.getLogger(SpringRefactorings.class.getName());

    private static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N

    public static boolean isJavaFile(FileObject fo) {
        return JAVA_MIME_TYPE.equals(fo.getMIMEType());
    }
    
    public static RenamedProperty getRenamedProperty(final TreePathHandle oldHandle, final JavaSource javaSource, final String newName) throws IOException {
        final RenamedProperty[] result = { null };
        javaSource.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws Exception {
                cc.toPhase(Phase.RESOLVED);
                Element element = oldHandle.resolveElement(cc);
                if (element == null || element.getKind() != ElementKind.METHOD) {
                    return;
                }

                PropertyType type = null;
                ExecutableElement ee = (ExecutableElement) element;
                if (JavaUtils.isGetter(ee)) {
                    type = PropertyType.READ_ONLY;
                } else if (JavaUtils.isSetter(ee)) {
                    type = PropertyType.WRITE_ONLY;
                } else {
                    return;
                }

                // gather and keep all overridden methods plus current method
                Collection<ElementHandle<ExecutableElement>> methodHandles = JavaUtils.getOverridenMethodsAsHandles(ee, cc);
                methodHandles = new ArrayList<ElementHandle<ExecutableElement>>(methodHandles);
                methodHandles.add(ElementHandle.create(ee));
                
                String oldName = JavaUtils.getPropertyName(element.getSimpleName().toString());
                element = element.getEnclosingElement();
                result[0] = new RenamedProperty(methodHandles, oldName, JavaUtils.getPropertyName(newName), type);
            }
        }, true);
        return result[0];
    }

    public static RenamedClassName getRenamedClassName(final TreePathHandle oldHandle, final JavaSource javaSource, final String newName) throws IOException {
        final RenamedClassName[] result = { null };
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController cc) throws IOException {
                cc.toPhase(Phase.RESOLVED);
                Element element = oldHandle.resolveElement(cc);
                if (element == null || element.getKind() != ElementKind.CLASS) {
                    return;
                }
                String oldBinaryName = ElementUtilities.getBinaryName((TypeElement)element);
                String oldSimpleName = element.getSimpleName().toString();
                String newBinaryName = null;
                element = element.getEnclosingElement();
                if (element.getKind() == ElementKind.CLASS) {
                    newBinaryName = ElementUtilities.getBinaryName((TypeElement)element) + '$' + newName;
                } else if (element.getKind() == ElementKind.PACKAGE) {
                    String packageName = ((PackageElement)element).getQualifiedName().toString();
                    newBinaryName = createQualifiedName(packageName, newName);
                } else {
                    LOGGER.log(Level.WARNING, "Enclosing element of {0} was neither class nor package", oldHandle);
                }
                result[0] = new RenamedClassName(oldSimpleName, oldBinaryName, newBinaryName);
            }
        }, true);
        return result[0];
    }

    public static List<String> getTopLevelClassNames(FileObject fo) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fo);
        if (javaSource == null) {
            return Collections.emptyList();
        }
        final List<String> result = new ArrayList<String>(1);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController cc) throws IOException {
                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                for (TypeElement typeElement : cc.getTopLevelElements()) {
                    result.add(ElementUtilities.getBinaryName(typeElement));
                }
            }
        }, true);
        return result;
    }

    public static String getPackageName(FileObject folder) {
        ClassPath cp = ClassPath.getClassPath(folder, ClassPath.SOURCE);
        if (cp != null) {
            return cp.getResourceName(folder, '.', false);
        }
        return null;
    }

    public static String getRenamedPackageName(FileObject folder, String newName) {
        FileObject parent = folder.getParent();
        if (parent == null) {
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(parent, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }
        String parentName = cp.getResourceName(parent, '.', false);
        if (parentName == null) {
            return null;
        }
        if (parentName.length() > 0) {
            return parentName + '.' + newName;
        } else {
            return newName;
        }
    }

    public static String getPackageName(URL url) {
        File f = null;
        try {
            String path = URLDecoder.decode(url.getPath(), "UTF-8"); // NOI18N
            f = FileUtil.normalizeFile(new File(path));
        } catch (UnsupportedEncodingException u) {
            throw new IllegalArgumentException("Cannot create package name for URL " + url); // NOI18N
        }
        String suffix = "";
        do {
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null) {
                if ("".equals(suffix))
                    return getPackageName(fo);
                String prefix = getPackageName(fo);
                return prefix + ("".equals(prefix)?"":".") + suffix; // NOI18N
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix; // NOI18N
            }
            try {
                suffix = URLDecoder.decode(f.getPath().substring(f.getPath().lastIndexOf(File.separatorChar) + 1), "UTF-8") + suffix; // NOI18N
            } catch (UnsupportedEncodingException u) {
                throw new IllegalArgumentException("Cannot create package name for URL " + url); // NOI18N
            }
            f = f.getParentFile();
        } while (f!=null);
        throw new IllegalArgumentException("Cannot create package name for URL " + url); // NOI18N
    }

    public static String getSimpleElementName(String elementName) {
        for (;;) {
            if (elementName.length() == 0) {
                return elementName;
            }
            int lastDot = elementName.lastIndexOf('.');
            if (lastDot == -1) {
                return elementName;
            }
            if (lastDot == elementName.length() - 1) {
                elementName = elementName.substring(0, lastDot);
                continue;
            }
            return elementName.substring(lastDot + 1);
        }
    }

    public static String createQualifiedName(String packageName, String simpleName) {
        if (packageName.length() == 0) {
            return simpleName;
        } else {
            if (simpleName.length() == 0) {
                return packageName;
            } else {
                return packageName + '.' + simpleName;
            }
        }
    }

    public static final class RenamedClassName {

        private final String oldSimpleName;
        private final String oldBinaryName;
        private final String newBinaryName;

        public RenamedClassName(String oldSimpleName, String oldBinaryName, String newBinaryName) {
            this.oldSimpleName = oldSimpleName;
            this.oldBinaryName = oldBinaryName;
            this.newBinaryName = newBinaryName;
        }

        public String getOldSimpleName() {
            return oldSimpleName;
        }

        public String getOldBinaryName() {
            return oldBinaryName;
        }

        public String getNewBinaryName() {
            return newBinaryName;
        }
    }
    
    public static final class RenamedProperty {
        
        private final String oldName;
        private final String newName;
        private final PropertyType type;
        private final Collection<ElementHandle<ExecutableElement>> methodHandles;

        public RenamedProperty(Collection<ElementHandle<ExecutableElement>> methodHandles, String oldName, String newName, PropertyType type) {
            this.methodHandles = methodHandles;
            this.oldName = oldName;
            this.newName = newName;
            this.type = type;
        }

        public String getNewName() {
            return newName;
        }

        public String getOldName() {
            return oldName;
        }

        public PropertyType getType() {
            return type;
        }

        public Collection<ElementHandle<ExecutableElement>> getMethodHandles() {
            return methodHandles;
        }
    }
}
