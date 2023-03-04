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
package org.netbeans.modules.php.editor.completion;

import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class LatteUtils {
    private static final String LATTE_EXTENSION = "latte"; //NOI18N
    private static final String DOTTED_RELATIVE_PRESENTER_PATH = "../../presenters/"; //NOI18N
    private static final String COMMON_RELATIVE_PRESENTER_PATH = "../" + DOTTED_RELATIVE_PRESENTER_PATH; //NOI18N
    private static final String PRESENTER_CLASS_SUFFIX = "Presenter"; //NOI18N
    private static final String PRESENTER_FILE_SUFFIX = PRESENTER_CLASS_SUFFIX + ".php"; //NOI18N

    private LatteUtils() {
    }

    public static boolean isView(FileObject templateFile) {
        assert templateFile != null;
        return LATTE_EXTENSION.equals(templateFile.getExt());
    }

    public static FileObject getPresenterFile(FileObject templateFile) {
        String templateName = templateFile.getName();
        String presenterName;
        String relativePath;
        if (templateName.contains(".")) { //NOI18N
            String[] parts = templateName.split("\\."); //NOI18N
            assert parts.length > 0;
            presenterName = parts[0];
            relativePath = DOTTED_RELATIVE_PRESENTER_PATH;
        } else {
            presenterName = templateFile.getParent().getName();
            relativePath = COMMON_RELATIVE_PRESENTER_PATH;
        }
        return templateFile.getFileObject(relativePath + StringUtils.capitalize(presenterName) + PRESENTER_FILE_SUFFIX);
    }

}
