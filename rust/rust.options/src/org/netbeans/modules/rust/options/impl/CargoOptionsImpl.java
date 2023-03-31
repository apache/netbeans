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
 * CargoOptions implementation.
 */
public final class CargoOptionsImpl {

    private static final String CARGO_LOCATION_KEY = "cargo-location"; // NOI18N

    /**
     * Finds "cargo" (or "cargo.exe" on Windows) in any of the directories in
     * the PATH environment variable.
     *
     * @return
     */
    private static File findCargoInPath() {
        String path = System.getenv("PATH"); // NOI18N
        path = path == null ? "" : path;
        String[] parts = path.split(File.pathSeparator);
        String extension = Utilities.isWindows() ? ".exe" : ""; // NOI18N
        for (String part : parts) {
            File file = new File(part, "cargo" + extension); // NOI18N
            if (file.canExecute()) {
                return file;
            }
        }
        return null;
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(CargoOptionsImpl.class);
    }

    /**
     * Returns the full path to the "cargo" executable, or null if none exists.
     * If no preferences are present then we try "$HOME/.cargo/bin/cargo[.exe]",
     * and set the property.
     *
     * @param verifying true to verify that the path is indeed executable
     * @return The full path to the cargo executable, or null.
     */
    public static Path getCargoLocation(boolean verifying) {
        String cargo = getPreferences().get(CARGO_LOCATION_KEY, null);
        if (cargo == null) {
            File cargoExecutable = findCargoInPath();
            if (cargoExecutable != null) {
                cargo = cargoExecutable.getAbsolutePath();
                setCargoLocation(cargo);
                return Paths.get(cargo);
            }
        }
        // Check if cargo is valid, if not then reset from preferences
        if (cargo != null && verifying) {
            File cargoExecutable = new File(cargo);
            if (cargoExecutable.canExecute()) {
                return Paths.get(cargo);
            }
            // Reset from prefernces
            deleteCargoLocation();
            cargo = null;
        }
        // Warn the user if cargo cannot be found
        if (verifying) {
            showCargoNotFoundNotification();
        }
        return cargo == null ? null : Paths.get(cargo);
    }

    /**
     * Removes the previously saved cargo location.
     */
    public static void deleteCargoLocation() {
        getPreferences().remove(CARGO_LOCATION_KEY);
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
     * Opens the "Rust" options dialog, focused in the "Cargo" tab.
     */
    public static void showRustCargoOptions() {
        SwingUtilities.invokeLater(() -> {
            OptionsDisplayer.getDefault().open("Rust/Cargo"); // NOI18N
        });
    }

    @NbBundle.Messages({
        "MISSING_CARGO_TITLE=Cargo command was not found.",
        "MISSING_CARGO_DETAILS=Cargo could not be found in your PATH. Please select the cargo executable location"
    })
    public static void showCargoNotFoundNotification() {
        NotificationDisplayer.Priority priority = NotificationDisplayer.Priority.HIGH;
        String title = NbBundle.getMessage(CargoOptionsImpl.class, "MISSING_CARGO_TITLE"); // NOI18N
        String details = NbBundle.getMessage(CargoOptionsImpl.class, "MISSING_CARGO_DETAILS"); // NOI18N
        ImageIcon icon = new ImageIcon(ImageUtilities.loadImage(RustProjectAPI.ICON));
        NotificationDisplayer.getDefault().notify(title, icon, details, (actionEvent) -> {
            showRustCargoOptions();
        });
    }

}
