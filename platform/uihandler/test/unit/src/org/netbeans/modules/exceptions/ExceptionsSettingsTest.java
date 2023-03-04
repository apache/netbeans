/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
