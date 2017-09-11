/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.masterfs.VersioningAnnotationProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Utilities;

/**
 *
 * @author ondra
 */
public class VersioningQueryTest extends AbstractGitTestCase {


    public VersioningQueryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {
            VersioningAnnotationProvider.class,
            GitVCS.class});
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIsManaged () throws Exception {
        // unversioned file
        File file = new File(getWorkDir(), "unversionedfile");
        file.createNewFile();

        boolean versioned = VersioningQuery.isManaged(Utilities.toURI(file));
        assertFalse(versioned);

        commit();

        // metadata folder
        file = new File(repositoryLocation, ".git");

        versioned = VersioningQuery.isManaged(Utilities.toURI(file));
        assertTrue(versioned);

        // metadata file
        file = new File(new File(repositoryLocation, ".git"), "index");

        versioned = VersioningQuery.isManaged(Utilities.toURI(file));
        assertTrue(versioned);

        // versioned file
        file = new File(repositoryLocation, "attrfile");
        file.createNewFile();

        versioned = VersioningQuery.isManaged(Utilities.toURI(file));
        assertTrue(versioned);
    }

}
