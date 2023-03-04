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
package org.netbeans.modules.php.symfony2.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class SymfonyUtils {

    private static final String TWIG_MIME_TYPE = "text/x-twig"; // NOI18N
    private static final String SRC_DIR_NAME = "src"; // NOI18N
    private static final String RESOURCES_DIR_NAME = "Resources"; // NOI18N
    private static final String VIEWS_DIR_NAME = "views"; // NOI18N
    private static final String RESOURCES_VIEWS_REL_PATH = RESOURCES_DIR_NAME + '/' + VIEWS_DIR_NAME + '/'; // NOI18N
    private static final String RESOURCES_VIEWS_ABS_PATH = File.separator + RESOURCES_DIR_NAME + File.separator + VIEWS_DIR_NAME + File.separator; // NOI18N
    private static final String CONTROLLER_DIR_NAME = "Controller"; // NOI18N
    private static final String CONTROLLER_SUFFIX = "Controller.php"; // NOI18N
    private static final String VIEW_HTML_SUFFIX = ".html"; // NOI18N
    private static final String VIEW_HTML_TWIG_SUFFIX = VIEW_HTML_SUFFIX + ".twig"; // NOI18N
    private static final String ACTION_SUFFIX = "Action"; // NOI18N
    private static final String BUNDLE_SUFFIX = "Bundle"; // NOI18N


    private SymfonyUtils() {
    }

    public static boolean isViewWithAction(FileObject file) {
        if (!isView(file)) {
            return false;
        }
        return getViewRelPath(file) != null;
    }

    public static boolean isController(FileObject file) {
        if (!isController(file.getNameExt())) {
            return false;
        }
        return getControllerRelPath(file) != null;
    }

    @CheckForNull
    public static FileObject getController(FileObject sourceDir, FileObject view) {
        assert isView(view) : view;
        List<String> viewRelPath = getViewRelPath(view);
        assert viewRelPath != null : view;
        FileObject src = sourceDir.getFileObject(SRC_DIR_NAME);
        if (src == null) {
            return null;
        }
        int size = viewRelPath.size();
        StringBuilder sb = new StringBuilder();
        sb.append(CONTROLLER_DIR_NAME);
        sb.append('/'); // NOI18N
        for (int i = 0; i < size; i++) {
            sb.append(capitalize(viewRelPath.get(i), true));
            if (i < size - 1) {
                sb.append('/'); // NOI18N
            }
        }
        sb.append(CONTROLLER_SUFFIX);
        String controllerPath = sb.toString();
        // iterate over all bundles, the first with controller wins
        for (FileObject child : src.getChildren()) {
            if (child.isData()) {
                continue;
            }
            if (!child.getNameExt().endsWith(BUNDLE_SUFFIX)) {
                continue;
            }
            FileObject controller = child.getFileObject(controllerPath);
            if (controller != null) {
                return controller;
            }
        }
        return null;
    }

    public static String getActionMethodName(String viewName) {
        String actionName = viewName;
        if (actionName.endsWith(VIEW_HTML_SUFFIX)) {
            actionName = actionName.substring(0, actionName.length() - VIEW_HTML_SUFFIX.length());
        }
        return capitalize(actionName, false) + ACTION_SUFFIX;
    }

    @CheckForNull
    public static FileObject getView(FileObject controller, FileObject appDir, PhpBaseElement phpElement) {
        if (!(phpElement instanceof PhpClass.Method)) {
            return null;
        }
        String methodName = phpElement.getName();
        if (!methodName.endsWith(ACTION_SUFFIX)) {
            return null;
        }
        String viewName = methodName.substring(0, methodName.length() - ACTION_SUFFIX.length());
        List<String> relPath = getControllerRelPath(controller);
        assert relPath != null : controller;
        StringBuilder sb = new StringBuilder();
        sb.append(RESOURCES_VIEWS_REL_PATH);
        for (int i = relPath.size() - 1; i >= 0; i--) {
            sb.append(decapitalize(relPath.get(i)));
            sb.append('/'); // NOI18N
        }
        sb.append(decapitalize(viewName));
        sb.append(VIEW_HTML_TWIG_SUFFIX);
        return appDir.getFileObject(sb.toString());
    }

    @CheckForNull
    private static List<String> getViewRelPath(FileObject view) {
        assert isView(view) : view;
        final String name = view.getNameExt();
        if (StringUtils.hasText(name)
                && name.charAt(0) == '_') {
            // include
            return null;
        }
        String absolutePath = FileUtil.toFile(view).getAbsolutePath();
        int lastIndexOf = absolutePath.lastIndexOf(RESOURCES_VIEWS_ABS_PATH);
        if (lastIndexOf == -1) {
            return null;
        }
        return StringUtils.explode(
                absolutePath.substring(lastIndexOf + RESOURCES_VIEWS_ABS_PATH.length(), absolutePath.length() - name.length()), File.separator);
    }

    @CheckForNull
    private static List<String> getControllerRelPath(FileObject controller) {
        assert isController(controller.getNameExt()) : controller;
        List<String> path = new ArrayList<>();
        String name = controller.getNameExt();
        path.add(name.substring(0, name.length() - CONTROLLER_SUFFIX.length()));
        FileObject parent = controller.getParent();
        while (parent != null) {
            String parentName = parent.getNameExt();
            if (parentName.equals(CONTROLLER_DIR_NAME)) {
                FileObject bundle = parent.getParent();
                if (!bundle.getNameExt().endsWith(BUNDLE_SUFFIX)) {
                    return null;
                }
                break;
            }
            path.add(parentName);
            parent = parent.getParent();
        }
        return path;
    }

    private static boolean isView(FileObject file) {
        return FileUtil.getMIMEType(file, null, TWIG_MIME_TYPE) != null;
    }

    public static boolean isController(String filename) {
        return filename.endsWith(CONTROLLER_SUFFIX);
    }

    static String capitalize(String input, boolean capitalizeFirstChar) {
        assert input != null;
        StringBuilder sb = new StringBuilder(input.length());
        boolean capitalize = capitalizeFirstChar;
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '_') {
                capitalize = true;
                continue;
            }
            if (capitalize) {
                capitalize = false;
                ch = Character.toUpperCase(ch);
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    static String decapitalize(String input) {
        assert input != null;
        StringBuilder sb = new StringBuilder(input.length() * 2);
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    sb.append('_'); // NOI18N
                }
                ch = Character.toLowerCase(ch);
            }
            sb.append(ch);
        }
        return sb.toString();
    }

}
