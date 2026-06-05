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

package org.netbeans.modules.rust.options.impl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * RustAnalyzerOptions implementation.
 */
public final class RustAnalyzerOptionsImpl {

    private static final String RUST_ANALYZER_LOCATION_KEY = "rust-analyzer-location"; // NOI18N

    private static Preferences getPreferences() {
        return NbPreferences.forModule(RustAnalyzerOptionsImpl.class);
    }

    /**
     * Returns the full path to the "rust-analyzer" executable, or null if none
     * exists.
     *
     * @param verify true to verify that the path is indeed executable
     * @param showNotificationIfVerifyFail true to generate notification if rust analyzer is not executeable
     * @return The full path to the rust-analyzer executable, or null.
     */
    public static Path getRustAnalyzerLocation(boolean verify, boolean showNotificationIfVerifyFail) {
        String rustAnalyzer = getPreferences().get(RUST_ANALYZER_LOCATION_KEY, null);
        // Check if rust-analyzer is valid, if not then reset from preferences
        if (rustAnalyzer != null && !rustAnalyzer.trim().isEmpty() && verify) {
            File rustAnalyzerExecutable = new File(rustAnalyzer);
            if (rustAnalyzerExecutable.canExecute()) {
                return Paths.get(rustAnalyzer);
            }
            // Reset from prefernces
            deleteRustAnalyzerLocation();
            rustAnalyzer = null;
            // Warn the user if rust-analyzer cannot be found
            if (showNotificationIfVerifyFail) {
                showRustAnalyzerNotFoundNotification();
            }
        }
        return rustAnalyzer == null ? null : Paths.get(rustAnalyzer);
    }

    /**
     * Removes the previously saved rust-analyzer location.
     */
    public static void deleteRustAnalyzerLocation() {
        getPreferences().remove(RUST_ANALYZER_LOCATION_KEY);
    }

    /**
     * Sets a new rust-analyzer location. It is ignored if this is not a valid
     * rust-analyzer location.
     *
     * @param location The location (possibly an absolute path).
     */
    public static void setRustAnalyzerLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            deleteRustAnalyzerLocation();
        } else {
            File rustAnalyzer = new File(location);
            if (rustAnalyzer.canExecute()) {
                getPreferences().put(RUST_ANALYZER_LOCATION_KEY, rustAnalyzer.getAbsolutePath());
            }
        }
    }

    /**
     * Opens the "Rust" options dialog, focused in the "rust-analyzer" tab.
     */
    public static void showRustAnalyzerOptions() {
        SwingUtilities.invokeLater(() -> {
            OptionsDisplayer.getDefault().open("Rust/RustAnalyser"); // NOI18N
        });
    }

    @NbBundle.Messages({
        "MISSING_RUSTANALYZER_TITLE=rust-analyzer command was not found.",
        "MISSING_RUSTANALYZER_DETAILS=rust-analyzer could not be found in your PATH. Please select the rust-analyzer executable location"
    })
    public static void showRustAnalyzerNotFoundNotification() {
        NotificationDisplayer.Priority priority = NotificationDisplayer.Priority.HIGH;
        String title = Bundle.MISSING_RUSTANALYZER_TITLE();
        String details = Bundle.MISSING_RUSTANALYZER_DETAILS();
        ImageIcon icon = new ImageIcon(ImageUtilities.loadImage(RustProjectAPI.ICON));
        NotificationDisplayer.getDefault().notify(
                title,
                icon,
                details,
                actionEvent -> showRustAnalyzerOptions(),
                priority
        );
    }

}
