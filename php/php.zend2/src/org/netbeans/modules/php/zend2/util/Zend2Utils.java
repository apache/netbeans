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
package org.netbeans.modules.php.zend2.util;

import java.io.File;
import java.util.regex.Pattern;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.spi.project.support.ant.PropertyUtils;

public final class Zend2Utils {

    // controllers
    private static final String CONTROLLER_DIRECTORY = "Controller"; // NOI18N
    private static final String CONTROLLER_FILE_SUFFIX = "Controller.php"; // NOI18N
    public static final String CONTROLLER_CLASS_SUFFIX = "Controller"; // NOI18N
    private static final String CONTROLLER_METHOD_SUFFIX = "Action"; // NOI18N
    // views
    private static final String VIEW_DIRECTORY = "view"; // NOI18N
    private static final String FILE_VIEW_EXTENSION = ".phtml"; // NOI18N
    // paths
    private static final String CONTROLLER_RELATIVE_FILE = "../../../src/%s/" + CONTROLLER_DIRECTORY + "/%s.php"; // NOI18N
    private static final String VIEW_RELATIVE_FILE = "../../../" + VIEW_DIRECTORY + "/%s/%s/%s" + FILE_VIEW_EXTENSION; // NOI18N
    // other
    private static final String DASH = "-"; // NOI18N


    private Zend2Utils() {
    }

    public static boolean isViewWithAction(File file) {
        return isView(file) && getController(file) != null;
    }

    public static boolean isView(File file) {
        if (file == null) {
            return false;
        }
        if (!file.isFile() || !file.getName().endsWith(FILE_VIEW_EXTENSION)) {
            return false;
        }
        File parent = file.getParentFile(); // controller
        if (parent == null) {
            return false;
        }
        parent = parent.getParentFile(); // module
        if (parent == null) {
            return false;
        }
        parent = parent.getParentFile(); // view
        return VIEW_DIRECTORY.equals(parent.getName());
    }

    public static File getView(File controller, PhpBaseElement phpElement) {
        if (phpElement instanceof PhpClass.Method) {
            String namespace = getNamespaceFromController(controller);
            String viewFolderName = getViewFolderName(controller.getName());
            String viewName = getViewName(phpElement.getName());
            File view = PropertyUtils.resolveFile(controller.getParentFile(), String.format(VIEW_RELATIVE_FILE, dashize(namespace), viewFolderName, viewName));
            if (view.isFile()) {
                return view;
            }
        }
        return null;
    }

    static String getViewName(String actionName) {
        if (!actionName.endsWith(CONTROLLER_METHOD_SUFFIX)) {
            return null;
        }
        return dashize(actionName.replace(CONTROLLER_METHOD_SUFFIX, "")); // NOI18N
    }

    static String getViewFolderName(String controllerName) {
        return dashize(controllerName.replace(CONTROLLER_FILE_SUFFIX, "")); // NOI18N
    }

    public static boolean isController(File file) {
        return file.isFile()
                && file.getName().endsWith(CONTROLLER_FILE_SUFFIX)
                && file.getParentFile().getName().equals(CONTROLLER_DIRECTORY);
    }

    public static File getController(File view) {
        String namespace = getNamespaceFromView(view);
        String controllerName = getControllerName(view);
        File controller = PropertyUtils.resolveFile(view.getParentFile(), String.format(CONTROLLER_RELATIVE_FILE, namespace, controllerName));
        if (controller.isFile()) {
            return controller;
        }
        return null;
    }

    static String getNamespaceFromView(File view) {
        return undashize(view.getParentFile().getParentFile().getName(), false);
    }

    static String getNamespaceFromController(File controller) {
        return controller.getParentFile().getParentFile().getName();
    }

    static String getControllerName(File view) {
        return getControllerName(view.getParentFile().getName());
    }

    static String getControllerName(String viewFolderName) {
        return undashize(viewFolderName, false) + CONTROLLER_CLASS_SUFFIX;
    }

    public static String getActionName(File view) {
        return getActionName(view.getName().substring(0, view.getName().length() - FILE_VIEW_EXTENSION.length()));
    }

    static String getActionName(String viewName) {
        return undashize(viewName, true) + CONTROLLER_METHOD_SUFFIX;
    }

    // AllJobs -> all-jobs
    static String dashize(String input) {
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

    // all-jobs -> AllJobs or allJobs
    static String undashize(String input, boolean firstLowerCase) {
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

}
