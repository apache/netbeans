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

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.netbeans.api.templates.TemplateRegistration;
import static org.netbeans.modules.templatesui.HTMLJavaTemplateTest.assertCurrentStep;
import static org.netbeans.modules.templatesui.HTMLJavaTemplateTest.assertSelectedIndex;
import static org.netbeans.modules.templatesui.HTMLJavaTemplateTest.assertSteps;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

public class HTMLTemplateTest {
    @TemplateRegistration(
        scriptEngine = "js",
        folder = "Test", iconBase = "org/netbeans/modules/templatesui/x.png",
        page = "org/netbeans/modules/templatesui/x.html",
        content = "x.js"
    )
    static String myMethod() {
        return "init()";
    }
    
    @Test public void checkTheIterator() throws Exception {
        EnsureJavaFXPresent.checkAndThrow();
        final String path = "Templates/Test/x.js";
        FileObject fo = FileUtil.getConfigFile(path);
        assertNotNull(fo);
        
        FileObject tmp = fo.getFileSystem().getRoot().createFolder("tmp");
        DataFolder tmpF = DataFolder.findFolder(tmp);
        
        DataObject obj = DataObject.find(fo);
        
        final TemplateWizard tw = new TemplateWizard();
        tw.setTemplate(obj);
        tw.setTargetName("test");
        tw.setTargetFolder(tmpF);

        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                tw.doNextClick();
            }
        });
        
        Field f = tw.getClass().getDeclaredField("iterator");
        f.setAccessible(true);
        WizardDescriptor.Iterator<?> master = (WizardDescriptor.Iterator<?>) f.get(tw);
        assertNotNull("Master iterator found", master);
        
        WizardDescriptor.Panel<?> p1 = master.current();
        assertNotNull("Panel found", p1);
        assertTrue("It is HTML wizard: " + p1, p1 instanceof HTMLPanel);
        final HTMLPanel h1 = (HTMLPanel) p1;
        HTMLWizard it = (HTMLWizard) h1.getWizard();
        
        final CountDownLatch cdl = it.initializationDone;
        cdl.await();
        
        Component cmp1 = p1.getComponent();
        assertNotNull("component initialized", cmp1);
        
        while (!p1.isValid()) {
            awaitFX();
        }
        assertTrue("error code set to 0", p1.isValid());

        awaitSwing();
        assertSelectedIndex("Zero th panel is selected", cmp1, 0);
        
        assertSteps("There steps", cmp1, "One", "Two", "Three");
        
        assertCurrentStep(h1, "One");

        h1.getWizard().setProp("errorCode", 10);
        assertFalse("Now we are not valid", h1.getWizard().isValid());
        
        h1.getWizard().setProp("errorCode", 0);
        assertTrue("Now we are valid", h1.getWizard().isValid());
        
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    h1.getWizard().nextPanel();
                    assertCurrentStep(h1, "Two");

                    h1.getWizard().nextPanel();
                    assertCurrentStep(h1, "Three");
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        final Set res = h1.getWizard().instantiate();
        assertEquals("One file created: " + res, res.size(), 1);
        
        final DataObject[] dobjPtr = new DataObject[] { null };
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Object dObj = res.iterator().next();
                assertTrue("It is data object: " + dObj, dObj instanceof DataObject);
                dobjPtr[0] = (DataObject) dObj;
            }
        });
        
        FileObject created = dobjPtr[0].getPrimaryFile();
        
        assertTrue("Error: " + created.asText(), created.asText().contains("Hello from Finished"));
    }
    
    private static void awaitFX() throws Exception {
        final CountDownLatch cdl = new CountDownLatch(1);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                cdl.countDown();
            }
        });
        cdl.await();
    }

    private void awaitSwing() {
        try {
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                }
            });
        } catch (InterruptedException ex) {
            throw new AssertionError(null, ex);
        } catch (InvocationTargetException ex) {
            throw new AssertionError(null, ex);
        }
    }
    
}
