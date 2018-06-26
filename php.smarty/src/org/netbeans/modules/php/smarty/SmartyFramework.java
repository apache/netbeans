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
