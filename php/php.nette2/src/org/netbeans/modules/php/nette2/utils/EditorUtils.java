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
