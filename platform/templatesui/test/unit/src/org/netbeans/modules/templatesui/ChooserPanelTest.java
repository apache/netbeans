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
package org.netbeans.modules.templatesui;

import java.awt.EventQueue;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;

public class ChooserPanelTest extends AbstractWizard {
    private CountDownLatch notified = new CountDownLatch(1);
    private boolean edt;
    private ChangeListener changeListener;

    @Test
    public void recognizesTargetChooserStep() {
        changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                edt = EventQueue.isDispatchThread();
                notified.countDown();
                assertTrue("Changes only in edt", edt);
            }
        };
        this.addChangeListener(changeListener);


        TemplateWizard tw = new TemplateWizard();
        List<WizardDescriptor.Panel<WizardDescriptor>> arr = new ArrayList<>();
        
        AbstractWizard.fillPanels(tw, this, arr, Arrays.asList("One", "targetChooser", "Three"));
        this.fireChange();
        
        assertEquals("Three panels: " + arr, 3, arr.size());
        assertTrue(arr.get(0) instanceof HTMLPanel);
        assertEquals(tw.targetChooser(), arr.get(1));
        assertTrue(arr.get(2) instanceof HTMLPanel);
    }

    @After
    public void checkListener() throws InterruptedException {
        notified.await(100, TimeUnit.MILLISECONDS);
        assertTrue("used in event dispach thread", edt);
    }

    @Override
    protected Object initSequence(ClassLoader l) throws Exception {
        return null;
    }

    @Override
    protected URL initPage(ClassLoader l) {
        return null;
    }

    @Override
    protected void initializationDone(Throwable error) {
    }

    @Override
    protected String[] getTechIds() {
        return new String[0];
    }
    
}
