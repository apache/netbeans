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
