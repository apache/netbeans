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
package org.netbeans.modules.php.smarty.editor.utlis;

import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.smarty.SmartyFramework;
import org.netbeans.modules.php.smarty.SmartyPhpModuleCustomizerExtender;
import org.netbeans.modules.php.smarty.editor.TplMetaData;
import org.netbeans.modules.php.smarty.ui.options.SmartyOptions;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Fousek
 */
public final class TplUtils {

    private TplUtils() {
    }

    public static TplMetaData getProjectPropertiesForFileObject(FileObject fo) {
        String oDelim = SmartyOptions.getInstance().getDefaultOpenDelimiter();
        String cDelim = SmartyOptions.getInstance().getDefaultCloseDelimiter();
        SmartyFramework.Version version = SmartyOptions.getInstance().getSmartyVersion();

        if (fo != null) {
            PhpModule phpModule = PhpModule.Factory.forFileObject(fo);

            // file outside of any PHP project
            if (phpModule != null) {
                if (!SmartyPhpModuleCustomizerExtender.getCustomOpenDelimiter(phpModule).equals("")) {
                    oDelim = SmartyPhpModuleCustomizerExtender.getCustomOpenDelimiter(phpModule);
                }
                if (!SmartyPhpModuleCustomizerExtender.getCustomCloseDelimiter(phpModule).equals("")) {
                    cDelim = SmartyPhpModuleCustomizerExtender.getCustomCloseDelimiter(phpModule);
                }
            }
        }
        return new TplMetaData(oDelim, cDelim, version);
    }
}
