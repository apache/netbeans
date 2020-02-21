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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.dbx.arraybrowser;

import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.BorderLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JTabbedPane;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.awt.TabbedPaneFactory;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;

import com.sun.tools.swdev.glue.dbx.GPDbxVItemDynamic;
import com.sun.tools.swdev.glue.dbx.GPDbxVItemStatic;

public final class ArrayBrowserWindow extends TopComponent implements ChangeListener, PropertyChangeListener {

    private final String name = 
	Catalog.get("TITLE_ArrayBrowserWindow");  // NOI18N
    static final String preferredID = "ArrayBrowserWindow"; // NOI18N
    static ArrayBrowserWindow DEFAULT;

    // has two views now, will add at least one more for subscripts
    private final TextView abwTextView;
    private final JTabbedPane abwTabbedPane = TabbedPaneFactory.createCloseButtonTabbedPane();
    private final Vector<ArrayView> abwArrayViewVector;

    private ArrayView abwArrayView = null;

    private boolean abwProcGone;
    private NativeDebugger debugger = null;
    private ArrayBrowserController controller;

    public synchronized static ArrayBrowserWindow getDefault() {
	if (DEFAULT == null) {
	    ArrayBrowserWindow tc = (ArrayBrowserWindow) WindowManager.getDefault().findTopComponent(preferredID);
	    if (tc == null)
		new ArrayBrowserWindow();
	}
	return DEFAULT;
    }

    public ArrayBrowserWindow() {
	super.setName(name);
	// ijc FIXUP
	// setIcon(org.openide.util.Utilities.loadImage(
	//      Catalog.get("ICON_ArrayBrowserView")));  // NOI18N

	setName(Catalog.get("TITLE_ArrayBrowserView")); 
	setLayout(new BorderLayout());
	abwTextView = new TextView();
	add(abwTextView, BorderLayout.NORTH);
	abwArrayViewVector = new Vector<ArrayView>();
        abwTabbedPane.addChangeListener(this);
        abwTabbedPane.addPropertyChangeListener(TabbedPaneFactory.PROP_CLOSE, this);
	abwProcGone = false;
	DEFAULT = this;
    }

    public void addArrayView(GPDbxVItemStatic vis) {

	ArrayView av;

        av = new ArrayView();
	av.setArrayViewStatic(vis);
	av.createArrayViewScroll();
	abwArrayViewVector.add(av);
	addTab(av, abwArrayViewVector.size());
    }

    public void updateArrayView(int nitems, GPDbxVItemDynamic items[]) {

	ArrayView av;

	av = findArrayViewById(items[0].vid_id);
	if (av != null) {
	    av.setArrayViewDynamic(nitems, items);
	    av.getTable().repaint();
	}
    }

    /*package*/ ArrayView findArrayViewByName(String name) {

	int i = 0;
	int n = abwArrayViewVector.size();
        ArrayView av;
        String s;
	boolean found = false;

	for (i = 0; i < n; i++) {
	    av = abwArrayViewVector.elementAt(i);
	    s = av.getName();
	    if (name.compareTo(s) == 0) {
		found = true;
		break;
	    }
	}

	if (found)
	    return abwArrayViewVector.elementAt(i);
	else
	    return null;
    }

    /*package*/ ArrayView findArrayViewById(int id) {
	
	int i = 0;
	int n = abwArrayViewVector.size();
	boolean found = false;

	for (i = 0; i < n; i++) {
	    if (abwArrayViewVector.elementAt(i).getId() == id) {
		found = true;
		break;
	    }
	}

	if (found)
	    return abwArrayViewVector.elementAt(i);
	else
	    return null;

    }

    // interface TopComponent
    @Override
    protected String preferredID() {
	return this.getClass().getName();
    }

    // interface TopComponent
    @Override
    public void componentHidden() {
//	if (debugger != null)
//            debugger.registerArrayBrowserWindow(null);
	if (!procGone())
	    requestArrayViewUpdates(false);  // disable update
    }

    // interface TopComponent
    @Override
    public void componentShowing () {

	debugger = NativeDebuggerManager.get().currentDebugger();
	if (debugger == null)
            return;
//        debugger.registerArrayBrowserWindow(this);
	requestArrayViewUpdates(true);  // enable update
	super.componentShowing();
    }

    // interface TopComponent
    @Override
    public void componentClosed() {
	super.componentClosed();
//	if (debugger != null) {
//	    debugger.registerArrayBrowserWindow(null);
//	}
    }

    // interface TopComponent
    @Override
    public void componentActivated() {
	super.componentActivated();
    }

    // interface TopComponent
    @Override
    public int getPersistenceType() {
	return PERSISTENCE_ALWAYS;
    }

    // interface TopComponent
    @Override
    public String getName() {
	return name;
    }

    // interface TopComponent
    @Override
    public String getToolTipText() {
	// ijc FIXUP
	return Catalog.get("CTL_ArrayBrowser"); 
    }

    public void setDebugger(NativeDebugger debugger) {
	this.debugger = debugger;
    }

    public NativeDebugger getDebugger() {
	return debugger;
    }

    public void setArrayBrowserController(ArrayBrowserController c) {
	controller = c;
    }

    public ArrayBrowserController getArrayBrowserController() {
	return controller;
    }

    public void stateChanged(ChangeEvent e) {

	/* Array updates are only sent when ArrayView is "selected".
	 * So when stateChanged() is called, the newly selected
	 * array may not have up-to-date content.  Make a request
	 * to have new update for this array.
	 */

	Object s = e.getSource();
	ArrayView av = null;
	if (s instanceof JTabbedPane) {
	    JTabbedPane p = (JTabbedPane) s;
	    av = (ArrayView ) p.getSelectedComponent();
	    if (av != null) {
		/* stateChanged interface will be called when
		 * a TabbedPane is added, selected, or deleted.
		 *
		 * When a Tab is added or selected, we should make
		 * a request to engine to get updates.
		 *
		 * When a Tab is deleted, don't request update on
		 * the deleted Tab, but do request update on the Tab 
		 * the becomes  selected as the result of deletion.
		 *
		 * If a Tab is deleted (i.e. the controller sends a
		 * request to dbx to delete a vitem, then dbx sends
		 * a request to delete ArrayView in gui), the Array
		 * View corresponds to it will be removed from
		 * abwArrayViewVector and av.getId() will return null.
		 */

		if (findArrayViewById(av.getId()) != null) {
		    /* 
		     * Chech to see if the process is going away,i.e.
		     * if the stateChanged interface is called due to
		     * vitem_delete() calls make from dbx to clean up
		     * all the vitems at cb_proc_gone().  If so, there
		     * is no need to get any updates.
		     */
		    if (!procGone())
	                controller.sendArrayViewUpdates(true, av.getId());
		}
	    }
	}
    }

    public void propertyChange(PropertyChangeEvent pce) {

	if (TabbedPaneFactory.PROP_CLOSE.equals(pce.getPropertyName())) {
	    ArrayView av = (ArrayView) pce.getNewValue();
	    closeRequest(av);
	}
    }

    /*package*/ void closeRequest(ArrayView av) {
        controller.deleteArray(av.getId());
    }

    public void deleteArrayView(int id) {
	ArrayView av = findArrayViewById(id);
	if (av != null)
	    removeTab(av, abwArrayViewVector.size());
    }

    /*package*/ void removeTab(ArrayView av, int n) {

	// if (n == 1), control will not come in here
	// if (n == otheres), do nothing

	if (n == 1) {
	    // this only happens when vitem_delete is called from
	    // the engine by cb_proc_gone
	    abwArrayViewVector.remove(av);
	    remove(abwArrayView);
	    abwArrayView = null;
	    // hack: now that all the tabs have been removed
	    //       abwProcGone is not needed for this process
	    //       anymore, so reset it to false so that future 
	    //       updates from next process can happen
	    setProcGone(false);
	} else if (n == 2) {
	    abwArrayViewVector.remove(av);
	    abwArrayView = abwArrayViewVector.firstElement();
	    abwTabbedPane.remove(av);
	    abwTabbedPane.remove(abwArrayView);
	    remove(abwTabbedPane);
	    add(abwArrayView, BorderLayout.CENTER);
	    super.addImpl(abwArrayView, BorderLayout.CENTER, 0);
	    revalidate();
	} else if (n > 2) {
	    abwArrayViewVector.remove(av);
	    abwTabbedPane.remove(av);
	}
    }

    public void setProcGone(boolean status) {
	abwProcGone = status;
    }

    public boolean procGone() {
	return abwProcGone;
    }


    protected void addTab(ArrayView av, int n) {

	if (n == 1) { 
	    // first Array View, don't create TabbedPane
	    abwArrayView = av;
	    add(av, BorderLayout.CENTER);
	    super.addImpl(av, BorderLayout.CENTER, 0);
	    revalidate();
	} else if (n == 2) {
	    // 2nd Array View, create new TabbedPane
	    remove(abwArrayView);
	    add(abwTabbedPane, BorderLayout.CENTER);
	    abwTabbedPane.add(abwArrayView);
	    abwTabbedPane.add(av);
	    abwTabbedPane.setSelectedComponent(av);
	    abwArrayView = null;
	    revalidate();
	} else if (n > 2) {
	    // TabbedPane has been created already, just use it
	    abwTabbedPane.add(av);
	    abwTabbedPane.setSelectedComponent(av);
	    revalidate();
	} else { 
	    // Throw some exception 
	}
    }

    public void requestArrayViewUpdates(boolean enable) {
	if (abwArrayView != null) {
	    // only one ArrayView tab, get its id and send to engine
	    controller.sendArrayViewUpdates(enable, abwArrayView.getId());
	} else if (abwTabbedPane.getTabCount() > 1) {
	    // it is not an empty ArrayBrowserWindow
	    ArrayView av = (ArrayView) abwTabbedPane.getSelectedComponent();
	    if (av != null)
	        controller.sendArrayViewUpdates(enable, av.getId());
	}
	// if abwArrayView == null and tab count < 1, then there is
	// no ArrayView to update
    }
}
