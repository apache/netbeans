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
package org.netbeans.modules.rust.options.api;

import java.nio.file.Path;
import org.netbeans.modules.rust.options.impl.RustOptionsImpl;

/**
 * Returns the options for Rust and Cargo.
 */
public final class RustOptions {

    /**
     * Returns the Path where cargo is installed, or null.
     *
     * @param verifying If true then the path is checked for validity (the path
     * exists and is executable) and if it is incorrect then a notification is
     * shown to the user.
     * @return The Path where cargo is installed, or null.
     */
    public static final Path getCargoLocation(boolean verifying) {
        return RustOptionsImpl.getCargoLocation(verifying);
    }

    /**
     * Returns the Path where rustup is installed, or null.
     * @param verifying If true then the path is checked for validity (the path
     * exists and is executable) and if it is incorrect then a notification is
     * shown to the user.
     * @return The path where rustup is installed, or null.
     */
    public static final Path getRustupLocation(boolean verifying) {
        return RustOptionsImpl.getRustupLocation(verifying);
    }

    /**
     * Opens the Cargo options panel.
     */
    public static void showRustCargoOptions() {
        RustOptionsImpl.shotRustOptions();
    }

}
