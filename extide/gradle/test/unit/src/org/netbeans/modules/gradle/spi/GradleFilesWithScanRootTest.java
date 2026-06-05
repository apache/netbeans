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
package org.netbeans.modules.gradle.spi;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.junit.After;
import org.junit.Before;

public class GradleFilesWithScanRootTest extends GradleFilesTest {

    private File scanRoot;
    private File forbiddenTestsDir;

    @Before
    public void setup() {
        scanRoot = root.getRoot().getParentFile();
        forbiddenTestsDir = new File(scanRoot, "forbiddenTests");
        File fd1 = new File(forbiddenTestsDir, "forbidden1");
        File fd2 = new File(new File(forbiddenTestsDir, "f2"), "forbidden2");
        System.setProperty("project.limitScanRoot", scanRoot.getAbsolutePath());
        System.setProperty("project.forbiddenFolders", fd1.getAbsolutePath() + ";" + fd2.getAbsolutePath());
        fd1.mkdirs();
        fd2.mkdirs();
    }

    @After
    public void cleanup() {
        try {
            Files.walkFileTree(forbiddenTestsDir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    return file.toFile().delete() ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return dir.toFile().delete() ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
