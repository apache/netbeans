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
package org.netbeans.modules.php.nette2.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class FileUtils {
    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());
    private static final Set<PosixFilePermission> PERMISSIONS_777 = EnumSet.allOf(PosixFilePermission.class);

    private FileUtils() {
    }

    public static FileObject getFile(JTextComponent textComponent) {
        assert textComponent != null;
        return NbEditorUtilities.getFileObject(textComponent.getDocument());
    }

    public static void chmod777Recursively(FileObject fileObject) {
        assert fileObject != null;
        File file = FileUtil.toFile(fileObject);
        if (file != null) {
            chmod777(file);
            Enumeration<? extends FileObject> allNestedChildren = fileObject.getChildren(true);
            while (allNestedChildren.hasMoreElements()) {
                File child = FileUtil.toFile(allNestedChildren.nextElement());
                if (child != null) {
                    chmod777(child);
                }
            }
        }
    }

    private static void chmod777(File file) {
        assert file != null;
        try {
            Files.setPosixFilePermissions(file.toPath(), PERMISSIONS_777);
        } catch (UnsupportedOperationException | IOException ex) {
            // UnsupportedOperationException - probably on Windows
            LOGGER.log(Level.FINE, null, ex);
        }
    }

    public static void copyDirectory(File sourceDirectory, File destinationDirectory) {
        assert sourceDirectory != null;
        assert destinationDirectory != null;
        try {
            Files.walkFileTree(sourceDirectory.toPath(), new CopyDirectoryVisitor(sourceDirectory.toPath(), destinationDirectory.toPath()));
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
    }

    private static final class CopyDirectoryVisitor extends SimpleFileVisitor<Path> {
        private final Path source;
        private final Path destination;

        CopyDirectoryVisitor(Path source, Path destination) {
            this.source = source;
            this.destination = destination;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) throws IOException {
            Path targetPath = destination.resolve(source.relativize(directory));
            if(!Files.exists(targetPath)){
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
            Files.copy(file, destination.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }
    }

}
