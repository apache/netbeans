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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Manifest;

/*
 * The OSGi metadata of the net.java.html.boot require org.objectweb.asm; version="[5.0.0,6.0.0),
 * even though the code has shaded-in version of ASM. And this ancient version of ASM is not
 * and will not be present in NetBeans. Strip this completely unnecessary dependency from the
 * module:
 */
public class WorkaroundBogusManifest {
    private static final String IMPORT_PACKAGE = "Import-Package";
    private static final String TO_REMOVE = ",org.objectweb.asm;version=\"[5.0,6)\",org.objectweb.asm.signature;version=\"[5.0,6)\"";
    public static void main(String... args) throws IOException {
        Path jarPath = Paths.get(args[0]);
        ClassLoader cl = WorkaroundBogusManifest.class.getClassLoader();
        try (FileSystem fs = FileSystems.newFileSystem(jarPath, cl)) {
            Path manifestFile = fs.getPath("META-INF", "MANIFEST.MF");
            Manifest manifest;
            try (InputStream is = Files.newInputStream(manifestFile)) {
                manifest = new Manifest(is);
            }
            String importPackage = manifest.getMainAttributes().getValue(IMPORT_PACKAGE);
            String newImportPackage = importPackage.replace(TO_REMOVE, "");

            if (!newImportPackage.equals(importPackage)) {
                System.out.println("writing back modified import packages");
                System.out.println("original importPackages: " + importPackage);
                System.out.println("new      importPackages: " + newImportPackage);

                manifest.getMainAttributes().putValue(IMPORT_PACKAGE, newImportPackage);

                try (OutputStream os = Files.newOutputStream(manifestFile)) {
                    manifest.write(os);
                }
            } else {
                System.out.println("manifest unchanged");
            }
        }
    }
}