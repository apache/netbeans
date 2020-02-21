/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.debugger.dbx.rtc;

import java.awt.Point;

import javax.swing.*;


public final class AccessComponent extends RtcComponent {

    protected void additionalInitComponents() {
    }

    public AccessComponent(RtcView view, Hyperlink.Resolver resolver) {
	super(view, resolver);
	getActiveTerm().setHorizontallyScrollable(true);
    }

    /** Complete initialization */
    public void componentOpened() {
    }

    private final RtcModel.Listener listener = new RtcModel.Listener() {
	public void profileChanged() {}

	public void modelChanged() {}

	public void runBegin(RtcModel.Run run) {
	    String kind = Catalog.get("AccessStarted");	// NOI18N
	    renderer().renderRunBegin(run, kind);
	    if (run.isAccessErrorsCleared())
		renderer().accessErrorsCleared();
	}
	public void runEnd() {
	    renderer().renderAccessEnd();
	}
	public void accessStateChanged(RtcState state) {
	}
	public void accessItem(RtcModel.AccessError item) {
	    possiblyFront(view().frontAccessOption());

	    // SHOULD add glyph to the rtc tab widgets to indicate that
	    // there are new results pending???
	    // setRtcAccessIndicator(TRUE);

	    renderer().renderAccessItem(item);
	}

	public void memuseStateChanged(RtcState state) {}

	public void memuseBegin(RtcModel.MemoryReportHeader header) {}
	public void memuseItem(RtcModel.MemoryReportItem item) {}
	public void memuseEnd() {} 
	public void memuseInterrupted() {}

	public void leaksBegin(RtcModel.MemoryReportHeader header) {}
	public void leakItem(RtcModel.MemoryReportItem item) {}
	public void leaksEnd() {}
	public void leaksInterrupted() {}
    };

    public void listenTo(RtcModel model, RtcController controller) {
	if (model != null) {
	    listenTo(model, listener, controller);
	} else {
	    listenTo(null, null, null);
	}
    }


    public void refreshWork() {
	model().traverse(listener, view().isDetailedReportView());
    }

    protected void populateMenu(Object source, Point pt, JPopupMenu popup) {
	int xpos = pt.x;
	int ypos = pt.y;

	/* OLD
	// Enable/Disable Access checking
	CallableSystemAction tmca = (CallableSystemAction)SystemAction.get(
					ToggleAccessChecksAction.class);
	JMenuItem jmi = tmca.getPopupPresenter();
 	popup.add(jmi);
	
	popup.addSeparator();
	*/

	// Additional menu items
	popup.add(view().suppressAction.getPopupPresenter());
	popup.addSeparator();
	popup.add(view().saveAsTextAction.getPopupPresenter());
	popup.addSeparator();
	popup.add(view().clearAction.getPopupPresenter());
	popup.add(view().clearAllAction.getPopupPresenter());

	if (true /* OLD Log.Rtc.godmode */) {
	    popup.addSeparator();
	    JMenuItem detailedStackItem =
		view().detailedStackAction.getPopupPresenter();
	    JMenuItem summaryStackItem =
		view().summaryStackAction.getPopupPresenter();
	    popup.add(detailedStackItem);
	    popup.add(summaryStackItem);
	    
	    ButtonGroup bg = new ButtonGroup();
	    bg.add(detailedStackItem);
	    bg.add(summaryStackItem);
	}

	// OLD addSystemActionsToMenu(popup);
    }
}
