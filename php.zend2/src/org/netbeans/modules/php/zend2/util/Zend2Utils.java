/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
