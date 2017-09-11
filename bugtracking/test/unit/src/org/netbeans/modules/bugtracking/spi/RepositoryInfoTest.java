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

package org.netbeans.modules.bugtracking.spi;

import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NbPreferences;

/**
 *
 * @author tomas
 */
public class RepositoryInfoTest extends NbTestCase {
    

    public RepositoryInfoTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {    
    }

    @Override
    protected void tearDown() throws Exception {   
    }

    public void testCreate() {
        RepositoryInfo info = 
            new RepositoryInfo(
                "id", 
                "cid", 
                "http://url", 
                "displayName", 
                "tooltip", 
                "user", 
                "httpUser", 
                "password".toCharArray(), 
                "httpPassword".toCharArray());
        
        assertEquals("id", info.getID());
        assertEquals("cid", info.getConnectorId()); 
        assertEquals("http://url", info.getUrl());
        assertEquals("displayName", info.getDisplayName());
        assertEquals("tooltip", info.getTooltip());
        assertEquals("user", info.getUsername());
        assertEquals("httpUser", info.getHttpUsername());
        assertEquals("password", new String(info.getPassword()));
        assertEquals("httpPassword", new String(info.getHttpPassword()));
    }
    
    public void testValues() {
        RepositoryInfo info = 
            new RepositoryInfo(
                "id", 
                "cid", 
                "http://url", 
                "displayName", 
                "tooltip", 
                "user", 
                "httpUser", 
                "password".toCharArray(), 
                "httpPassword".toCharArray());
        
        info.putValue("key1", "value1");
        info.putValue("key2", "value2");
        assertEquals("value1", info.getValue("key1"));
        assertEquals("value2", info.getValue("key2"));
    }
    
    public void testStoreAndRead() {
        RepositoryInfo storedInfo = 
            new RepositoryInfo(
                "id", 
                "cid", 
                "http://url", 
                "displayName", 
                "tooltip", 
                "user", 
                "httpUser", 
                "password".toCharArray(), 
                "httpPassword".toCharArray());
        storedInfo.putValue("key1", "value1");
        storedInfo.putValue("key2", "value2");
        
        Preferences pref = NbPreferences.forModule(RepositoryInfo.class);
        storedInfo.store(pref, "key");
        RepositoryInfo readInfo = RepositoryInfo.read(pref, "key");
        
        assertEquals(storedInfo.getID(), readInfo.getID());
        assertEquals(storedInfo.getConnectorId(), readInfo.getConnectorId()); 
        assertEquals(storedInfo.getUrl(), readInfo.getUrl());
        assertEquals(storedInfo.getDisplayName(), readInfo.getDisplayName());
        assertEquals(storedInfo.getTooltip(), readInfo.getTooltip());
        assertEquals(storedInfo.getUsername(), readInfo.getUsername());
        assertEquals(storedInfo.getHttpUsername(), readInfo.getHttpUsername());
        assertEquals(new String(storedInfo.getPassword()), new String(readInfo.getPassword()));
        assertEquals(new String(storedInfo.getHttpPassword()), new String(readInfo.getHttpPassword()));
        assertEquals(storedInfo.getValue("key1"), readInfo.getValue("key1"));
        assertEquals(storedInfo.getValue("key2"), readInfo.getValue("key2"));
    }
    
    public void testNoPassword() {
        RepositoryInfo info = 
            new RepositoryInfo(
                "id", 
                "cid", 
                "http://url", 
                "displayName", 
                "tooltip", 
                "user", 
                "httpUser", 
                null, 
                null);        
        assertEquals(0, info.getHttpPassword().length);
        assertEquals(0, info.getHttpPassword().length);
        
        info = 
            new RepositoryInfo(
                "id", 
                "cid", 
                "http://url", 
                "displayName", 
                "tooltip", 
                "user", 
                "httpUser", 
                new char[0], 
                new char[0]);        
        assertEquals(0, info.getHttpPassword().length);
        assertEquals(0, info.getHttpPassword().length);
    }
}
