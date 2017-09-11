/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.source.parsing;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 *
 * @author Tomas Zezula
 */
public abstract class AbstractPathArchive implements Archive {
    private static final Logger LOG = Logger.getLogger(AbstractPathArchive.class.getName());
    protected final Path root;
    protected final String rootURI;
    protected final char separator;
    private volatile Boolean multiRelease;

    protected AbstractPathArchive(
            @NonNull final Path root,
            @NullAllowed final URI rootURI) {
        assert root != null;
        this.root = root;
        this.rootURI = rootURI == null ? null : rootURI.toString();
        final String separator = root.getFileSystem().getSeparator();
        if (separator.length() != 1) {
            throw new IllegalArgumentException("Multi character separators are unsupported");
        }
        this.separator = separator.charAt(0);
    }

    @Override
    public void clear() {
        multiRelease = null;
    }

    @Override
    public boolean isMultiRelease() {
        Boolean res = multiRelease;
        if (res == null) {
            res = Boolean.FALSE;
            if (FileObjects.JAR.equals(root.getFileSystem().provider().getScheme())) {
                try {
                    final JavaFileObject jfo = getFile("META-INF/MANIFEST.MF"); //NOI18N
                    if (jfo != null) {
                        try(final InputStream in = new BufferedInputStream(jfo.openInputStream())) {
                            res = FileObjects.isMultiVersionArchive(in);
                        }
                    }
                } catch (IOException ioe) {
                    LOG.log(
                            Level.WARNING,
                            "Cannot read: {0} manifest",    //NOI18N
                            rootURI.toString());
                }
            }
            multiRelease = res;
        }
        return res;
    }
}
