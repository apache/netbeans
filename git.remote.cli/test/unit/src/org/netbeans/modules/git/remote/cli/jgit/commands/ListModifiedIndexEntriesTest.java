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
package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class ListModifiedIndexEntriesTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;
    
    public ListModifiedIndexEntriesTest(String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList().contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testSingleModification () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(f);
        add();
        commit();

        write(f, "modification");

        VCSFileProxy[] modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals(0, modifications.length);
        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(0, modifications.length);

        add(f);
        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals(1, modifications.length);
        assertTrue(Arrays.equals(new VCSFileProxy[] { f }, modifications));
        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(1, modifications.length);
        assertTrue(Arrays.equals(new VCSFileProxy[] { f }, modifications));
    }

    public void testMultipleModification () throws Exception {
        VCSFileProxy f1 = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(f1);
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "file2");
        VCSFileProxySupport.createNew(f2);
        add();
        commit();

        write(f1, "modification");
        write(f2, "modification 2");

        VCSFileProxy[] modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { f1, f2 }, NULL_PROGRESS_MONITOR);
        assertEquals(0, modifications.length);
        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(0, modifications.length);

        add(f1, f2);
        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { f1 }, NULL_PROGRESS_MONITOR);
        assertEquals(1, modifications.length);
        assertTrue(Arrays.equals(new VCSFileProxy[] { f1 }, modifications));

        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { f2 }, NULL_PROGRESS_MONITOR);
        assertEquals(1, modifications.length);
        assertTrue(Arrays.equals(new VCSFileProxy[] { f2 }, modifications));

        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, modifications.length);
        assertEquals(new HashSet<VCSFileProxy>(Arrays.asList(new VCSFileProxy[] { f1, f2 })), new HashSet<VCSFileProxy>(Arrays.asList(modifications)));
    }
}
