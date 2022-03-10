/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.lsp.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Some Clangd startup options (others are hard-coded in the ClangdProcess).
 *
 * @author antonio
 */
public final class ClangdOptions {

    private static final String CLANGD_LOG_LEVEL_NAME = "clangd.log.level"; // NOI18N
    private static final String CLANGD_LOG_FILE_NAME = "clangd.log.file"; // NOI18N
    private static final String CLANGD_EXECUTABLE = "clangd.exe"; // NOI18N

    /**
     * The --log= flag to Clangd.
     */
    public static enum ClangdLogLevel {
        ERROR,
        INFO,
        VERBOSE
    };

    /**
     * Loads Clangd option from preferences.
     *
     * @return The ClangdOptions from preferences.
     */
    public static final ClangdOptions load() {
        Preferences preferences = NbPreferences.forModule(ClangdOptions.class);

        String levelString = preferences.get(CLANGD_LOG_LEVEL_NAME, ClangdLogLevel.VERBOSE.name());
        ClangdLogLevel level = ClangdLogLevel.valueOf(levelString);
        String logFile = preferences.get(CLANGD_LOG_FILE_NAME, "");
        String clangdExecutable = preferences.get(CLANGD_EXECUTABLE, "");

        ClangdOptions options = new ClangdOptions();
        options.setLevel(level);
        options.setLogFile("".equals(logFile) ? null : new File(logFile));
        options.setClangdExecutable("".equals(clangdExecutable) ? null : new File(clangdExecutable));

        return options;
    }

    /**
     * Saves Clangd startup options to preferences.
     *
     * @param options The options to save
     * @return The options saved.
     */
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public static final ClangdOptions save(ClangdOptions options) {
        Preferences preferences = NbPreferences.forModule(ClangdOptions.class);

        preferences.put(CLANGD_LOG_LEVEL_NAME, options.getLevel().name());
        preferences.put(CLANGD_LOG_FILE_NAME, options.getLogFile() == null ? "" : options.logFile.getAbsolutePath());
        preferences.put(CLANGD_EXECUTABLE, options.getClangdExecutable() == null ? "" : options.clangdExecutable.getAbsolutePath());

        return options;
    }

    private ClangdLogLevel level;
    private File logFile;
    private File clangdExecutable;

    public ClangdOptions() {
        this.level = ClangdLogLevel.VERBOSE;
        this.logFile = null;
        this.clangdExecutable = null;
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public ClangdOptions(ClangdOptions options) {
        this.level = options.level;
        this.logFile = options.logFile;
        this.clangdExecutable = options.clangdExecutable;
    }

    public List<String> getCommandLineOptions() {
        ArrayList<String> commandLineOptions = new ArrayList<>();
        switch (level) {
            case ERROR:
                commandLineOptions.add("--log=error");
                break;
            case INFO:
                commandLineOptions.add("--log=info");
                break;
            case VERBOSE:
                commandLineOptions.add("--log=verbose");
                break;
        }
        return commandLineOptions;
    }

    public ClangdLogLevel getLevel() {
        return level;
    }

    public void setLevel(ClangdLogLevel level) {
        this.level = level;
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    public File getClangdExecutable() {
        return clangdExecutable;
    }

    public void setClangdExecutable(File clangdExecutable) {
        this.clangdExecutable = clangdExecutable;
    }

    public boolean isMisconfigured() {
        return clangdExecutable == null
                || !clangdExecutable.isFile()
                || !clangdExecutable.exists()
                || !clangdExecutable.canExecute();
    }

}
