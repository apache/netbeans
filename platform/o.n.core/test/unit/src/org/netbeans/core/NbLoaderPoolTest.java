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

package org.netbeans.core;

import java.io.*;
import java.util.*;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.*;
import org.openide.actions.*;
import org.openide.loaders.DataLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.actions.SystemAction;


/**
 *
 * @author Jaroslav Tulach
 */
public class NbLoaderPoolTest extends NbTestCase {
    private OldStyleLoader oldL;
    private NewStyleLoader newL;

    public NbLoaderPoolTest (String testName) {
        super (testName);
    }

    protected @Override void setUp() throws Exception {
        NbLoaderPool.installationFinished();

        oldL = DataLoader.getLoader(OldStyleLoader.class);
        newL = DataLoader.getLoader(NewStyleLoader.class);
        NbLoaderPool.doAdd(oldL, null, NbLoaderPool.getNbLoaderPool());
        NbLoaderPool.doAdd(newL, null, NbLoaderPool.getNbLoaderPool());
        NbLoaderPool.waitFinished();
    }

    protected @Override void tearDown() throws Exception {
        NbLoaderPool.remove(oldL, NbLoaderPool.getNbLoaderPool());
        NbLoaderPool.remove(newL, NbLoaderPool.getNbLoaderPool());
    }

    public void testOldLoaderThatChangesActionsBecomesModified () throws Exception {
        assertFalse("Not modified at begining", NbLoaderPool.isModified(oldL));
        Object actions = oldL.getActions ();
        assertNotNull ("Some actions there", actions);
        assertTrue ("Default actions called", oldL.defaultActionsCalled);
        assertFalse("Still not modified", NbLoaderPool.isModified(oldL));
        
        List<SystemAction> list = new ArrayList<SystemAction>();
        list.add(SystemAction.get(OpenAction.class));
        list.add(SystemAction.get(NewAction.class));
        oldL.setActions(list.toArray(new SystemAction[0]));
        
        assertTrue("Now it is modified", NbLoaderPool.isModified(oldL));
        List l = Arrays.asList (oldL.getActions ());
        assertEquals ("Actions are the same", list, l);        
    }
    
    public void testNewLoaderThatChangesActionsBecomesModified () throws Exception {
        assertFalse("Not modified at begining", NbLoaderPool.isModified(newL));
        Object actions = newL.getActions ();
        assertNotNull ("Some actions there", actions);
        assertTrue ("Default actions called", newL.defaultActionsCalled);
        assertFalse("Still not modified", NbLoaderPool.isModified(newL));
        
        List<SystemAction> list = new ArrayList<SystemAction>();
        list.add(SystemAction.get(OpenAction.class));
        list.add(SystemAction.get(NewAction.class));
        newL.setActions(list.toArray(new SystemAction[0]));
        
        assertFalse("Even if we changed actions, it is not modified", NbLoaderPool.isModified(newL));
        List l = Arrays.asList (newL.getActions ());
        assertEquals ("But actions are changed", list, l);        
    }
    
    public static class OldStyleLoader extends UniFileLoader {
        boolean defaultActionsCalled;
        
        public OldStyleLoader () {
            super(MultiDataObject.class.getName());
        }
        
        protected MultiDataObject createMultiObject(FileObject fo) throws IOException {
            throw new IOException ("Not implemented");
        }

        @SuppressWarnings("deprecation")
        protected @Override SystemAction[] defaultActions() {
            defaultActionsCalled = true;
            SystemAction[] retValue;
            
            retValue = super.defaultActions();
            return retValue;
        }
        
        
    }
    
    public static final class NewStyleLoader extends OldStyleLoader {
        protected @Override String actionsContext() {
            return "Loaders/IamTheNewBeginning";
        }
    }
}
