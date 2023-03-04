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

package org.netbeans.modules.java.navigation.base;

import java.io.CharConversionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 * @author Tomas Zezula
 */
public class Utils {

    private static final String CLASS_EXTENSION = "class"; // NOI18N
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    private Utils() {
        throw new IllegalStateException();
    }

    @CheckForNull
    public static String escape(@NonNull final String s) {
        if (s != null) {
            try {
                return XMLUtil.toAttributeValue(s);
            } catch (CharConversionException ex) {
            }
        }
        return null;
    }


    @CheckForNull
    public static FileObject getFile(
            @NonNull final ElementHandle<TypeElement> toResolve,
            @NonNull final ClasspathInfo cpInfo) {
        FileObject res = SourceUtils.getFile(toResolve, cpInfo);
        if (res == null) {
            final ClassPath cp = ClassPathSupport.createProxyClassPath(
                    cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT),
                    cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE));
            res = cp.findResource(String.format(
                    "%s.%s",    //NOI18N
                    toResolve.getBinaryName().replace('.', '/'),    //NOI18N
                    CLASS_EXTENSION));
        }
        return res;
    }

    @NonNull
    public static <T extends JComponent> T updateBackground(@NonNull final T comp) {
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            comp.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
        return comp;
    }

    @CheckForNull
    public static <T extends Element> ElementHandle<T> createElementHandle(@NonNull final T element) {
        //ElementKind OTHER represents errors <any>, <none>.
        //These are special errors which cannot be resolved
        if (element.getKind() == ElementKind.OTHER) {
            return null;
        }
        try {
            return ElementHandle.create(element);
        } catch (IllegalArgumentException e) {
            LOG.log(
                    Level.INFO,
                    "Unresolvable element: {0}, reason: {1}",    //NOI18N
                    new Object[]{
                        element,
                        e.getMessage()
                    });
            return null;
        }
    }

    public static boolean signatureEquals(
        @NonNull final ElementHandle<Element> handle,
        @NonNull final Element element) {
        if (handle == null) {
            return false;
        }
        //ElementKind OTHER represents errors <any>, <none>.
        //These are special errors which cannot be resolved
        if (element.getKind() == ElementKind.OTHER) {
            return false;
        }
        try {
            return handle.signatureEquals(element);
        } catch (IllegalArgumentException e) {
            LOG.log(
                Level.INFO,
                "Unresolvable element: {0}, reason: {1}",    //NOI18N
                new Object[]{
                    element,
                    e.getMessage()
                });
            return false;
        }
    }
}
