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
package org.netbeans.core;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import javax.swing.JDialog;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.startup.TopLogging;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.UserQuestionException;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NbErrorManagerUserQuestionTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NbErrorManagerUserQuestionTest.class);
    }

    public NbErrorManagerUserQuestionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        MockServices.setServices(MockDD.class);
        System.setProperty("netbeans.user", getWorkDirPath());
        
        // init the whole system
        TopLogging.initializeQuietly();
    }
    
    public void testUserQuestionExceptionDisplayedOK() throws Exception {
        class UQE extends UserQuestionException {
            UQE() {
                super("HelloTest");
            }
            
            boolean confirm;
            @Override
            public void confirmed() throws IOException {
                confirm = true;
            }

            @Override
            public String getLocalizedMessage() {
                return "Reboot?";
            }
        }
        MockDD.reply = NotifyDescriptor.OK_OPTION;
        
        UQE ex = new UQE();
        Exceptions.printStackTrace(ex);
        
        waitEDT();

        assertNotNull("Dialog created", MockDD.lastDescriptor);
        assertEquals("Message is localized text", "Reboot?", MockDD.lastDescriptor.getMessage());
        assertTrue("The message has been confirmed", ex.confirm);
    }
    
    public void testUserQuestionExceptionDisplayedCancel() throws Exception {
        class UQE extends UserQuestionException {
            UQE() {
                super("HelloTest");
            }
            
            boolean confirm;
            @Override
            public void confirmed() throws IOException {
                confirm = true;
            }

            @Override
            public String getLocalizedMessage() {
                return "Reboot?";
            }
        }
        MockDD.reply = NotifyDescriptor.CLOSED_OPTION;
        
        UQE ex = new UQE();
        Exceptions.printStackTrace(ex);
        
        waitEDT();

        assertNotNull("Dialog created", MockDD.lastDescriptor);
        assertEquals("Message is localized text", "Reboot?", MockDD.lastDescriptor.getMessage());
        assertFalse("The message has not been confirmed", ex.confirm);
    }

    private void waitEDT() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
            }
        });
    }
    
    
    public static final class MockDD extends DialogDisplayer {
        static Object reply;
        static NotifyDescriptor lastDescriptor;

        @Override
        public Object notify(NotifyDescriptor descriptor) {
            lastDescriptor = descriptor;
            return reply;
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            lastDescriptor = descriptor;
            return new JDialog() {
                @SuppressWarnings("deprecation")
                @Override
                public void show() {
                }
            };
        }
    }
    
    
}
