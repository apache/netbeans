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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.mimelookup.impl;

import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
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
 * 
 *  @author Martin Roskanin
 */
public class MimeLookupPopupItemsChangeTest extends NbTestCase {
    
    private static final int WAIT_TIME = 5000;
    private String fsstruct [];
    
    public MimeLookupPopupItemsChangeTest(String testName) {
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
    
    /** This method is here to simulate that it is possible to get
     * instance of the lookup without querying any of registered InstanceProvider.
     * They could acquire AWT lock and that can cause deadlocks.
     */
    private Lookup getLookup(final MimePath path) throws Exception {
        
        class BlockAWTLock implements Runnable {
            Lookup l;
            
            public void run() {
                l = MimeLookup.getLookup(path);
            }
        }
        BlockAWTLock b = new BlockAWTLock();
        
        synchronized (PopupActions.LOCK) {
            SwingUtilities.invokeAndWait(b);
        }
        
        return b.l;
    }
    
    /** Testing Base level popup items lookup and sorting */
    @RandomlyFails // NB-Core-Build #4599: resultChangedCount is:2 instead of 1
    public void testDynamicChangeInPopupFolders() throws Exception {
        final int resultChangedCount[] = new int[1];
        resultChangedCount[0] = 0;

        MimePath mp = MimePath.parse("text/x-java/text/xml/text/html");
        Lookup lookup = getLookup(mp);
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
        mp = MimePath.get(MimePath.get("text/x-java"), "text/xml");
        lookup = getLookup(mp);
        checkPopupItemPresence(lookup, ReplaceAction.class, false);        
        checkPopupItemPresence(lookup, FindAction.class, true);
        
        // lookup for ReplaceAction in the folder that doesn't exist
        lookup = MimeLookup.getLookup("text/html"); //NOI18N
        checkPopupItemPresence(lookup, ReplaceAction.class, false); 
        // create folder with ReplaceAction
        TestUtilities.createFile(getWorkDir(), 
                "Editors/text/html/Popup/org-openide-actions-ReplaceAction.instance"); //NOI18N      

        checkPopupItemPresence(lookup, ReplaceAction.class, true);
        
    }

    private void checkPopupItemPresence(final Lookup lookup, final Class checkedClazz, final boolean shouldBePresent){
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
