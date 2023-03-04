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

package org.netbeans.modules.php.zend.util;

import java.io.File;
import java.util.regex.Pattern;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Tomas Mysik
 */
public final class ZendUtils {
    public static final String CONTROLLER_FILE_SUFIX = "Controller"; // NOI18N
    public static final String CONTROLLER_CLASS_SUFFIX = "Controller"; // NOI18N
    public static final String CONTROLLER_METHOD_SUFIX = "Action"; // NOI18N

    private static final String DIR_CONTROLLERS = "controllers"; // NOI18N
    private static final String DIR_SCRIPTS = "scripts"; // NOI18N
    private static final String DIR_VIEWS = "views"; // NOI18N
    private static final String FILE_VIEW_EXT = "phtml"; // NOI18N

    private static final String FILE_CONTROLLER_RELATIVE = "../../../" + DIR_CONTROLLERS + "/%s.php"; // NOI18N

    private static final String FILE_VIEW_RELATIVE = "../" + DIR_VIEWS + "/" + DIR_SCRIPTS + "/%s/%s." + FILE_VIEW_EXT; // NOI18N

    private static final String DASH = "-"; // NOI18N

    private ZendUtils() {
    }

    public static boolean isView(FileObject fo) {
        if (!fo.isData() || !fo.getExt().equals(FILE_VIEW_EXT)) {
            return false;
        }
        File file = FileUtil.toFile(fo);
        File parent = file.getParentFile(); // controller
        if (parent == null) {
            return false;
        }
        parent = parent.getParentFile(); // scripts
        if (!DIR_SCRIPTS.equals(parent.getName())) {
            return false;
        }
        parent = parent.getParentFile(); // scripts
        return parent != null && DIR_VIEWS.equals(parent.getName()); // views
    }

    public static boolean isViewWithAction(FileObject fo) {
        return isView(fo) && getAction(fo) != null;
    }

    public static boolean isAction(FileObject fo) {
        return fo.isData() && fo.getName().endsWith(CONTROLLER_FILE_SUFIX) && fo.getParent().getNameExt().equals(DIR_CONTROLLERS);
    }

    public static FileObject getAction(FileObject view) {
        File parent = FileUtil.toFile(view).getParentFile();
        File action = PropertyUtils.resolveFile(parent, String.format(FILE_CONTROLLER_RELATIVE, getControllerName(parent.getName())));
        if (action.isFile()) {
            return FileUtil.toFileObject(action);
        }
        return null;
    }

    public static FileObject getView(FileObject fo, PhpBaseElement phpElement) {
        FileObject view = null;
        if (phpElement instanceof PhpClass.Method) {
            view = getView(fo, getViewName(phpElement.getName()));
        }
        return view;
    }

    private static FileObject getView(FileObject fo, String viewName) {
        File parent = FileUtil.toFile(fo).getParentFile();
        File view = PropertyUtils.resolveFile(parent, String.format(FILE_VIEW_RELATIVE, getViewFolderName(fo.getName()), viewName));
        if (view.isFile()) {
            return FileUtil.toFileObject(view);
        }
        return null;
    }

    static String getViewName(String actionName) {
        if (!actionName.endsWith(CONTROLLER_METHOD_SUFIX)) {
            return null;
        }
        return dashize(actionName.replace(CONTROLLER_METHOD_SUFIX, "")); // NOI18N
    }

    // for unit tests
    static String getViewFolderName(String controllerName) {
        return dashize(controllerName.replace(CONTROLLER_FILE_SUFIX, "")); // NOI18N
    }

    // for unit tests
    static String getControllerName(String viewFolderName) {
        return undashize(viewFolderName, false) + CONTROLLER_CLASS_SUFFIX;
    }

    public static String getActionName(FileObject view) {
        return getActionName(view.getName());
    }

    // for unit tests
    public static String getActionName(String viewName) {
        return undashize(viewName, true) + CONTROLLER_METHOD_SUFIX;
    }

    // all-jobs -> AllJobs or allJobs
    private static String undashize(String input, boolean firstLowerCase) {
        StringBuilder sb = new StringBuilder(input.length());
        boolean first = firstLowerCase;
        for (String part : input.split(Pattern.quote(DASH))) {
            if (first) {
                first = false;
                sb.append(part);
            } else {
                sb.append(part.substring(0, 1).toUpperCase());
                sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    // AllJobs -> all-jobs
    private static String dashize(String input) {
        StringBuilder sb = new StringBuilder(2 * input.length());
        for (int i = 0; i < input.length(); ++i) {
            char ch = input.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i != 0) {
                    sb.append(DASH);
                }
                sb.append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

}
