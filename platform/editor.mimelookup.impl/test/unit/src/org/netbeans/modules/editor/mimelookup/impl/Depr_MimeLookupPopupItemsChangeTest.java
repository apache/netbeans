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
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.actions.CutAction;
import org.openide.actions.FindAction;
import org.openide.actions.RenameAction;
import org.openide.actions.ReplaceAction;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/** Testing functionality of dynamic change over inherited folders
 * Testing a deprecated MimePath.childLookup behaviour
 * 
 *  @author Martin Roskanin
 */
public class Depr_MimeLookupPopupItemsChangeTest extends NbTestCase {
    
    private static final int WAIT_TIME = 5000;
    private String fsstruct [];
    
    public Depr_MimeLookupPopupItemsChangeTest(String testName) {
        super(testName);
    }
    
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        fsstruct = new String [] {
            "Editors/Popup/org-openide-actions-CutAction.instance", //NOI18N
            "Editors/Popup/org-openide-actions-CopyAction.instance", //NOI18N
            "Editors/Popup/org-openide-actions-PasteAction.instance", //NOI18N
            "Editors/text/html/Popup/org-openide-actions-DeleteAction.instance", //NOI18N
            "Editors/text/html/Popup/org-openide-actions-RenameAction.instance", //NOI18N
            "Editors/text/xml/text/html/Popup/org-openide-actions-PrintAction.instance", //NOI18N
            "Editors/text/x-java/text/xml/text/html/Popup/org-openide-actions-NewAction.instance", //NOI18N
        };

        EditorTestLookup.setLookup(fsstruct, getWorkDir(), new Object[] {},
                   getClass().getClassLoader());
        
    }

    /** Testing Base level popup items lookup and sorting */
    @RandomlyFails // NB-Core-Build #3718
    public void testDynamicChangeInPopupFolders() throws IOException{
        final int resultChangedCount[] = new int[1];
        resultChangedCount[0] = 0;

        MimeLookup lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/xml"). //NOI18N
                childLookup("text/html"); //NOI18N
        Lookup.Result result = lookup.lookup(new Template(PopupActions.class));
        result.allInstances(); // remove this line if issue #60010 is fixed
        LookupListener listener = new LookupListener(){
            public void resultChanged(LookupEvent ev){
                resultChangedCount[0]++;
            }
        };
        result.addLookupListener(listener);
        PopupActions actions = (PopupActions) lookup.lookup(PopupActions.class);
        assertTrue("PopupActions should be found", actions != null);
        List popupActions = actions.getPopupActions();
        int size = popupActions.size();
        assertTrue("Number of PopupActions found:"+size+" and should be:"+fsstruct.length, size == fsstruct.length);

        //delete RenameAction
        TestUtilities.deleteFile(getWorkDir(),
                "Editors/text/html/Popup/org-openide-actions-RenameAction.instance");
        checkPopupItemPresence(lookup, RenameAction.class, false);

        // check firing the change
        assertTrue(("resultChangedCount is:"+resultChangedCount[0]+" instead of 1"),resultChangedCount[0] == 1);
        resultChangedCount[0] = 0;
        
        //delete base CutAction
        TestUtilities.deleteFile(getWorkDir(),
                "Editors/Popup/org-openide-actions-CutAction.instance");

        checkPopupItemPresence(lookup, CutAction.class, false);

        // check firing the change
        assertTrue(("resultChangedCount is:"+resultChangedCount[0]+" instead of 1"),resultChangedCount[0] == 1);
        resultChangedCount[0] = 0;
        
        //simulate module installation, new action will be added
        TestUtilities.createFile(getWorkDir(), 
                "Editors/Popup/org-openide-actions-FindAction.instance"); //NOI18N      

        checkPopupItemPresence(lookup, FindAction.class, true);

        // check firing the change
        assertTrue(("resultChangedCount is:"+resultChangedCount[0]+" instead of 1"),resultChangedCount[0] == 1);
        resultChangedCount[0] = 0;
        
        //simulate module installation, new action will be added
        TestUtilities.createFile(getWorkDir(), 
                "Editors/text/x-java/text/xml/text/html/Popup/org-openide-actions-ReplaceAction.instance"); //NOI18N      

        checkPopupItemPresence(lookup, ReplaceAction.class, true);
        
        //ReplaceAction was created in the uppermost folder
        // let's try it is missing in the lower lookup
        lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/xml"); //NOI18N
        checkPopupItemPresence(lookup, ReplaceAction.class, false);        
        checkPopupItemPresence(lookup, FindAction.class, true);
        
        // lookup for ReplaceAction in the folder that doesn't exist
        lookup = MimeLookup.getMimeLookup("text/html"); //NOI18N
        checkPopupItemPresence(lookup, ReplaceAction.class, false); 
        // create folder with ReplaceAction
        TestUtilities.createFile(getWorkDir(), 
                "Editors/text/html/Popup/org-openide-actions-ReplaceAction.instance"); //NOI18N

        checkPopupItemPresence(lookup, ReplaceAction.class, true);
        
    }

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
