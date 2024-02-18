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
package org.netbeans.modules.rust.cargo.impl;

import java.io.File;
import java.nio.file.Paths;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Antonio Vieiro <antonio@vieiro.net>
 */
public class CargoTOMLParserTest extends NbTestCase {

    public CargoTOMLParserTest(String name) {
        super(name);
    }

    @Test
    public void testShouldParseVirtualWorkspaceCorrectly() throws Exception {
        System.out.println("testShouldParseVirtualWorkspaceCorrectly");

        // Given the "arrow-rs" Cargo.toml file
        String test_file = "arrow-rs-virtual-workspace-Cargo.toml";
        File file = Paths.get(getDataDir().getAbsolutePath(), "cargo-examples", test_file).toFile();
        FileObject fo = FileUtil.toFileObject(file);
        assertNotNull("Cannot find fileobject for test file " + test_file, fo);

        // When we create (parsing) a CargoTOML object...
        CargoTOML cargo = CargoTOML.fromFileObject(fo);

        // Then it must be detected as a virtual workspace
        assertTrue(cargo.getKind() == CargoTOML.CargoTOMLKind.VIRTUAL_WORKSPACE);
    }

}
