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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.TestEntry;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Associated built module files with their owning project.
 * @author Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.project.FileOwnerQueryImplementation.class, position=50)
public final class UpdateTrackingFileOwnerQuery implements FileOwnerQueryImplementation {
    
    /** Default constructor for lookup. */
    public UpdateTrackingFileOwnerQuery() {}

    public Project getOwner(URI file) {
        if (!ModuleList.existKnownEntries()) {
            return null; // #65700
        }
        if (file.getScheme().equals("file")) { // NOI18N
            return getOwner(Utilities.toFile(file));
        } else {
            return null;
        }
    }

    public Project getOwner(FileObject file) {
        if (!ModuleList.existKnownEntries()) {
            return null; // #65700
        }
        File f = FileUtil.toFile(file);
        if (f != null) {
            return getOwner(f);
        } else {
            return null;
        }
    }
    
    private Project getOwner(File file) {
        for (ModuleEntry entry : ModuleList.getKnownEntries(file)) {
            File sourcedir = entry.getSourceLocation();
            if (sourcedir != null) {
                FileObject sourcedirFO = FileUtil.toFileObject(sourcedir);
                if (sourcedirFO != null) {
                    try {
                        Project p = ProjectManager.getDefault().findProject(sourcedirFO);
                        if (p != null) {
                            return p;
                        }
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }
        // may be tests.jar in tests distribution  
        TestEntry test = TestEntry.get(file);
        if (test != null) {
            Project p = test.getProject() ;
            if (p != null) {
                return p;
            }
        }
        return null;
    }
    
}
