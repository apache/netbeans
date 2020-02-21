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


public final class LeaksComponent extends RtcComponent {

    private RtcModel.ErrorCode reportLeaksType;
    
    // interface RtcComponent
    protected void additionalInitComponents() {
    }

    public LeaksComponent(RtcView view, Hyperlink.Resolver resolver) {
	super(view, resolver);
	getActiveTerm().setHorizontallyScrollable(true);
    }

    /** Complete initialization */
    public void componentOpened() {
    }

    private final RtcModel.Listener listener = new RtcModel.Listener() {

	// We ignore a memuse or leak report if it doesn't match this views
	// verbosity level

	private boolean ignoreReport;

	public void profileChanged() {}

	public void modelChanged() {}

	public void runBegin(RtcModel.Run run) {
	    String kind = Catalog.get("MemleaksStarted");	// NOI18N
	    renderer().renderRunBegin(run, kind);
	    if (run.isLeakErrorsCleared())
		renderer().reportCleared();
	}
	public void runEnd() { }

	public void accessStateChanged(RtcState state) {}
	public void accessItem(RtcModel.AccessError item) {}

	public void memuseStateChanged(RtcState state) {
	}

	public void leaksBegin(RtcModel.MemoryReportHeader header) {
            if (header.verbose() != view().isDetailedReportView()) {
                ignoreReport = true;
                return;
            } else {
                ignoreReport = false;
            }

	    reportLeaksType = header.type();
	    possiblyFront(view().frontMemuseOption());
	    renderer().renderMemoryReportBegin(header);
	}

	public void leakItem(RtcModel.MemoryReportItem item) {
	    if (!ignoreReport) {
		possiblyFront(view().frontMemuseOption());

		// SHOULD add glyph to the rtc tab widgets to indicate that
		// there are new results pending???
		// setRtcBlocksIndicator(TRUE);

		renderer().renderMemuseItem(item);
	    }
	}

	public void leaksEnd() {
	    if (!ignoreReport) {
		boolean isActual = (reportLeaksType == RtcModel.ErrorCode.ADDRESS_IN_BLOCK);
		renderer().renderLeaksEnd(isActual);
	    }
	}

	public void leaksInterrupted() {
	}

	public void memuseBegin(RtcModel.MemoryReportHeader header) {}
	public void memuseItem(RtcModel.MemoryReportItem item) {}
	public void memuseEnd() {} 
	public void memuseInterrupted() {}
    };

    public void listenTo(RtcModel model, RtcController controller) {
	if (model != null) {
	    listenTo(model, listener, controller);
	} else {
	    listenTo(null, null, controller);
	}

    }

    public void refreshWork() {
	renderer().setDetailedStack(view().isDetailedStackView());
	model().traverse(listener, view().isDetailedReportView());
    }



    // override TermWindow
    protected void populateMenu(Object source, Point pt, JPopupMenu popup) {
	int xpos = pt.x;
	int ypos = pt.y;
	/* OLD
	// Enable/Disable Memuse checking
	CallableSystemAction tmca = (CallableSystemAction)
	    SystemAction.get(ToggleLeaksChecksAction.class);
	JMenuItem jmi = tmca.getPopupPresenter();
	popup.add(jmi);

	popup.addSeparator();
	*/

	// Additional menu items
	popup.add(view().suppressAction.getPopupPresenter());
	popup.addSeparator();
	popup.add(view().newLeaksAction.getPopupPresenter());
	popup.add(view().allLeaksAction.getPopupPresenter());
	popup.addSeparator();
	popup.add(view().saveAsTextAction.getPopupPresenter());
	popup.addSeparator();
	popup.add(view().clearAction.getPopupPresenter());
	popup.add(view().clearAllAction.getPopupPresenter());
	popup.addSeparator();
	
	{
	JMenuItem detailedReportItem =
	    view().detailedReportAction.getPopupPresenter();
	JMenuItem summaryReportItem =
	    view().summaryReportAction.getPopupPresenter();
	popup.add(detailedReportItem);
	popup.add(summaryReportItem);
	
	ButtonGroup bg = new ButtonGroup();
	bg.add(detailedReportItem);
	bg.add(summaryReportItem);
	}

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
