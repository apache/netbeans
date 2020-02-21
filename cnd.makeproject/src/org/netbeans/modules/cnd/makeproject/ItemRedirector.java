/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
