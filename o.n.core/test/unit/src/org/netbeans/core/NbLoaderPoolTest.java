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
