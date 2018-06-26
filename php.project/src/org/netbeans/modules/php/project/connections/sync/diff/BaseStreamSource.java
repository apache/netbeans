/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.connections.sync.diff;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Base class for stream source.
 */
abstract class BaseStreamSource extends StreamSource {

    protected static final Logger LOGGER = Logger.getLogger(BaseStreamSource.class.getName());

    private final String name;
    private final String mimeType;
    private final boolean remote;


    BaseStreamSource(String name, String mimeType, boolean remote) {
        this.name = name;
        this.mimeType = mimeType;
        this.remote = remote;
    }

    protected abstract Reader createReaderInternal() throws IOException;

    @Override
    public final String getName() {
        return name;
    }

    @NbBundle.Messages({
        "BaseStreamSource.title.local=Local Version",
        "BaseStreamSource.title.remote=Remote Version"
    })
    @Override
    public final String getTitle() {
        return remote ? Bundle.BaseStreamSource_title_remote() : Bundle.BaseStreamSource_title_local();
    }

    @Override
    public final String getMIMEType() {
        return mimeType;
    }

    @Override
    public final Reader createReader() throws IOException {
        if (!mimeType.startsWith("text/")) { // NOI18N
            LOGGER.log(Level.INFO, "No reader for non-text file; MIME type is {0}", mimeType);
            return null;
        }
        return createReaderInternal();
    }

    @Override
    public final Writer createWriter(Difference[] conflicts) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected static Reader createReader(FileObject fileObject) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(fileObject.getInputStream(), FileEncodingQuery.getEncoding(fileObject)));
    }

}
