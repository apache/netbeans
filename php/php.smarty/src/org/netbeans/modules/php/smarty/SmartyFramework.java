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

package org.netbeans.modules.php.smarty;

import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.smarty.ui.options.SmartyOptions;
import org.openide.filesystems.FileObject;


/**
 * @author Martin Fousek
 */
public class SmartyFramework {

    public static final String BASE_CLASS_NAME = "Smarty"; // NOI18N

    /**
     * Smarty framework defined open delimiter.
     */
    public static final String OPEN_DELIMITER = "{"; //NOI18N
    /**
     * Smarty framework defined close delimiter.
     */
    public static final String CLOSE_DELIMITER = "}"; //NOI18N
    /**
     * Open delimiter in SMARTY templates.
     */
    private static String delimiterDefaultOpen = SmartyOptions.getInstance().getDefaultOpenDelimiter();
    /**
     * Close delimiter in SMARTY templates.
     */
    private static String delimiterDefaultClose = SmartyOptions.getInstance().getDefaultCloseDelimiter();
    /**
     * Version of SMARTY templates.
     */
    private static Version smartyVersion = Version.SMARTY3;
    /**
     * Toggle comment option. How to comment inside .tpl files.
     */
    private static ToggleCommentOption toggleCommentOption = ToggleCommentOption.SMARTY;

    /**
     * Gets the close delimiter for the given file. In relation to the owning project and general settings.
     * @param fileObject any FileObject, never {@code null}
     * @return close delimiter of the project which is owning that file if any or global close delimiter from Options
     */
    public static String getCloseDelimiter(FileObject fileObject) {
        PhpModule phpModule = PhpModule.Factory.forFileObject(fileObject);
        if (phpModule != null && !SmartyPhpModuleCustomizerExtender.getCustomCloseDelimiter(phpModule).isEmpty()) {
            return SmartyPhpModuleCustomizerExtender.getCustomCloseDelimiter(phpModule);
        } else {
            return delimiterDefaultClose;
        }
    }

    /**
     * Gets the open delimiter for the given file. In relation to the owning project and general settings.
     * @param fileObject any FileObject, never {@code null}
     * @return open delimiter of the project which is owning that file if any or global open delimiter from Options
     */
    public static String getOpenDelimiter(FileObject fileObject) {
        PhpModule phpModule = PhpModule.Factory.forFileObject(fileObject);
        if (phpModule != null && !SmartyPhpModuleCustomizerExtender.getCustomOpenDelimiter(phpModule).isEmpty()) {
            return SmartyPhpModuleCustomizerExtender.getCustomOpenDelimiter(phpModule);
        } else {
            return delimiterDefaultOpen;
        }
    }

    public static String getDelimiterDefaultOpen() {
        return delimiterDefaultOpen;
    }

    public static String getDelimiterDefaultClose() {
        return delimiterDefaultClose;
    }

    public static void setDelimiterDefaultClose(String delimiterDefaultClose) {
        SmartyFramework.delimiterDefaultClose = delimiterDefaultClose;
    }

    public static void setDelimiterDefaultOpen(String delimiterDefaultOpen) {
        SmartyFramework.delimiterDefaultOpen = delimiterDefaultOpen;
    }

    public static void setSmartyVersion(Version version) {
        SmartyFramework.smartyVersion = version;
    }

    public static void setToggleCommentOption(ToggleCommentOption toggleCommentOption) {
        SmartyFramework.toggleCommentOption = toggleCommentOption;
    }

    public enum Version {
        SMARTY3,
        SMARTY2;
    }

    public enum ToggleCommentOption {
        SMARTY,
        CONTEXT
    }
}
