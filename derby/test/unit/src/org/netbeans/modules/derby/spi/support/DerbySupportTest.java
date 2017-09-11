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

package org.netbeans.modules.derby.spi.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.derby.DerbyOptions;

/**
 *
 * @author Andrei Badea
 */
public class DerbySupportTest extends NbTestCase {
    public DerbySupportTest(String testName) {
        super(testName);
    }
    
    public void testDefaultSystemHomeWhenNDSHPropertySetIssue76908() {
        // ensure "foo" ist not the "default" derby home
        assert (! DerbySupport.getDefaultSystemHome().equals("foo"));

        // ... but returning it when it is
        System.setProperty(DerbyOptions.NETBEANS_DERBY_SYSTEM_HOME, "foo");
        assertEquals("foo", DerbySupport.getDefaultSystemHome());
    }
    
    public void testGetSystemHome() throws IOException {
        System.clearProperty(DerbyOptions.NETBEANS_DERBY_SYSTEM_HOME);
        clearWorkDir();

        String origUserHome = System.getProperty("user.home");
        String origOsName = System.getProperty("os.name");
        
        System.setProperty("org.netbeans.modules.derby.spi.support.DerbySupport.overrideAppData", getWorkDirPath());
        
        System.setProperty("user.home", getWorkDirPath());
        
        // This test is only partitially correct (the tested method reads
        // environment variables, so this is a partitial solution) - the idea:
        // On non-windows systems default system home is user.home/.netbeans-derby
        // On windows system default system home has "Derby" as final path part
        
        System.setProperty("os.name", "Linux");
        assertEquals(new File(getWorkDirPath(), ".netbeans-derby").getAbsolutePath(), DerbySupport.getDefaultSystemHome());

        System.setProperty("os.name", "Windows 8");
        assertEquals("Derby", new File(DerbySupport.getDefaultSystemHome()).getName());
        
        Files.createDirectory(new File(getWorkDirPath(), ".netbeans-derby").toPath());
        assertEquals(new File(getWorkDirPath(), ".netbeans-derby").getAbsolutePath(), DerbySupport.getDefaultSystemHome());
        
        System.clearProperty("org.netbeans.modules.derby.spi.support.DerbySupport.overrideAppData");
        System.setProperty("user.home", origUserHome);
        System.setProperty("os.name", origOsName);
    }
}
