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

import java.nio.file.Path;
import java.util.Objects;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Tests the RustOptionsImpl.
 */
public class CargoOptionsImplTest {

    @Test
    public void testShouldNullPathDeleteCargoLocation() {
        System.out.println("testShouldNullPathDeleteCargoLocation");
        RustOptionsImpl.setCargoLocation(null);
    }

    @Test
    public void testShouldAutomaticallySetPathWhenAlreadyInstalled() {
        System.out.println("testShouldAutomaticallySetPathWhenAlreadyInstalled");

        // Given that we're deleting the cargo path from preferences.
        RustOptionsImpl.deleteCargoLocation();

        // When we get it again, probably from the default $HOME/.cargo/bin/cargo path
        Path cargo = RustOptionsImpl.getCargoLocation(true);

        // Then if cargo is not null
        if (cargo != null) {
            // It must exist and be executable
            assertTrue(cargo.toFile().canExecute());
        }
    }

    @Test
    public void testShouldFindCargoConsistently() {
        System.out.println("testShouldFindCargoConsistently");

        // Given a cargo found with verification
        Path cargo = RustOptionsImpl.getCargoLocation(true);

        // When we get cargo without verification
        Path cargo2 = RustOptionsImpl.getCargoLocation(false);

        // Then these must be equal
        assertTrue(Objects.equals(cargo, cargo2));
    }

}
