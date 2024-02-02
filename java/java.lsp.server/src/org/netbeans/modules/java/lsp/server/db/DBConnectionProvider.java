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
package org.netbeans.modules.java.lsp.server.db;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import static java.nio.file.attribute.AclEntryPermission.*;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import static java.nio.file.attribute.PosixFilePermission.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
@ServiceProvider(service = CommandProvider.class)
public class DBConnectionProvider implements CommandProvider {
    private static final Logger LOG = Logger.getLogger(DBConnectionProvider.class.getName());
    private static final String  GET_DB_CONNECTION = "nbls.db.connection"; //NOI18N

    private static final boolean POSIX = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");  // NOI18N
    private static final EnumSet<PosixFilePermission> readWritePosix = EnumSet.of(OWNER_READ, OWNER_WRITE);
    private static final EnumSet<AclEntryPermission> readOnlyAcl = EnumSet.of(READ_ACL, READ_ATTRIBUTES, WRITE_ATTRIBUTES, READ_DATA, READ_NAMED_ATTRS, DELETE, SYNCHRONIZE);

    // temporary directory location
    private static final Path tmpdir = Path.of(System.getProperty("java.io.tmpdir"));       // NOI18N

    public DBConnectionProvider() {
        try {
            deleteOldFiles(generateDirPath());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "deleteOldFiles", ex);
        }
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        Map<String, String> result = new HashMap<> ();
        CompletableFuture ret = new CompletableFuture();
        Properties dbProps = new Properties();
        DatabaseConnection conn = ConnectionManager.getDefault().getPreferredConnection(true);

        if (conn != null) {
            Path temp = null;
            Path dir = generateDirPath();

            try {
                if (!Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
                    Files.createDirectory(dir);
                }
                if (POSIX) {
                    FileAttribute<?> readWriteAttribs = PosixFilePermissions.asFileAttribute(readWritePosix);
//                  FileAttribute<?> readWriteAttribs = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));
                    temp = Files.createTempFile(dir, "db-", ".properties", readWriteAttribs);       // NOI18N
                } else {
                    temp = Files.createTempFile(dir, "db-", ".properties");                         // NOI18N
                    AclFileAttributeView acl = Files.getFileAttributeView(temp, AclFileAttributeView.class);
                    AclEntry ownerEntry = null;
                    for(AclEntry e : acl.getAcl()) {
                        if (e.principal().equals(acl.getOwner())) {
                            ownerEntry = e;
                            break;
                        }
                    }
                    if (ownerEntry != null) {
                        acl.setAcl(Collections.singletonList(ownerEntry));
                    } else {
                        deleteTempFile(temp);
                        ret.completeExceptionally(new IOException("Owner missing, file:"+temp.toString())); // NOI18N
                        return ret;
                    }
                }
            } catch (IOException ex) {
                deleteTempFile(temp);
                ret.completeExceptionally(ex);
                return ret;
            }

            try (Writer writer = new FileWriter(temp.toFile(), Charset.defaultCharset());) {
                dbProps.put("datasources.default.url", conn.getDatabaseURL()); //NOI18N
                dbProps.put("datasources.default.username", conn.getUser()); //NOI18N
                dbProps.put("datasources.default.password", conn.getPassword()); //NOI18N
                dbProps.put("datasources.default.driverClassName", conn.getDriverClass()); //NOI18N
                String ocid = (String) conn.getConnectionProperties().get("OCID"); //NOI18N
                if (ocid != null && !ocid.isEmpty()) {
                    dbProps.put("datasources.default.ocid", ocid); //NOI18N
                }
                dbProps.store(writer, "");
                if (POSIX) {
                    PosixFileAttributeView attribs = Files.getFileAttributeView(temp, PosixFileAttributeView.class);
                    attribs.setPermissions(EnumSet.of(OWNER_READ));
                } else {
                    DosFileAttributeView attribs = Files.getFileAttributeView(temp, DosFileAttributeView.class);
                    attribs.setReadOnly(true);
                    AclFileAttributeView acl = Files.getFileAttributeView(temp, AclFileAttributeView.class);
                    AclEntry ownerEntry = null;
                    if (acl.getAcl().size() != 1) {
                        deleteTempFile(temp);
                        ret.completeExceptionally(new IOException("Too many Acls, file:"+temp.toString()));     // NOI18N
                        return ret;
                    }
                    for(AclEntry e : acl.getAcl()) {
                        if (e.principal().equals(acl.getOwner())) {
                            ownerEntry = e;
                            break;
                        }
                    }
                    if (ownerEntry != null) {
                        AclEntry readOnly = AclEntry.newBuilder(ownerEntry).setPermissions(readOnlyAcl).build();
                        acl.setAcl(Collections.singletonList(readOnly));
                    } else {
                        deleteTempFile(temp);
                        ret.completeExceptionally(new IOException("Owner missing, file:"+temp.toString()));     // NOI18N
                        return ret;
                    }
                }
                temp.toFile().deleteOnExit();
                result.put("MICRONAUT_CONFIG_FILES", temp.toAbsolutePath().toString());     // NOI18N
            } catch (IOException ex) {
                deleteTempFile(temp);
                ret.completeExceptionally(ex);
                return ret;
            }
        }

        ret.complete(result);
        return ret;
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(GET_DB_CONNECTION);
    }

    private static Path generateDirPath() {
        String s = GET_DB_CONNECTION + "_" + System.getProperty("user.name");       // NOI18N
        Path name = tmpdir.getFileSystem().getPath(s);
        return tmpdir.resolve(name);
    }

    private static void deleteOldFiles(Path dir) throws IOException {
        if (Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> stream  = Files.newDirectoryStream(dir)) {
                for (Path f : stream) {
                    deleteTempFile(f);
                }
            }
        }
    }

    private static void deleteTempFile(Path temp) {
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
