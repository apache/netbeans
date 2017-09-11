/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seplatform.platformdefinition.jrtfs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.spi.ArchiveRootProvider;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=ArchiveRootProvider.class, position = 1000)
public class NBJRTArchiveRootProvider implements ArchiveRootProvider {

    private static final Logger LOG = Logger.getLogger(NBJRTArchiveRootProvider.class.getName());

    @Override
    public boolean isArchiveFile(@NonNull final URL url, final boolean strict) {
        final FileObject fo = URLMapper.findFileObject(url);
        return fo == null ? false : isArchiveFile(fo, strict);
    }

    @Override
    public boolean isArchiveFile(FileObject fo, boolean strict) {
        if (!fo.isFolder()) {
            return false;
        }
        final File file = FileUtil.toFile(fo);
        return file == null ? false : NBJRTUtil.getNIOProvider(file) != null;
    }

    @Override
    public boolean isArchiveArtifact(@NonNull final URL url) {
        return NBJRTUtil.PROTOCOL.equals(url.getProtocol());
    }

    @Override
    @CheckForNull
    public URL getArchiveFile(@NonNull final URL url) {
        final Pair<URL,String> p = NBJRTUtil.parseURL(url);
        return p != null ? p.first() : null;
    }

    @Override
    @NonNull
    public URL getArchiveRoot(@NonNull final URL url) {
        try {
            return new URL(String.format("%s:%s!/", //NOI18N
                    NBJRTUtil.PROTOCOL,
                    url));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
