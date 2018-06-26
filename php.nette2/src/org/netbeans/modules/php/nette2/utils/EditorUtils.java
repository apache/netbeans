/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.nette2.utils;

import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class EditorUtils {
    private static final String FILE_PRESENTER_RELATIVE_CLASSIC =
            "../../presenters/%s" + Constants.NETTE_PRESENTER_SUFFIX + Constants.NETTE_PRESENTER_EXTENSION; //NOI18N
    private static final String FILE_PRESENTER_RELATIVE_DOTTED =
            "../presenters/%s" + Constants.NETTE_PRESENTER_SUFFIX + Constants.NETTE_PRESENTER_EXTENSION; //NOI18N
    private static final String FILE_VIEW_RELATIVE_CLASSIC = "../templates/%s/%s" + Constants.LATTE_TEMPLATE_EXTENSION; //NOI18N
    private static final String FILE_VIEW_RELATIVE_DOTTED = "../templates/%s.%s" + Constants.LATTE_TEMPLATE_EXTENSION; //NOI18N
    private static final String MODULE_PREFIX_PATTERN = "^(.*)_"; //NOI18N

    private EditorUtils() {
    }

    public static String firstLetterSmall(String text) {
        assert text != null;
        String result = text;
        if (text.length() > 0) {
            result = text.substring(0, 1).toLowerCase() + text.substring(1);
        }
        return result;
    }

    public static String firstLetterCapital(String text) {
        assert text != null;
        String result = text;
        if (text.length() > 0) {
            result = text.substring(0, 1).toUpperCase() + text.substring(1);
        }
        return result;
    }

    public static boolean isViewWithAction(FileObject fileObject) {
        assert fileObject != null;
        return isView(fileObject) && getAction(fileObject) != null;
    }

    public static boolean isView(FileObject fileObject) {
        assert fileObject != null;
        return Constants.LATTE_MIME_TYPE.equals(fileObject.getMIMEType());
    }

    public static FileObject getAction(FileObject fileObject) {
        assert fileObject != null;
        FileObject result = null;
        FileObject parent = fileObject.getParent();
        if (parent != null) {
            String presenterName = parent.getName();
            String relativePresenterPath = String.format(resolveActionRelativePath(fileObject), presenterName);
            result = fileObject.getFileObject(relativePresenterPath);
        }
        return result;
    }

    private static String resolveActionRelativePath(FileObject fo) {
        String result = FILE_PRESENTER_RELATIVE_CLASSIC;
        if (isDottedView(fo)) {
            result = FILE_PRESENTER_RELATIVE_DOTTED;
        }
        return result;
    }

    private static boolean isDottedView(FileObject fo) {
        return firstLetterCapital(fo.getName()).equals(fo.getName());
    }

    public static boolean isAction(FileObject fileObject) {
        assert fileObject != null;
        return fileObject.isData() && fileObject.getName().endsWith(Constants.NETTE_PRESENTER_SUFFIX);
    }

    public static FileObject getView(FileObject fileObject, PhpBaseElement phpElement) {
        assert fileObject != null;
        assert phpElement != null;
        FileObject result = null;
        if (phpElement instanceof PhpClass.Method) {
            result = getView(fileObject, getViewName(phpElement.getName()));
        }
        return result;
    }

    private static String getViewName(String actionName) {
        assert actionName != null;
        String result = null;
        if (actionName.startsWith(Constants.NETTE_ACTION_METHOD_PREFIX) || actionName.startsWith(Constants.NETTE_RENDER_METHOD_PREFIX)) {
            result = extractActionSimpleName(actionName);
        }
        return result;
    }

    private static String extractActionSimpleName(String actionName) {
        assert actionName != null;
        String simple;
        if (actionName.startsWith(Constants.NETTE_ACTION_METHOD_PREFIX)) {
            simple = actionName.replace(Constants.NETTE_ACTION_METHOD_PREFIX, ""); // NOI18N
        } else {
            simple = actionName.replace(Constants.NETTE_RENDER_METHOD_PREFIX, ""); // NOI18N
        }
        return firstLetterSmall(simple);
    }

    private static FileObject getView(FileObject fileObject, String viewName) {
        assert fileObject != null;
        assert viewName != null;
        FileObject result = null;
        FileObject parent = fileObject.getParent();
        if (parent != null) {
            String presenterName = extractPresenterName(fileObject.getName());
            String relativeClassicViewPath = String.format(FILE_VIEW_RELATIVE_CLASSIC, presenterName, viewName);
            FileObject classicView = parent.getFileObject(relativeClassicViewPath);
            if (classicView != null && !classicView.isFolder()) {
                result = classicView;
            } else {
                String relativeDottedViewPath = String.format(FILE_VIEW_RELATIVE_DOTTED, presenterName, viewName);
                FileObject dottedView = parent.getFileObject(relativeDottedViewPath);
                if (dottedView != null && !dottedView.isFolder()) {
                    result = dottedView;
                }
            }
        }
        return result;
    }

    private static String extractPresenterName(String presenterFileName) {
        assert presenterFileName != null;
        String presenterSimpleName = presenterFileName
                .replaceAll(Constants.NETTE_PRESENTER_EXTENSION, "")
                .replaceAll(Constants.NETTE_PRESENTER_SUFFIX, "")
                .replaceFirst(MODULE_PREFIX_PATTERN, ""); //NOI18N
        return firstLetterCapital(presenterSimpleName);
    }

    public static String getActionName(FileObject view) {
        assert view != null;
        return getActionRenderName(view, Constants.NETTE_ACTION_METHOD_PREFIX);
    }

    public static String getRenderName(FileObject view) {
        assert view != null;
        return getActionRenderName(view, Constants.NETTE_RENDER_METHOD_PREFIX);
    }

    private static String getActionRenderName(FileObject view, String methodPrefix) {
        assert view != null;
        assert methodPrefix != null;
        String actionSimpleName;
        String[] parts;
        if (isDottedView(view)) {
            parts = view.getNameExt().split("\\.", 3); //NOI18N
            actionSimpleName = parts[1];
        } else {
            parts = view.getNameExt().split("\\."); //NOI18N
            actionSimpleName = parts[0];
        }
        return methodPrefix + firstLetterCapital(actionSimpleName);
    }

}
