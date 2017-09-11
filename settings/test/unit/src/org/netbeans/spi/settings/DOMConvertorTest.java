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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
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

package org.netbeans.spi.settings;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author  Jan Pokorsky
 */
public class DOMConvertorTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(DOMConvertorTest.class);
    }

    /** Creates a new instance of EnvTest */
    public DOMConvertorTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 15000;
    }
    
    @Override
    protected void setUp() throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);
    }
    
    public void testCreateSetting() throws Exception {
        try {
        org.openide.filesystems.FileUtil.createFolder(FileUtil.getConfigRoot(), "testCreateSetting");
        DataFolder folder = DataFolder.findFolder(FileUtil.getConfigFile("testCreateSetting"));
        
        ComposedSetting cs = new ComposedSetting();
        cs.b1 = new java.awt.Button();
        cs.b2 = cs.b1;
        cs.cs = new ComposedSetting();
        cs.cs.b1 = new java.awt.Button();
        DataObject dobj = InstanceDataObject.create(folder, "testCreateSetting", cs, null);
        
        // test reading
        FileObject fo = dobj.getPrimaryFile().copy(FileUtil.getConfigRoot(), dobj.getPrimaryFile().getName() + "_copy", "settings");
        org.openide.cookies.InstanceCookie ic = DataObject.find(fo).getCookie(org.openide.cookies.InstanceCookie.class);
        assertNotNull("missing InstanceCookie", ic);
        assertEquals(cs.getClass(), ic.instanceClass());
        
        try {
            ComposedSetting cs2 = (ComposedSetting) ic.instanceCreate();
            assertEquals(cs2.b1, cs2.b2);
        } catch (IOException e) {
            System.err.println("File contents:\n");
            FileUtil.copy(fo.getInputStream(), System.err);
            throw e;
        }
        } catch (Exception ex) {
            Logger.global.log(Level.WARNING, null, ex);
            throw ex;
        }
    }
    
    public void testCreateSetting_XML() throws Exception {
        try {
        org.openide.filesystems.FileUtil.createFolder(FileUtil.getConfigRoot(), "testCreateSetting");
        DataFolder folder = DataFolder.findFolder(FileUtil.getConfigFile("testCreateSetting"));

        ComposedSetting cs = new ComposedSetting();
        cs.b1 = new java.awt.Button();
        cs.b2 = cs.b1;
        cs.cs = new ComposedSetting();
        cs.cs.b1 = new java.awt.Button();
        DataObject dobj = InstanceDataObject.create(folder, "testCreateSetting", cs, null);

        // test reading
        FileObject fo = dobj.getPrimaryFile().copy(FileUtil.getConfigRoot(), dobj.getPrimaryFile().getName() + "_copy", "xml");
        fo.getParent().setAttribute("recognizeXML", Boolean.TRUE);
        org.openide.cookies.InstanceCookie ic = DataObject.find(fo).getCookie(org.openide.cookies.InstanceCookie.class);
        assertNotNull("missing InstanceCookie", ic);
        assertEquals(cs.getClass(), ic.instanceClass());

        try {
            ComposedSetting cs2 = (ComposedSetting) ic.instanceCreate();
            assertEquals(cs2.b1, cs2.b2);
        } catch (IOException e) {
            System.err.println("File contents:\n");
            FileUtil.copy(fo.getInputStream(), System.err);
            throw e;
        }
        } catch (Exception ex) {
            Logger.global.log(Level.WARNING, null, ex);
            throw ex;
        }
    }

    public static class ComposedSetting {
        java.awt.Button b1;
        java.awt.Button b2;
        ComposedSetting cs;
    }
    
    public static class ComposedSettingConvertor extends DOMConvertor {
        private final static String PUBLIC_ID = "-//NetBeans org.netbeans.modules.settings.xtest//DTD ComposedSetting 1.0//EN"; // NOI18N
        private final static String SYSTEM_ID = "nbres:/org/netbeans/modules/settings/convertors/data/composedsetting-1_0.dtd"; // NOI18N
        private final static String ELM_COMPOSED_SETTING = "composedsetting"; // NOI18N
        
        public ComposedSettingConvertor() {
            super(PUBLIC_ID, SYSTEM_ID, ELM_COMPOSED_SETTING);
        }
        
        protected Object readElement(org.w3c.dom.Element element) throws java.io.IOException, ClassNotFoundException {
            if (!element.getTagName().equals(ELM_COMPOSED_SETTING)) {
                throw new IllegalArgumentException("required element: " +
                    ELM_COMPOSED_SETTING + ", but was: " + element.getTagName());
            }
            
            // test presence of context
            Lookup l = findContext(element.getOwnerDocument());
            if (l == null) throw new NullPointerException("missing context");
            FileObject fo = (FileObject) l.lookup(FileObject.class);
            if (fo == null) throw new NullPointerException("missing info about source");
            
            ComposedSetting cs = new ComposedSetting();
            NodeList nl = element.getChildNodes();
            for (int i = 0, len = nl.getLength(); i < len; i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    Object obj = delegateRead((Element) n);
                    if (obj instanceof java.awt.Button) {
                        if (cs.b1 == null) {
                            cs.b1 = (java.awt.Button) obj;
                        } else {
                            cs.b2 = (java.awt.Button) obj;
                        }
                    } else {
                        cs.cs = (ComposedSetting) obj;
                    }
                }
            }
            return cs;
        }
        
        public void registerSaver(Object inst, Saver s) {
        }
        
        public void unregisterSaver(Object inst, Saver s) {
        }
        
        protected void writeElement(org.w3c.dom.Document doc, org.w3c.dom.Element el, Object inst) throws java.io.IOException, org.w3c.dom.DOMException {
            if (!(inst instanceof ComposedSetting)) {
                throw new IllegalArgumentException("required: " + ComposedSetting.class.getName() + " but was: " + inst.getClass());
            }
            ComposedSetting cs = (ComposedSetting) inst;
            // test CDATA wrapping
            Element subel;
            if (cs.b1 != null) {
                subel = delegateWrite(doc, cs.b1);
                el.appendChild(subel);
            }
            if (cs.b2 != null) {
                subel = delegateWrite(doc, cs.b2);
                el.appendChild(subel);
            }

            if (cs.cs != null) {
                subel = delegateWrite(doc, cs.cs);
                el.appendChild(subel);
            }
            
            // test presence of context
            Lookup l = findContext(doc);
            if (l == null) throw new NullPointerException("missing context");
            FileObject fo = (FileObject) l.lookup(FileObject.class);
            if (fo == null) throw new NullPointerException("missing info about source");
        }
        
    }
}
