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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

public class UnitTestForSourceQueryImpl implements MultipleRootsUnitTestForSourceQueryImplementation {

    private NbModuleProject project;

    public UnitTestForSourceQueryImpl(NbModuleProject project) {
        this.project = project;
    }

    public URL[] findUnitTests(FileObject source) {
        return find(source, "src.dir", "test.unit.src.dir"); // NOI18N
    }
    
    public URL[] findSources(FileObject unitTest) {
        return find(unitTest, "test.unit.src.dir", "src.dir"); // NOI18N
    }
    
    private URL[] find(FileObject file, String from, String to) {
        Project p = FileOwnerQuery.getOwner(file);
        if (p == null) {
            return null;
        }
        AntProjectHelper helper = project.getHelper();
        String val = project.evaluator().getProperty(from);
        assert val != null : "No value for " + from + " in " + project;
        FileObject fromRoot = helper.resolveFileObject(val);
        if (!file.equals(fromRoot)) {
            return null;
        }
        val = project.evaluator().getProperty(to);
        assert val != null : "No value for " + to + " in " + project;
        try {
            File f = helper.resolveFile(val);
            if (! f.exists()) {
                // #143633: need not to exist, ensure proper URI ending with a slash
                URI u = Utilities.toURI(f);
                String path = u.getPath();
                if (! path.endsWith("/"))
                    path = path.concat("/");
                try {
                    u = new URI(u.getScheme(), u.getHost(), path, u.getFragment());
                    return new URL[] {u.toURL()};
                } catch (URISyntaxException ex) {
                    Logger.getLogger(UnitTestForSourceQueryImpl.class.getName())
                            .log(Level.WARNING, "Problems getting URI for " + f, ex);
                }
            }

            return new URL[] {Utilities.toURI(f).toURL()};
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
}
