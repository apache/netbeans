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
package org.netbeans.modules.cloud.oracle.assets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import static java.nio.file.attribute.AclEntryPermission.APPEND_DATA;
import static java.nio.file.attribute.AclEntryPermission.READ_ACL;
import static java.nio.file.attribute.AclEntryPermission.READ_ATTRIBUTES;
import static java.nio.file.attribute.AclEntryPermission.READ_DATA;
import static java.nio.file.attribute.AclEntryPermission.READ_NAMED_ATTRS;
import static java.nio.file.attribute.AclEntryPermission.SYNCHRONIZE;
import static java.nio.file.attribute.AclEntryPermission.WRITE_ACL;
import static java.nio.file.attribute.AclEntryPermission.WRITE_ATTRIBUTES;
import static java.nio.file.attribute.AclEntryPermission.WRITE_DATA;
import static java.nio.file.attribute.AclEntryPermission.WRITE_NAMED_ATTRS;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.Places;

/**
 *
 * @author Dusan Petrovic
 */
public class TempFileGenerator {
    
    private static final Logger LOG = Logger.getLogger(TempFileGenerator.class.getName());

    private static final boolean POSIX = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");  // NOI18N
    private static final EnumSet<PosixFilePermission> readWritePosix = EnumSet.of(OWNER_READ, OWNER_WRITE);
    private static final EnumSet<PosixFilePermission> readPosix = EnumSet.of(OWNER_READ);

    private static final EnumSet<AclEntryPermission> readOnlyAcl = EnumSet.of(
                READ_ACL,
                READ_ATTRIBUTES,
                READ_DATA,
                READ_NAMED_ATTRS,
                SYNCHRONIZE
        );

    private static final EnumSet<AclEntryPermission> readWriteAcl = EnumSet.of(
                READ_DATA,
                WRITE_DATA,
                APPEND_DATA,
                READ_NAMED_ATTRS,
                WRITE_NAMED_ATTRS,
                READ_ATTRIBUTES,
                WRITE_ATTRIBUTES,
                READ_ACL,
                WRITE_ACL,
                SYNCHRONIZE
        );
    
    private final boolean readOnly;
    private final String filePrefix;
    private final String fileSufix;
    private final String configPath;

    public TempFileGenerator(String filePrefix, String fileSufix, String configPath, boolean readOnly) {
        this.readOnly = readOnly;
        this.configPath = configPath;
        this.filePrefix = filePrefix;
        this.fileSufix = fileSufix;
    }
    
    public Path writePropertiesFile(Properties props) throws IOException {
        return writeToNewTempFile(w -> props.store(w, ""));
    }
    
    public Path writeTextFile(String text) throws IOException {
        return writeToNewTempFile(w -> w.write(text));
    }
    
    private Path writeToNewTempFile(WriterConsumer c) throws IOException {
        Path temp = null;
        try {
            temp = generateConfigFile();
            writeToFile(c, temp);
        } catch (IOException ex) {
            deleteTempFile(temp);
            throw ex;
        }
        
        return temp;
    }
    
    @FunctionalInterface
    private static interface WriterConsumer {

        public void accept(Writer t) throws IOException;
        
    }
    
    private Path generateConfigFile() throws IOException {
        Path dir = generateDirPath();
        
        if (!Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectory(dir);
        }
        if (POSIX) {
            return createFilePosix(dir);       
        } 
        
        Path temp = Files.createTempFile(dir, this.filePrefix, this.fileSufix);
        setFileOwnerAcl(temp, readWriteAcl);
        return temp;
    }
    
    private void writeToFile(WriterConsumer c, Path filePath) throws IOException {
        try (Writer writer = new FileWriter(filePath.toFile(), Charset.defaultCharset());) {
            c.accept(writer);
            if (POSIX) {
                setFilePermissionPosix(filePath);
            } else {
                if (readOnly) {
                    DosFileAttributeView attribs = Files.getFileAttributeView(filePath, DosFileAttributeView.class);
                    attribs.setReadOnly(true);
                    setFileOwnerAcl(filePath, readOnlyAcl);
                } else {
                    setFileOwnerAcl(filePath, readWriteAcl);
                }
            }
            filePath.toFile().deleteOnExit();
        }
    }
    
    private void setFileOwnerAcl(Path filePath, Set<AclEntryPermission> permissions) throws IOException {
        AclFileAttributeView acl = Files.getFileAttributeView(filePath, AclFileAttributeView.class);
        AclEntry ownerEntry = findFileOwner(acl);
        AclEntry.Builder aclBuilder;
        
        if (ownerEntry == null) {
            LOG.info("Owner missing, file:" + filePath.toString()); // NOI18N
            aclBuilder = AclEntry.newBuilder().setPrincipal(acl.getOwner()).setType(AclEntryType.ALLOW);
        } else {
            aclBuilder = AclEntry.newBuilder(ownerEntry);
        }

        AclEntry ownerAcl = aclBuilder.setPermissions(permissions).build();
        acl.setAcl(Collections.singletonList(ownerAcl));
    }

    private AclEntry findFileOwner(AclFileAttributeView acl) throws IOException {
        for(AclEntry e : acl.getAcl()) {
            if (e.principal().equals(acl.getOwner())) {
                return e;
            }
        }
        return null;
    }
    
    private void setFilePermissionPosix(Path temp) throws IOException {
        PosixFileAttributeView attributes = Files.getFileAttributeView(temp, PosixFileAttributeView.class);
        if (this.readOnly) {
            attributes.setPermissions(readPosix);
        } else {
            attributes.setPermissions(readWritePosix);
        }
    }
    
    
    private Path createFilePosix(Path dir) throws IOException {
        FileAttribute<?> readWriteAttribs = PosixFilePermissions.asFileAttribute(readWritePosix);
        return Files.createTempFile(dir,this.filePrefix, this.fileSufix, readWriteAttribs);
    }
    
    private Path generateDirPath() {
        File file = Places.getCacheSubdirectory(this.configPath);
        file.deleteOnExit();
        return file.toPath();
    }
    
    private void deleteTempFile(Path temp) {
        if (temp != null && Files.isRegularFile(temp, LinkOption.NOFOLLOW_LINKS)) {
            try {
                if (POSIX) {
                    PosixFileAttributeView attribs = Files.getFileAttributeView(temp, PosixFileAttributeView.class);
                    attribs.setPermissions(readWritePosix);
                } else {
                    DosFileAttributeView attribs = Files.getFileAttributeView(temp, DosFileAttributeView.class);
                    attribs.setReadOnly(false);
                }
                Files.delete(temp);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "deleteTempFile", ex);
            }
        }
    }
}
