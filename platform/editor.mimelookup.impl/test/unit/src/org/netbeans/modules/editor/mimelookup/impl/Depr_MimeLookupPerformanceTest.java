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

package org.netbeans.modules.editor.mimelookup.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import junit.framework.*;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.junit.MemoryFilter;
import org.netbeans.junit.NbTestCase;
import org.openide.actions.CutAction;
import org.openide.actions.FindAction;
import org.openide.actions.RenameAction;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;

/** Testing functionality of dynamic change over inherited folders
 * Testing a deprecated MimePath.childLookup behaviour
 * 
 *  @author Martin Roskanin
 */
public class Depr_MimeLookupPerformanceTest extends NbTestCase {

    private static final int WAIT_TIME = 5000;    
    private static MemoryFilter filter;
    
    private String fsstruct [];
    
    public Depr_MimeLookupPerformanceTest(java.lang.String testName) {
        super(testName);
    }
    
    protected @Override void setUp() throws java.lang.Exception {
        fsstruct = new String [] {
            "Editors/Popup/org-openide-actions-CutAction.instance", //NOI18N
            "Editors/Popup/org-openide-actions-CopyAction.instance", //NOI18N
            "Editors/Popup/org-openide-actions-PasteAction.instance", //NOI18N
            "Editors/text/xml/Popup/org-openide-actions-DeleteAction.instance", //NOI18N
            "Editors/text/xml/Popup/org-openide-actions-RenameAction.instance", //NOI18N
            "Editors/text/html/text/xml/Popup/org-openide-actions-PrintAction.instance", //NOI18N
            "Editors/text/x-java/text/html/text/xml/Popup/org-openide-actions-NewAction.instance", //NOI18N
        };

        EditorTestLookup.setLookup(fsstruct, getWorkDir(), new Object[] {},
                   getClass().getClassLoader());
        
    }

    private void gc(){
        for (int i = 0; i<5; i++){
            System.gc();
        }
    }

    private static synchronized MemoryFilter getFilter(){
        if (filter == null){
            filter = new MemoryFilter(){
                public boolean reject(Object obj){
                    return false;
                }
            };
        }
        return filter;
    }
    
    public void testDummy() {
    }
    
    /*
    public void testMimeLookupObjectInstallingUninstallingSize() throws IOException{
        MimeLookup lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/html"). //NOI18N
                childLookup("text/xml"); //NOI18N
        PopupActions popup = (PopupActions) lookup.lookup(PopupActions.class);
        List list = popup.getPopupActions();
        checkPopupItemPresence(lookup, RenameAction.class, true);
        int size = 0;
        
        for (int i=0; i<30; i++){
            //delete RenameAction
            TestUtilities.deleteFile(getWorkDir(),
                    "Editors/text/xml/Popup/org-openide-actions-RenameAction.instance");
            checkPopupItemPresence(lookup, RenameAction.class, false);

            //delete base CutAction
            TestUtilities.deleteFile(getWorkDir(),
                    "Editors/Popup/org-openide-actions-CutAction.instance");
            checkPopupItemPresence(lookup, CutAction.class, false);

            //simulate module installation, new action will be added
            TestUtilities.createFile(getWorkDir(), 
                    "Editors/Popup/org-openide-actions-FindAction.instance"); //NOI18N      
            checkPopupItemPresence(lookup, FindAction.class, true);

            // now reverse the operations

            //simulate module installation, new action will be added
            TestUtilities.createFile(getWorkDir(), 
                    "Editors/text/xml/Popup/org-openide-actions-RenameAction.instance"); //NOI18N      
            checkPopupItemPresence(lookup, RenameAction.class, true);

            TestUtilities.createFile(getWorkDir(), 
                    "Editors/Popup/org-openide-actions-CutAction.instance"); //NOI18N      
            checkPopupItemPresence(lookup, CutAction.class, true);

            //delete FindAction
            TestUtilities.deleteFile(getWorkDir(),
                    "Editors/Popup/org-openide-actions-FindAction.instance");
            checkPopupItemPresence(lookup, FindAction.class, false);

            if (i == 0){
                gc();                
                size = assertSize("", Arrays.asList( new Object[] {lookup} ), 10000000,  getFilter());
            }
        }       
        gc(); gc();
        assertSize("", size + 3000, lookup); // 3000 is threshold
    }
    
    public void testClassLookup() throws IOException{
        MimeLookup lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/html"). //NOI18N
                childLookup("text/xml"); //NOI18N
        PopupActions popup = (PopupActions) lookup.lookup(PopupActions.class);
        List list = popup.getPopupActions();
        checkPopupItemPresence(lookup, RenameAction.class, true);
        gc();
        int size = assertSize("", Arrays.asList( new Object[] {lookup} ), 10000000,  getFilter());
        for (int i=0; i<30; i++){
            popup = (PopupActions) lookup.lookup(PopupActions.class);
            list = popup.getPopupActions();
            checkPopupItemPresence(lookup, RenameAction.class, true);
        }
        gc();
        assertSize("", size, lookup);
    }

    public void testTemplateLookup() throws IOException{
        MimeLookup lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/html"). //NOI18N
                childLookup("text/xml"); //NOI18N
        Result result = lookup.lookup(new Template(PopupActions.class));
        Collection col = result.allInstances();
        checkPopupItemPresence(lookup, RenameAction.class, true);
        gc();
        int size = assertSize("", Arrays.asList( new Object[] {lookup} ), 10000000,  getFilter());
        for (int i=0; i<30; i++){
            result = lookup.lookup(new Template(PopupActions.class));
            col = result.allInstances();
            checkPopupItemPresence(lookup, RenameAction.class, true);
        }
        gc();
        assertSize("", size, lookup);
    }
    */
    
    
    
    private void checkPopupItemPresence(final MimeLookup lookup, final Class checkedClazz, final boolean shouldBePresent){
        TestUtilities.waitMaxMilisForValue(WAIT_TIME, new TestUtilities.ValueResolver(){
            public Object getValue(){
                PopupActions pa = (PopupActions)lookup.lookup(PopupActions.class);
                if (pa == null){
                    return Boolean.FALSE;
                }
                boolean bool = false;
                List items = pa.getPopupActions();
                for (int i=0; i<items.size(); i++){
                    Object obj = items.get(i);
                    if (checkedClazz == obj.getClass()){
                        bool = true;
                        break;
                    }
                }
                if (!shouldBePresent){
                    bool = !bool;
                }
                return Boolean.valueOf(bool);
            }
        }, Boolean.TRUE);
        PopupActions pa = (PopupActions)lookup.lookup(PopupActions.class);
        assertTrue("PopupActions should be found", pa != null);        
        boolean bool = false;
        List items = pa.getPopupActions();
        for (int i=0; i<items.size(); i++){
            Object obj = items.get(i);
            if (checkedClazz == obj.getClass()){
                bool = true;
                break;
            }
        }
        if (shouldBePresent){
            assertTrue("Class: "+checkedClazz+" should be present in lookup", bool);
        }else{
            assertTrue("Class: "+checkedClazz+" should not be present in lookup", !bool);
        }
    }
    
    
}
