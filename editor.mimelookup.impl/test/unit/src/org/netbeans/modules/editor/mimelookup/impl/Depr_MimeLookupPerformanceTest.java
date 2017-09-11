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
