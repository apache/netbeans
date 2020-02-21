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

import java.awt.Event;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.netbeans.lib.terminalemulator.ActiveRegion;
import org.netbeans.lib.terminalemulator.ActiveTerm;

/*
This seems to have been inherited from OutputTabTerm, and is used for
the hyperlinks in the RTC tabs.
Branding modules interposition on OutputTabTerm for BOW does all of this
differently!
YET ... because OutputTabTerm is going away my might be forced to
or want to use a TermWindow for BOW.
*/

public class HyperlinkKeyProcessor extends KeyAdapter {

    public ActiveTerm term = null;
    private ActiveRegion currentHyperlink = null;

    public HyperlinkKeyProcessor(ActiveTerm t) {
	term = t;
    }

    public void setHyperlink(ActiveRegion ar) {
	if (ar == null) {
	    currentHyperlink = null;
	} else {
	    gotoHyperlink(ar);
	}
    }

    public void activateHyperlink() {
	if (currentHyperlink != null) {
	    Hyperlink link = (Hyperlink) currentHyperlink.getUserObject();
	    if (link != null) {
		link.activate(this);
	    }
	}
    }

    private ActiveRegion firstHyperlink() {
	ActiveRegion ar = term.regionManager().root();
	if (ar != null) {
	    ar = ar.firstChild();
	}
	return ar;
    }

    private ActiveRegion lastHyperlink() {
	ActiveRegion ar = term.regionManager().root();
	if (ar != null) {
	    ar = ar.lastChild();
	}
	return ar;
    }

    private boolean nextHyperlink() {
	ActiveRegion ar = currentHyperlink;
	if (ar == null) {
	    ar = firstHyperlink();
	} else {
	    ar = ar.getNextSibling();
	    if (ar == null) {
		ar = firstHyperlink();
	    }
	}
	gotoHyperlink(ar);
	return true;
    }

    private boolean prevHyperlink() {
	ActiveRegion ar = currentHyperlink;
	if (ar == null) {
	    ar = lastHyperlink();
	} else {
	    ar = ar.getPreviousSibling();
	    if (ar == null) {
		ar = lastHyperlink();
	    }
	}
	gotoHyperlink(ar);
	return true;
    }

    private void gotoHyperlink(ActiveRegion ar) {
	if (ar == null) {
	    return;
	}
	ActiveRegion link = ar.firstChild();
	if (link == null) {
	    // We have a one-level region
	    link = ar;
	}
	currentHyperlink = ar;
	term.setSelectionExtent(link.getExtent());
	term.possiblyNormalize(ar);
    }

    @Override
    public void keyPressed(KeyEvent e) {
	switch (e.getKeyCode()) {
	    case KeyEvent.VK_ENTER:
	    case KeyEvent.VK_SPACE:
		// Consume it first because link activation
		// may shift focus and we don't want the
		// event to be delivered elsewhere.
		// Except that it doesn't seem to help
		e.consume();
		activateHyperlink();
		break;
	    case KeyEvent.VK_T:
		if (e.getModifiers() == (Event.CTRL_MASK | Event.SHIFT_MASK)) {
		    // Shift-Ctrl-T previous error
		    prevHyperlink();
		    e.consume();
		} else if (e.getModifiers() == Event.CTRL_MASK) {
		    // Ctrl-T next error
		    nextHyperlink();
		    e.consume();
		}
		break;
	}
    }
}
