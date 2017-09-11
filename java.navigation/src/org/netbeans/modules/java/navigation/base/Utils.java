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
