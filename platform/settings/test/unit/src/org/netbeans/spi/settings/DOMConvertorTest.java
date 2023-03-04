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
        private static final String PUBLIC_ID = "-//NetBeans org.netbeans.modules.settings.xtest//DTD ComposedSetting 1.0//EN"; // NOI18N
        private static final String SYSTEM_ID = "nbres:/org/netbeans/modules/settings/convertors/data/composedsetting-1_0.dtd"; // NOI18N
        private static final String ELM_COMPOSED_SETTING = "composedsetting"; // NOI18N
        
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
