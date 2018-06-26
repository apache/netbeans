/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.symfony.util;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Tomas Mysik
 */
public final class SymfonyUtils {
    public static final String ACTION_METHOD_PREFIX = "execute"; // NOI18N
    public static final String ACTION_CLASS_SUFFIX = "actions"; // NOI18N

    private static final String FILE_ACTION = "actions.class.php"; // NOI18N
    private static final String FILE_ACTION_RELATIVE = "../actions/" + FILE_ACTION; // NOI18N

    private static final String DIR_TEMPLATES = "templates"; // NOI18N
    private static final String DIR_TEMPLATES_RELATIVE = "../" + DIR_TEMPLATES; // NOI18N
    private static final String VIEW_FILE_SUFFIX = "Success"; // NOI18N
    private static final String TEMPLATE_REGEX = "%s" + VIEW_FILE_SUFFIX + "(\\.\\w+)?\\.php"; // NOI18N

    private SymfonyUtils() {
    }

    public static boolean isView(FileObject fo) {
        File file = FileUtil.toFile(fo);
        return DIR_TEMPLATES.equals(file.getParentFile().getName());
    }

    public static boolean isViewWithAction(FileObject fo) {
        return isView(fo) && getAction(fo) != null;
    }

    public static boolean isAction(FileObject fo) {
        return FILE_ACTION.equals(fo.getNameExt());
    }

    public static FileObject getAction(FileObject fo) {
        File parent = FileUtil.toFile(fo).getParentFile();
        File action = PropertyUtils.resolveFile(parent, FILE_ACTION_RELATIVE);
        if (action.isFile()) {
            return FileUtil.toFileObject(action);
        }
        return null;
    }

    public static String getActionName(FileObject view) {
        return getActionName(view.getName());
    }

    static String getActionName(String viewName) {
        String[] parts = viewName.split("\\."); // NOI18N
        return ACTION_METHOD_PREFIX + parts[0].replaceAll(VIEW_FILE_SUFFIX + "$", "").toLowerCase(); // NOI18N
    }

    public static List<FileObject> getViews(FileObject fo, PhpBaseElement phpElement) {
        List<FileObject> views = new LinkedList<>();
        if (phpElement instanceof PhpClass.Method) {
            String methodName = phpElement.getName();
            if (methodName.startsWith(ACTION_METHOD_PREFIX)) {
                String partName = methodName.substring(ACTION_METHOD_PREFIX.length());
                views.addAll(getViews(fo, partName.substring(0, 1).toLowerCase() + partName.substring(1)));
            }
        }
        return views;
    }

    private static List<FileObject> getViews(FileObject fo, final String viewName) {
        List<FileObject> views = new LinkedList<>();
        File parent = FileUtil.toFile(fo).getParentFile();
        File templatesDir = PropertyUtils.resolveFile(parent, DIR_TEMPLATES_RELATIVE);
        File[] fileViews = templatesDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile() && pathname.getName().matches(String.format(TEMPLATE_REGEX, viewName))) {
                    return true;
                }
                return false;
            }
        });
        assert fileViews != null : templatesDir;
        for (File view : fileViews) {
            views.add(FileUtil.toFileObject(view));
        }
        return views;
    }
}
