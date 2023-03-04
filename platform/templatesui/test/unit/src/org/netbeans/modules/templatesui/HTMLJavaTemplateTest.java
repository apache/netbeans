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
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javax.swing.JComponent;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Model;
import net.java.html.json.Property;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.junit.NbTestCase;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

@Model(className = "XModel", properties = {
    @Property(name = "errorCode", type = int.class),
    @Property(name = "steps", type = String.class, array = true),
    @Property(name = "current", type = String.class)
})
public class HTMLJavaTemplateTest {
    @ComputedProperty static String message(String current) {
        return "Three".equals(current) ? "Finished" : null;
    }
    
    @TemplateRegistration(
        scriptEngine = "js",
        folder = "JavaTest", iconBase = "org/netbeans/modules/templatesui/x.png",
        page = "x.html",
        content = "x.js", 
        techIds = { "ein", "zwei", "drei" }
    )
    static XModel myMethod() {
        return new XModel(0, "One", "One", "Two", "Three");
    }
    
    @Test public void checkTheIterator() throws Exception {
        EnsureJavaFXPresent.checkAndThrow();
        final String path = "Templates/JavaTest/x.js";
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
        HTMLPanel h1 = (HTMLPanel) p1;
        HTMLWizard it = (HTMLWizard) h1.getWizard();
        
        final CountDownLatch cdl = it.initializationDone;
        cdl.await();
        
        Component cmp1 = p1.getComponent();
        assertNotNull("component initialized", cmp1);
        assertEquals("the right title", "One", cmp1.getName());
        
        while (!p1.isValid()) {
            awaitFX();
        }
        assertTrue("error code set to 0", p1.isValid());

        try {
            System.setProperty("assertgc.paths", "0");
            NbTestCase.assertGC("Shouldn't GC", it.ref());
            throw new IllegalStateException("Ref for " + it.data() + " should exist: " + it.ref().get());
        } catch (AssertionError ex) {
            // OK
        }
        
        assertSelectedIndex("Zero th panel is selected", cmp1, 0);
        
        assertSteps("There steps", cmp1, "One", "Two", "Three");
        
        assertCurrentStep(h1, "One");

        h1.getWizard().setProp("errorCode", 10);
        assertFalse("Now we are not valid", h1.getWizard().isValid());
        
        h1.getWizard().setProp("errorCode", 0);
        assertTrue("Now we are valid", h1.getWizard().isValid());
        
        h1.getWizard().nextPanel();
        assertCurrentStep(h1, "Two");
        
        h1.getWizard().nextPanel();
        assertCurrentStep(h1, "Three");
        
        Set res = h1.getWizard().instantiate();
        assertEquals("One file created: " + res, res.size(), 1);
        
        Object dObj = res.iterator().next();
        assertTrue("It is data object: " + dObj, dObj instanceof DataObject);
        
        FileObject created = ((DataObject)dObj).getPrimaryFile();
        
        assertTrue("Error: " + created.asText(), created.asText().contains("Hello from Finished"));
        
        assertEquals("Three techIds", it.getTechIds().length, 3);
        assertEquals("ein", it.getTechIds()[0]);
        assertEquals("zwei", it.getTechIds()[1]);
        assertEquals("drei", it.getTechIds()[2]);
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
    
    static void assertSelectedIndex(String msg, Component c, int index) {
        Object selIndex = ((JComponent)c).getClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX);
        assertTrue(msg + selIndex, selIndex instanceof Number);
        assertEquals(msg, index, ((Number)selIndex).intValue());
    }

    static void assertSteps(String msg, Component c, Object... arr) {
        Object obj = ((JComponent)c).getClientProperty(WizardDescriptor.PROP_CONTENT_DATA);
        assertTrue(msg + " it is array: " + obj, obj instanceof Object[]);
        Object[] real = (Object[]) obj;
        assertEquals(msg + " same size", arr.length, real.length);
        assertEquals(msg, Arrays.asList(arr), Arrays.asList(real));
    }
    
    static void assertCurrentStep(HTMLPanel p, String name) throws Exception {
        Object value = p.getWizard().evaluateProp("current");
        assertEquals("Current step is set properly", name, value);
    }
}
