/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.templatesui;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import static junit.framework.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
