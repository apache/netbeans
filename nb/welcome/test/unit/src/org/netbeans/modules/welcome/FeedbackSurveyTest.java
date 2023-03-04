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

package org.netbeans.modules.welcome;

import java.awt.Dialog;
import java.net.URL;
import java.util.Locale;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;

/**
 *
 * @author Jaroslav Tulach
 */
public class FeedbackSurveyTest extends NbTestCase {
    
    public FeedbackSurveyTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(DD.class, UD.class);
        Locale.setDefault(new Locale("te", "ST"));
        
        DD.nd = null;
        DD.toReturn = -1;
        UD.url = null;
    }
    
    public void testStartMultipleTimesBeforeTimeout() throws Exception {
        MemoryURL.registerURL("memory://survey", "ok");
        
        for (int i = 0; i < 10; i++) {

            FeedbackSurvey.start();
            assertNull("NO dialog", DD.nd);
            assertNull("NO url", UD.url);
        
        }
        
        Thread.sleep(3000);
        DD.toReturn = 0;
            
        FeedbackSurvey.start();
        assertNotNull("Time passed, dialog shown", DD.nd);
        assertNotNull("Time passed, url shown", UD.url);
    }

    @RandomlyFails // always for jglick
    public void testStartAfterTimeout() throws Exception {
        MemoryURL.registerURL("memory://survey", "ok");
        
        for (int i = 0; i < 5; i++) {

            FeedbackSurvey.start();
            assertNull("NO dialog", DD.nd);
            assertNull("NO url", UD.url);
        
        }
        
        Thread.sleep(3000);
        DD.toReturn = 0;

        for (int i = 0; i < 3; i++) {

            FeedbackSurvey.start();
            assertNull("No dialog" + i, DD.nd);
            assertNull("No url" + i, UD.url);
        
        }
            
        FeedbackSurvey.start();
        assertNotNull("Time passed, dialog shown", DD.nd);
        assertNotNull("Time passed, url shown", UD.url);
    }
    
    @RandomlyFails // always for jglick
    public void testJustThreeReminds() throws Exception {
        MemoryURL.registerURL("memory://survey", "ok");
        
        for (int i = 0; i < 10; i++) {

            FeedbackSurvey.start();
            assertNull("NO dialog", DD.nd);
            assertNull("NO url", UD.url);
        
        }
        
        Thread.sleep(3000);

        for (int i = 0; i < 3; i++) {
            DD.toReturn = 1;
            DD.nd = null;
            FeedbackSurvey.start();
            assertNotNull("Dialog shown" + i, DD.nd);
            assertNull("but no browser" + i, UD.url);
        
        }
            
        DD.toReturn = 0;
        DD.nd = null;
        FeedbackSurvey.start();
        assertNull("No dialogs, three times canceled", DD.nd);
        assertNull("No dialogs, three times canceled", UD.url);
    }
    
    public static final class DD extends DialogDisplayer {
        static NotifyDescriptor nd;
        static int toReturn = -1;
        
        public Object notify(NotifyDescriptor descriptor) {
            assertNull("No dialog yet", nd);
            nd = descriptor;
            
            
            Object r = descriptor.getOptions()[toReturn];
            toReturn = -1;
            
            return r;
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    public static final class UD extends HtmlBrowser.URLDisplayer {
        static URL url;
        
        public void showURL(URL u) {
            assertNull("no url yet", url);
            url = u;
        }
        
    }
}
