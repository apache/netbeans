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

/*
 * Factoring of AccessWindow, MemuseWindow and LeaksWindow
 */

import org.netbeans.modules.cnd.debugger.common2.debugger.io.TermComponentFactory;
import java.util.HashSet;

import java.io.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import javax.accessibility.*;

import org.openide.*;
import org.openide.util.HelpCtx;


import org.netbeans.lib.terminalemulator.ActiveRegion;
import org.netbeans.lib.terminalemulator.ActiveTermListener;

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import org.netbeans.modules.cnd.debugger.common2.utils.options.Option;

import javax.swing.JComponent.AccessibleJComponent;


/**
 * Code common to AccessComponent, LeaksComponent and MemuseComponent.
 */

abstract class RtcComponent extends OldTermComponent implements Accessible {

    private final RtcView view;

    protected ActiveTermListener termListener;
    protected final HyperlinkKeyProcessor hyperlinkProcessor;

    private RtcModel model;
    private RtcModel.Listener listener;
    private RtcController controller;

    private Renderer renderer;


    // As we render unto the term we also write into 'saveAsString'
    // which may be saved by saveAs.
    private StringBuilder saveAsString;

    protected boolean details = false;

    RtcComponent(RtcView view, Hyperlink.Resolver resolver) {
	    super(view.owner(), TermComponentFactory.ACTIVE);
	this.view = view;

	hyperlinkProcessor = setupHyperlinkProcessing();

	renderer = Renderer.newTermRenderer(getActiveTerm(),
					    resolver,
					    hyperlinkProcessor);
	renderer.setDetailedStack(view.isDetailedStackView());

	getActiveTerm().setAnchored(true);

	// override term settings so possiblyNormalize works ..
	getActiveTerm().setScrollOnInput(false);

	initComponents(); // Maintained by GUI Designer
	add(BorderLayout.CENTER, getActiveTerm());
	additionalInitComponents();
	initializeA11y();

	addPopupListener(getActiveTerm().getScreen());
	addPopupListener(this);


	termListener = new ActiveTermListener() {
	    public void action(ActiveRegion r, InputEvent e) {
		// ignore root region
		if (r.parent() == null)
		    return;
		hyperlinkProcessor.setHyperlink(r);
		hyperlinkProcessor.activateHyperlink();
	    }
	};

	// Listen for Hyper link action:
	getActiveTerm().setActionListener(termListener);

    }

    private void initComponents() {
        setLayout(new java.awt.BorderLayout());
    }


    protected RtcView view() {
	return view;
    }

    protected RtcModel model() {
	return model;
    }

    Renderer renderer() {
	return renderer;
    }

    protected RtcController controller() {
	return controller;
    }


    /*package*/ void listenTo(RtcModel model,
			    RtcModel.Listener listener,
			    RtcController controller) {

	// out with the old
	RtcModel oldModel = this.model;
	RtcModel.Listener oldListener = this.listener;
	if (oldModel != null)
	    oldModel.removeListener(oldListener);

	// in with the new
	this.model = model;
	this.listener = listener;
	this.controller = controller;
	if (model != null)
	    model.addListener(listener);
    }


    /**
     * FileChooser dialog for RTC which allows user to choose
     * what parts of the experiment to save.
     *
     * LATER SHOULD provide feedback regarding what is available.
     * For now if info ins't available print "not available".
     */

    static private class RTCSaveAsTextFileChooser extends JFileChooser {

	/*
	 * Describes what the user intends to save.
	 */
	public static class SaveOptions {
	    public boolean access = false;
	    public boolean memuse = false;
	    public boolean memuse_summary = true;
	    public boolean memuse_detailed = false;
	    public boolean leaks = false;
	    public boolean leaks_summary = true;
	    public boolean leaks_detailed = false;
	}

	private final SaveOptions saveOptions;

	public RTCSaveAsTextFileChooser(SaveOptions saveOptions) {
	    this.saveOptions = saveOptions;

	    setDialogTitle(Catalog.get("Rtc_SaveAsText")); // NOI18N
	    setFileSelectionMode(JFileChooser.FILES_ONLY);
	    setMultiSelectionEnabled(false);
	    // OLD setFileSystemView(new UnixFileSystemView(getFileSystemView()));
	    setFileHidingEnabled(false);
	    // SHOULD pick a decent default here?
	    // setCurrentDirectory(dir);
	}
    }

    public final void saveAs() {

	RTCSaveAsTextFileChooser.SaveOptions saveOptions =
	    new RTCSaveAsTextFileChooser.SaveOptions();
	if (view.isAccessVisible())
	    saveOptions.access = true;
	else if (view.isMemuseVisible())
	    saveOptions.memuse = true;
	else if (view.isLeaksVisible())
	    saveOptions.leaks = true;

	RTCSaveAsTextFileChooser chooser =
	    new RTCSaveAsTextFileChooser(saveOptions);
	File file = null;

	int returnVal = chooser.showSaveDialog(null);
	if (returnVal == JFileChooser.APPROVE_OPTION)
	    file = chooser.getSelectedFile();

	if (file != null) {
	    try {
		PrintStream out = new PrintStream(file);
		Renderer saveRenderer = renderer;
		try {
		    renderer = Renderer.newTextRenderer(out);
		    renderer.setDetailedStack(true);
		    refresh();
		} finally {
		    renderer = saveRenderer;
		    out.close();
		}
	    } catch (IOException e) {
		String msg = Catalog.format("FailedToSaveAsText",
					    file.getAbsolutePath(),
					    e.getMessage());
		ErrorManager.getDefault().annotate(e,
						   ErrorManager.USER,
						   msg,
						   msg,
						   null,
						   null);
		ErrorManager.getDefault().notify(e);
	    }
	}
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("Debugging_RTC"); // NOI18N
    }

    /**
     * Set accessible names and descriptions as necessary
     */
    @Override
    protected void initializeA11y() {
	super.initializeA11y();

	/*
	 * We need to do the following:
	 * - Have Term pass thru all of the system accelerator keystrokes.
	 *   This is done by creating a list and passing it in via
	 *   Term.setKeyStrokeSet().
	 *   However, it doesn't seem to work as advartised.
	 * - Arrange for an "escape" operation, so that when focus is in 
	 *   Term we can shift focus elsewhere. 
	 *   - JLF2, Appendix A, TabbedPanes recommends Ctrl-up-arrow, but
	 *     Ctrl-up-arrow is also the primary scrolling operation, so 
	 *     we have a conflict.
	 *   - Another generic move to next component operation is
	 *     Ctrl-Tab. For example the NB editor uses it to move between 
	 *     editor tabs.
	 *     However, the NB editor is a global accelerator and overrides
	 *     our own keystroke handling.
	 */

	// KeyStrokeSet to be passed to term for it to ignore.
	HashSet<KeyStroke> kset = new HashSet<KeyStroke>();

	//
	// Tell Term to ignore the global accelerators
	// 

	/* LATER

	This doesn't work 
	the point is so that Term doesn't consume things like Alt-W
	but it seems to nevertheless

	Keymap systemKeymap = (Keymap) Lookup.getDefault().lookup(Keymap.class);
	// SHOULD use the Lookup mechanism which allows up to track changes
	KeyStroke karray[] = systemKeymap.getBoundKeyStrokes();
	for (int kx = 0; kx < karray.length; kx++) {
	    kset.add(karray[kx]);
	}
	*/


	//
	// Various attempts at providing an escape.
	// 
	/* LATER

	VK_UP + CTRL_MASK is uspposed to shift focus out of a content
	pane to the associated containing tab.
	But Term is already handling VK_UP + CTRL_MASK for scrolling.
	*/

	kset.add(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.CTRL_MASK));
	kset.add(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.CTRL_MASK));

	kset.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.CTRL_MASK));
	kset.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.SHIFT_MASK));

	// Assign the accumulated KeyStrokeSet to Term
	getActiveTerm().setKeyStrokeSet(kset);

	getActiveTerm().getScreen().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        if (e.getModifiers() == Event.CTRL_MASK) {
                            e.consume();
			    view.focusToTab(RtcComponent.this);
                        }
                        break;
                    case KeyEvent.VK_TAB:
                        if (e.getModifiers() == Event.CTRL_MASK) {
                            e.consume();
			    view.nextTab();
                        } else if (e.getModifiers() == Event.SHIFT_MASK) {
                            e.consume();
			    view.previousTab();
			}
			break;
                }
            }
	} );
    }

    protected abstract void additionalInitComponents();

    protected abstract void refreshWork();

    private boolean refreshing;

    protected boolean isRefreshing() {
	return refreshing;
    }

    public void refresh() {
	renderer().clear();
	hyperlinkProcessor.setHyperlink(null);
	refreshing = true;
	try {
	    refreshWork();
	} finally {
	    refreshing = false;
	}
    }





    /**
     * Front this Tab and its containing TopComponent if the given
     * option has that feature enabled.
     *
     * was: part of reportBegin1
     *
     * @param option Specific option governing fronting.
     *
     */

    protected void possiblyFront(Option option) {
	if (isRefreshing())
	    return;

	if (option == null)
	    return;		// static view

	if (controller() != null) {
	    OptionSet optionSet = controller().optionSet();
	    if (optionSet != null) {
		if (option.isEnabled(optionSet)) {
		    view().requestVisible(this);
		}
	    }
	}
    }

    //..........................................................................
    // Accessibility stuff is all here
    //..........................................................................

    private AccessibleContext accessibleContext;

    @Override
    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null)
	    accessibleContext = new RtcAccessibleContext();
	return accessibleContext;
    }

    protected class RtcAccessibleContext extends AccessibleJComponent {
        @Override
	public AccessibleRole getAccessibleRole() {
	    return AccessibleRole.PANEL;
	}
    }
}
