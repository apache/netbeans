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
