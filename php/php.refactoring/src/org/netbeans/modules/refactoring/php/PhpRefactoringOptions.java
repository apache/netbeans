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
package org.netbeans.modules.refactoring.php;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class PhpRefactoringOptions {
    private static final PhpRefactoringOptions INSTANCE = new PhpRefactoringOptions();
    private static final String RENAME_FILE = "php-refactoring-rename-file"; //NOI18N
    private static final boolean RENAME_FILE_DEFAULT = false;
    private static final String LOWER_CASE_FILE_NAME = "php-refactoring-lower-case-file-name"; //NOI18N
    private static final boolean LOWER_CASE_FILE_NAME_DEFAULT = false;

    private PhpRefactoringOptions() {
    }

    public static PhpRefactoringOptions getInstance() {
        return INSTANCE;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(PhpRefactoringOptions.class);
    }

    public void setRenameFile(boolean renameFile) {
        getPreferences().putBoolean(RENAME_FILE, renameFile);
    }

    public boolean getRenameFile() {
        return getPreferences().getBoolean(RENAME_FILE, RENAME_FILE_DEFAULT);
    }

    public void setLowerCaseFileName(boolean lowerCaseFileName) {
        getPreferences().putBoolean(LOWER_CASE_FILE_NAME, lowerCaseFileName);
    }

    public boolean getLowerCaseFileName() {
        return getPreferences().getBoolean(LOWER_CASE_FILE_NAME, LOWER_CASE_FILE_NAME_DEFAULT);
    }

}
