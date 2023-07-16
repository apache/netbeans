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
import org.openide.util.Utilities;

/**
 * RustOptions implementation.
 */
public final class RustOptionsImpl {

    private static final String CARGO_LOCATION_KEY = "cargo-location"; // NOI18N
    private static final String CARGO_COMMAND = "cargo"; // NOI18N
    private static final String RUSTUP_LOCATION_KEY = "rustup-location"; // NOI18N
    private static final String RUSTUP_COMMAND = "rustup"; // NOI18N

    /**
     * Finds "rustup" (or "rustup.exe" on Windows) in any of the directories in
 the PATH environment variable.
     *
     * @return
     */
    private static File findInPath(String command) {
        String path = System.getenv("PATH"); // NOI18N
        path = path == null ? "" : path;
        String[] parts = path.split(File.pathSeparator);
        String extension = Utilities.isWindows() ? ".exe" : ""; // NOI18N
        for (String part : parts) {
            File file = new File(part, command + extension); // NOI18N
            if (file.canExecute()) {
                return file;
            }
        }
        return null;
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(RustOptionsImpl.class);
    }

    /**
     * Returns the full path to the "rustup" executable, or null if none exists.
     *
     * @param verifying true to verify that the path is indeed executable
     * @return The full path to the rustup executable, or null.
     */
    public static Path getCargoLocation(boolean verifying) {
        String cargo = getPreferences().get(CARGO_LOCATION_KEY, null);
        if (cargo == null) {
            File cargoExecutable = findInPath(CARGO_COMMAND);
            if (cargoExecutable != null) {
                cargo = cargoExecutable.getAbsolutePath();
                setCargoLocation(cargo);
                return Paths.get(cargo);
            }
        }
        // Check if rustup is valid, if not then reset from preferences
        if (cargo != null && verifying) {
            File cargoExecutable = new File(cargo);
            if (cargoExecutable.canExecute()) {
                return Paths.get(cargo);
            }
            // Reset from prefernces
            deleteCargoLocation();
            cargo = null;
        }
        // Warn the user if rustup cannot be found
        if (verifying) {
            showCargoNotFoundNotification();
        }
        return cargo == null ? null : Paths.get(cargo);
    }

    /**
     * Returns the full path to the "rustup" executable, or null if none exists.
     *
     * @param verifying true to verify that the path is indeed executable
     * @return The full path to the rustup executable, or null.
     */
    public static Path getRustupLocation(boolean verifying) {
        String rustup = getPreferences().get(RUSTUP_LOCATION_KEY, null);
        if (rustup == null) {
            File rustupLocation = findInPath(RUSTUP_COMMAND);
            if (rustupLocation != null) {
                rustup = rustupLocation.getAbsolutePath();
                setRustupLocation(rustup);
                return Paths.get(rustup);
            }
        }
        // Check if rustup is valid, if not then reset from preferences
        if (rustup != null && verifying) {
            File rustupExecutable = new File(rustup);
            if (rustupExecutable.canExecute()) {
                return Paths.get(rustup);
            }
            // Reset from prefernces
            deleteRustupLocation();
            rustup = null;
        }
        // Warn the user if rustup cannot be found
        if (verifying) {
            showRustupNotFoundNotification();
        }
        return rustup == null ? null : Paths.get(rustup);
    }

    /**
     * Removes the previously saved rustup location.
     */
    public static void deleteCargoLocation() {
        getPreferences().remove(CARGO_LOCATION_KEY);
    }

    /**
     * Removes the previously saved rustup location.
     */
    public static void deleteRustupLocation() {
        getPreferences().remove(RUSTUP_LOCATION_KEY);
    }

    /**
     * Sets a new cargo location. It is ignored if this is not a valid cargo
     * location.
     *
     * @param location The location (possibly an absolute path).
     */
    public static void setCargoLocation(String location) {
        if (location == null) {
            deleteCargoLocation();
        } else {
            File cargo = new File(location);
            if (cargo.canExecute()) {
                getPreferences().put(CARGO_LOCATION_KEY, cargo.getAbsolutePath());
            }
        }
    }

    /**
     * Sets a new rustup location. It is ignored if this is not a valid rustup
     * location.
     *
     * @param location The location (possibly an absolute path).
     */
    public static void setRustupLocation(String location) {
        if (location == null) {
            deleteRustupLocation();
        } else {
            File rustup = new File(location);
            if (rustup.canExecute()) {
                getPreferences().put(RUSTUP_LOCATION_KEY, rustup.getAbsolutePath());
            }
        }
    }

    /**
     * Opens the "Rust" options dialog, focused in the "Cargo" tab.
     */
    public static void shotRustOptions() {
        SwingUtilities.invokeLater(() -> {
            // Keep "Rust/Paths" in sync with RustOptionsPanelController path
            OptionsDisplayer.getDefault().open("Rust/Paths"); // NOI18N
        });
    }

    @NbBundle.Messages({
        "MISSING_CARGO_TITLE=Cargo command was not found.",
        "MISSING_CARGO_DETAILS=Cargo could not be found in your PATH. Please select the cargo executable location"
    })
    public static void showCargoNotFoundNotification() {
        NotificationDisplayer.Priority priority = NotificationDisplayer.Priority.HIGH;
        String title = NbBundle.getMessage(RustOptionsImpl.class, "MISSING_CARGO_TITLE"); // NOI18N
        String details = NbBundle.getMessage(RustOptionsImpl.class, "MISSING_CARGO_DETAILS"); // NOI18N
        ImageIcon icon = new ImageIcon(ImageUtilities.loadImage(RustProjectAPI.ICON));
        NotificationDisplayer.getDefault().notify(title, icon, details, (actionEvent) -> {
            shotRustOptions();
        });
    }

    @NbBundle.Messages({
        "MISSING_RUSTUP_TITLE=Rustup command was not found.",
        "MISSING_RUSTUP_DETAILS=Rustup could not be found in your PATH. Please select the rustup executable location"
    })
    public static void showRustupNotFoundNotification() {
        NotificationDisplayer.Priority priority = NotificationDisplayer.Priority.HIGH;
        String title = NbBundle.getMessage(RustOptionsImpl.class, "MISSING_RUSTUP_TITLE"); // NOI18N
        String details = NbBundle.getMessage(RustOptionsImpl.class, "MISSING_RUSTUP_DETAILS"); // NOI18N
        ImageIcon icon = new ImageIcon(ImageUtilities.loadImage(RustProjectAPI.ICON));
        NotificationDisplayer.getDefault().notify(title, icon, details, (actionEvent) -> {
            shotRustOptions();
        });
    }

}
