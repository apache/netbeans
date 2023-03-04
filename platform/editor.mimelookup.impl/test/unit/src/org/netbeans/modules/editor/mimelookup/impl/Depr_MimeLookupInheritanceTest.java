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

import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSeparator;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.junit.NbTestCase;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.PasteAction;
import org.openide.actions.ReplaceAction;
import org.openide.actions.FindAction;
import org.openide.actions.NewAction;


/** Testing merging and sorting merged objects
 * Testing a deprecated MimePath.childLookup behaviour
 * 
 *  @author Martin Roskanin
 */
public class Depr_MimeLookupInheritanceTest extends NbTestCase {
    
    private static final int WAIT_TIME = 2000;
    
    public Depr_MimeLookupInheritanceTest(java.lang.String testName) {
        super(testName);
    }
    
    protected @Override void setUp() throws java.lang.Exception {
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getResource("test-layer.xml")
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader(), 
            null
        );
        Logger.getLogger("org.openide.filesystems.Ordering").setLevel(Level.OFF);
    }

    private void testPopupItems(MimeLookup lookup, Class[] layerObjects){
        PopupActions actions = (PopupActions) lookup.lookup(PopupActions.class);
        assertTrue("PopupActions should be found", actions != null);
        if (actions != null){
            List popupActions = actions.getPopupActions();
            int popupSize = popupActions.size();
            assertTrue("popupActions count is not the same as etalon action count" +
                    "Expecting:"+layerObjects.length+" Found:"+popupSize,
                    popupSize == layerObjects.length);
            
            for (int i = 0; i<layerObjects.length; i++){
                Object obj = popupActions.get(i);
                assertTrue("Incorrect sorting or item is missing in the popup menu." +
                        "Expecting:"+layerObjects[i]+" Found:"+obj.getClass(),
                        layerObjects[i].isAssignableFrom(obj.getClass()));
            }   
        }
    }
    
    /** Testing Base level popup items lookup and sorting */
    public void testBaseLevelPopups(){
        MimeLookup lookup = MimeLookup.getMimeLookup(""); //NOI18N
        Class layerObjects[] = {CutAction.class, CopyAction.class, PasteAction.class};
        testPopupItems(lookup, layerObjects);
    }

    /** Testing MIME level popup items lookup, inheritance and sorting */
    public void testMimeLevelPopups(){
        MimeLookup lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/html"); //NOI18N
        Class layerObjects[] = {CutAction.class, CopyAction.class, NewAction.class, PasteAction.class};
        testPopupItems(lookup, layerObjects);
    }

    /** Testing MIME level popup items lookup, inheritance and sorting */
    public void testMimeLevelPopupsWithStringAndSeparator(){
        MimeLookup lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/html").childLookup("text/xml"); //NOI18N
        Class layerObjects[] = {CutAction.class, CopyAction.class, PasteAction.class, ReplaceAction.class, JSeparator.class, String.class};
        testPopupItems(lookup, layerObjects);
    }

    /**
     * Issue #61216: MimeLookup should support layer hidding
     */
    public void testHidding(){
        MimeLookup lookup = MimeLookup.getMimeLookup("text/xml");
        checkLookupObject(lookup, CopyAction.class, true);
        checkLookupObject(lookup, ReplaceAction.class, true);
        checkLookupObject(lookup, PasteAction.class, false);
        lookup = MimeLookup.getMimeLookup("text/x-ant+xml");
        checkLookupObject(lookup, CutAction.class, true);
        checkLookupObject(lookup, CopyAction.class, false);
        checkLookupObject(lookup, PasteAction.class, true);
        checkLookupObject(lookup, ReplaceAction.class, false);
    }
    
    /**
     * Issue #61245: Delegate application/*+xml -> text/xml
     */
    public void test61245(){
        MimeLookup lookup = MimeLookup.getMimeLookup("application/xml");
        checkLookupObject(lookup, FindAction.class, true);
        lookup = MimeLookup.getMimeLookup("application/xhtml+xml");
        checkLookupObject(lookup, CutAction.class, true);
        checkLookupObject(lookup, FindAction.class, false);
        checkLookupObject(lookup, ReplaceAction.class, true);
    }
    
    public void testAntXmlPopup(){
        MimeLookup lookup = MimeLookup.getMimeLookup("text/xml"); //NOI18N
        Class layerObjects[] = {CutAction.class, CopyAction.class, PasteAction.class, ReplaceAction.class};
        testPopupItems(lookup, layerObjects);
        lookup = MimeLookup.getMimeLookup("text/x-ant+xml"); //NOI18N
        Class layerObjects2[] = {CutAction.class, CopyAction.class, PasteAction.class, ReplaceAction.class, FindAction.class};
        testPopupItems(lookup, layerObjects2);
    }
    
    /** Method will wait max. <code> maxMiliSeconds </code> miliseconds for the <code> requiredValue </code>
     *  gathered by <code> resolver </code>.
     *
     *  @param maxMiliSeconds maximum time to wait for requiredValue
     *  @param resolver resolver, which is gathering an actual value
     *  @param requiredValue if resolver value equals requiredValue the wait cycle is finished
     *
     *  @return false if the given maxMiliSeconds time elapsed and the requiredValue wasn't obtained
     */
    protected boolean waitMaxMilisForValue(int maxMiliSeconds, ValueResolver resolver, Object requiredValue){
        int time = (int) maxMiliSeconds / 100;
        while (time > 0) {
            Object resolvedValue = resolver.getValue();
            if (requiredValue == null && resolvedValue == null){
                return true;
            }
            if (requiredValue != null && requiredValue.equals(resolvedValue)){
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                time=0;
            }
            time--;
        }
        return false;
    }
    
    /** Interface for value resolver needed for i.e. waitMaxMilisForValue method.  
     *  For more details, please look at {@link #waitMaxMilisForValue()}.
     */
    public static interface ValueResolver{
        /** Returns checked value */
        Object getValue();
    }

    
    private void checkLookupObject(final MimeLookup lookup, final Class clazz, final boolean shouldBePresent){
        waitMaxMilisForValue(WAIT_TIME, new ValueResolver(){
            public Object getValue(){
                Object obj = lookup.lookup(clazz);
                boolean bool = (shouldBePresent) ? obj != null : obj == null;
                return Boolean.valueOf(bool);
            }
        }, Boolean.TRUE);
        Object obj = lookup.lookup(clazz);
        if (shouldBePresent){
            assertTrue("Object should be present in the lookup",obj!=null);
        } else {
            assertTrue("Object should NOT be present in the lookup",obj==null);
        }
    }
    
    
}
