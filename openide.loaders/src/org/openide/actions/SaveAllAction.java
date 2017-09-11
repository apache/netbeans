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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.openide.actions;


import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.LifecycleManager;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/** Save all open objects.
* @see DataObject#getRegistry
* @see LifecycleManager#saveAll
*
* @author   Jan Jancura, Ian Formanek
*/
public final class SaveAllAction extends CallableSystemAction {

    public SaveAllAction () {
        // listen to the changes
        chl = new ModifiedListL();
        DataObject.getRegistry().addChangeListener(
            (org.openide.util.WeakListeners.change(chl, DataObject.getRegistry())));
    }

    static final long serialVersionUID = 333L;

    /** to make sure only one instance of this class can run at a time */
    private static final Object RUNNING = new Object ();

    /** Reference to the change listener
    * (we treat it weakly, so we have to to prevent it from
    * being finalized before finalization of this action) */
    private ChangeListener chl;

    /* Creates new HashMap and inserts some properties to it.
    * @return the hash map
    */
    @Override
    protected void initialize () {
        super.initialize ();
        // false by default
        putProperty (PROP_ENABLED, Boolean.FALSE);
        // default tooltip  warning about CoS feature #148977
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(
             org.openide.loaders.DataObject.class, "HINT_SaveAll"));
        // listen to the changes
        chl = new ModifiedListL();
        DataObject.getRegistry().addChangeListener(
            (org.openide.util.WeakListeners.change(chl, DataObject.getRegistry())));
    }

    public String getName() {
        return NbBundle.getMessage(org.openide.loaders.DataObject.class, "SaveAll");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (SaveAllAction.class);
    }

    @Override
    protected String iconResource () {
        return "org/openide/loaders/saveAll.gif"; // NOI18N
    }

    public void performAction() {
        synchronized (RUNNING) {
            while (getProperty (RUNNING) != null) {
                try {
                    RUNNING.wait ();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            putProperty (RUNNING, RUNNING);
        }
        try {
            LifecycleManager.getDefault().saveAll();
        } finally {
            synchronized (RUNNING) {
                putProperty (RUNNING, null);
                RUNNING.notifyAll ();
            }
            
        }
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    /* Listens to the chnages in list of modified data objects
    * and enables / disables this action appropriately */
    final class ModifiedListL implements ChangeListener {
        public void stateChanged(final ChangeEvent evt) {
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    setEnabled(DataObject.getRegistry().getModifiedSet().size() > 0);
                }
            });
        }
    } // end of ModifiedListL inner class
}
