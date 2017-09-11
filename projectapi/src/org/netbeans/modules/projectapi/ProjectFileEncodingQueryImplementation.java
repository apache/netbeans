/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.projectapi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * @author Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.FileEncodingQueryImplementation.class, position=200)
public class ProjectFileEncodingQueryImplementation extends FileEncodingQueryImplementation {

    private static final Logger LOG = Logger.getLogger(ProjectFileEncodingQueryImplementation.class.getName());

    public ProjectFileEncodingQueryImplementation() {}

    public Charset getEncoding(FileObject file) {
        if (isSystemFS(file)) {
            LOG.log(Level.FINER, "{0}: on system filesystem", file);    //NOI18N
            return null;
        }
        Project p = FileOwnerQuery.getOwner(file);
        if (p == null) {
            LOG.log(Level.FINER, "{0}: no owner", file);
            return null;
        }
        FileEncodingQueryImplementation delegate = p.getLookup().lookup(FileEncodingQueryImplementation.class);
        if (delegate == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0}: no FEQI in {1}", new Object[] {file, p});
            }
            return null;
        }
        Charset encoding = delegate.getEncoding(file);
        if (LOG.isLoggable(Level.FINE)) {
           LOG.log(Level.FINE, "{0}: got {1} from {2}", new Object[] {file, encoding, delegate});
        }
        return encoding;
    }

    private static boolean isSystemFS(@NonNull final FileObject file) {
        try {
            return file.getFileSystem().isDefault();
        } catch (IOException ioe) {
            return false;
        }
    }

}
