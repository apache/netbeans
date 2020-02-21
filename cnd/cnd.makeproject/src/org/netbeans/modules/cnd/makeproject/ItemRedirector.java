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
package org.netbeans.modules.cnd.makeproject;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.spi.utils.FileObjectRedirector;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=FileObjectRedirector.class,position=1000)
public class ItemRedirector implements FileObjectRedirector {

    /* implements FileObjectRedirector */
    
    /**
     * Look for a replacement FO. The main reason we might want to replace the original FO
     * would be to go to original tuxedo interface header.
     */
    @Override
    public FileObject redirect(FileObject fo) {
        if (!isEnabled(fo)) {
            return null;
        }
        ExecutionEnvironment env;
        try {
            env = FileSystemProvider.getExecutionEnvironment(fo.getFileSystem());
        } catch (FileStateInvalidException ex) {
            return null;
        }
        if (env.isLocal()) {
            return resolveSymbolicLink(fo);
        } else {
            try {
                if (FileSystemProvider.isLink(fo)) {
                   String path = FileSystemProvider.resolveLink(fo);
                   if (path != null) {
                       return fo.getFileSystem().findResource(path);
                   }
                }
                return null;
            } catch (Exception ex) {
                CndUtils.printStackTraceOnce(ex);
                return null;
            }
        }
    }
    
    
    public static FileObject resolveSymbolicLink(final FileObject fo) {
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<FileObject>) () -> {
                URI uri = fo.toURI();
                Path path = Paths.get(uri);
                for (int i = 0; i < 5; i++) {
                    if (Files.isSymbolicLink(path)) {
                        Path to = Files.readSymbolicLink(path);
                        if (!to.isAbsolute()) {
                            to = path.getParent().resolve(to).normalize();
                        }
                        if (Files.isRegularFile(to)) {
                            return FileUtil.toFileObject(to.toFile());
                        }
                        path = to;
                    } else {
                        return null;
                    }
                }
                return null;
            });
        } catch (Exception ex) {
            CndUtils.printStackTraceOnce(ex);
            return null;
        }
    }

    private boolean isEnabled(FileObject fo) {
        Project owner = FileOwnerQuery.getOwner(fo);
        if (owner != null) {
            ConfigurationDescriptorProvider cdp = owner.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (cdp != null && cdp.gotDescriptor()) {
                MakeConfigurationDescriptor cd = cdp.getConfigurationDescriptor();
                if (cd != null) {
                    MakeConfiguration activeConfiguration = cd.getActiveConfiguration();
                    if (activeConfiguration != null) {
                        return activeConfiguration.getCodeAssistanceConfiguration().getResolveSymbolicLinks().getValue();
                    }
                }
            }
        }
        return false;
    }
}
