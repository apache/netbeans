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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTestCase;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.subversion.Subversion;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class ParsedStatusTestHidden extends AbstractCommandTestCase {
    
    // XXX terst remote change
    
    private enum StatusCall {
        filearray,
        file
    }
    
    public ParsedStatusTestHidden(String testName) throws Exception {
        super(testName);
    }

    // XXX check with javahl
    public void testGetStatusWrongAmount() throws Exception {                                
        File folder = createFolder("folder");        
        File folder1 = createFolder(folder, "folder1");        
        File folder2 = createFolder(folder, "folder2");        
        File file1 = createFolder(folder2, "file1");        
        
        add(folder);
        add(folder1);
        add(folder2);
        add(file1);
        commit(getWC());
                
        ISVNStatus[] s1 = getNbClient().getStatus(folder, true, false);

        // returns crap (4 entries) only for the cli which was intentionaly implemted that way
        // to stay compatible with svnClientAdapter's commandline client.
        // javahl and svnkit should return a zero length array
        // commandline no lomger parses metadata, returns the same as javahl and svnkit
        assertEquals(0, s1.length);
    }
    
    
}
