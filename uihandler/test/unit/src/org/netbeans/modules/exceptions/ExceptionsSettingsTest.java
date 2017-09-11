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

package org.netbeans.modules.exceptions;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Jindrich Sedek
 */
public class ExceptionsSettingsTest extends NbTestCase {
    
    public ExceptionsSettingsTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        List<String> orderedMethods = new ArrayList<String>();
        orderedMethods.add("testGuestGetsChanged1");
        orderedMethods.add("testGuestGetsChanged2");
    
        for (String methodName : orderedMethods) {
            suite.addTest(new ExceptionsSettingsTest(methodName));
        }
    
        // Run other tests in any order.
        for (Method m : ExceptionsSettingsTest.class.getMethods()) {
            if (m.getName().startsWith("test")
                    && m.getParameterTypes().length == 0
                    && m.getGenericReturnType().equals(Void.TYPE)
                    && !Modifier.isStatic(m.getModifiers())
                    && Modifier.isPublic(m.getModifiers())
                    && !orderedMethods.contains(m.getName())) {
                
                suite.addTest(new ExceptionsSettingsTest(m.getName()));
            }
        }
        return suite;
    }

    public void testEmpty(){
        ExceptionsSettings settings = new ExceptionsSettings();
        assertNotNull(settings.getPasswd());
    }

    public void testUserName() {
        String str = "Moje_Jmeno";
        String previous;
        ExceptionsSettings settings = new ExceptionsSettings();
        previous = settings.getUserName();
        settings.setUserName(str);
        assertEquals(str, settings.getUserName());
        settings.setUserName(previous);
        assertEquals(previous, settings.getUserName());
    }

    public void testPasswd() {
        char[] str = "MY_PASSWD".toCharArray();
        ExceptionsSettings settings = new ExceptionsSettings();
        settings.setPasswd(str);
        assertArraysEquals("MY_PASSWD".toCharArray(), settings.getPasswd());
    }

    public void testIsGuest() {
        ExceptionsSettings settings = new ExceptionsSettings();
        boolean previous = settings.isGuest();
        settings.setGuest(true);
        assertTrue(settings.isGuest());
        settings.setGuest(false);
        assertFalse(settings.isGuest());
        settings.setGuest(previous);
        assertEquals(previous, settings.isGuest());
    }

    public void testSaveUserData(){
        ExceptionsSettings settings = new ExceptionsSettings();
        settings.setGuest(false);
        settings.setUserName("HALLO");
        settings.setPasswd("HALLO".toCharArray());
        settings.setRememberPasswd(true);
        settings.save();

        ReportPanel panel = new ReportPanel(false, new ExceptionsSettings());
        assertArraysEquals("correctly loaded", "HALLO".toCharArray(), panel.getPasswdChars());
        assertEquals("correctly loaded", false, panel.asAGuest());
    }
    
    public void testGuestGetsChanged1() throws Exception {
        System.out.println("testGuestGetsChanged1()");
        ExceptionsSettings settings = new ExceptionsSettings();
        settings.setGuest(false);
        // Run this first and block file change listener until guest is set to true.
    }

    public void testGuestGetsChanged2() throws Exception {
        System.out.println("testGuestGetsChanged2()");
        ExceptionsSettings settings = new ExceptionsSettings();
        settings.setGuest(true);
        boolean isGuest = settings.isGuest();
        assertTrue(isGuest);
    }

    public void assertArraysEquals(String message, char[] x, char[] y){
        assertEquals(message, x.length, y.length);
        for (int i = 0; i < y.length; i++) {
            assertEquals(message, x[i], y[i]);
        }

    }
    public void assertArraysEquals(char[] x, char[] y){
        assertArraysEquals(null, x, y);
    }
}
