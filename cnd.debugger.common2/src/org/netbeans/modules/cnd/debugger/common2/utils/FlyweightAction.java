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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Image;
import java.awt.Component;

import javax.swing.event.MouseInputAdapter;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.openide.util.ImageUtilities;
import org.openide.util.HelpCtx;
import org.openide.util.SharedClassObject;
import org.openide.util.actions.Presenter;

/**
 * A FlyweightAction allows multiple instantiations while sharing common data
 * in a SharedClassObject.
 *
 * The following properties are per instance:
 * - handling of actionPerformed
 * - enabledness.
 * All other properties are shared.
 */

abstract public class FlyweightAction extends AbstractAction
    implements HelpCtx.Provider, Presenter.Toolbar, Presenter.Popup {

    protected abstract static class Shared extends SharedClassObject {

	private static final String BLANK_ICON =
	    "org/openide/resources/actions/empty.gif"; // NOI18N
	private static ImageIcon blankIcon;
	private Icon image;

	protected Shared() {
	    initialize();
	}

	// like SystemAction
	abstract protected String iconResource();

	// like SystemAction
	abstract public HelpCtx getHelpCtx();

	// like SystemAction
        @Override
	protected void initialize() {
	    super.initialize();
	}

	// utility
	protected void setMnemonic(char mnemonic) {
	    putValue(MNEMONIC_KEY, new Integer(mnemonic));
	}

	// utility
	protected void setAccelerator(String acceleratorStr) {
	    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(acceleratorStr));
	}

	// interface Action
	public final void putValue(String name, Object value) {
	    // delegate to SharedClassObject
	    putProperty(name, value, true);
	}

	// interface Action
	public Object getValue(String key) {
	    if (Action.SMALL_ICON.equals(key))
		return getIcon(false);
	    else
		return getProperty(key);
	}

	private static ImageIcon getBlankIcon() {
	    if (blankIcon == null)
		blankIcon = new ImageIcon(ImageUtilities.loadImage(BLANK_ICON, true));
	    return blankIcon;
	}

	// like SystemAction
	final Icon getIcon(boolean createLabel) {
	    if (image == null) {
		String ir = iconResource();
		if (ir != null) {
		    Image i = ImageUtilities.loadImage(ir, true);
		    if (i != null)
			image = new ImageIcon(i);
		} else {
		    assert createLabel == false;
		    // TMP image = getBlankIcon();
		}
	    }
	    return image;
	}

	public KeyStroke getAccelerator() {
	    return (KeyStroke) getValue(ACCELERATOR_KEY);
	}
    }

    private Shared shared;

    protected Shared shared() {
	return shared;
    }

    protected FlyweightAction(Class<? extends Shared> sharedClass) {
	shared = SharedClassObject.findObject(sharedClass, true);
	setEnabled(false);
    }

    // interface Action
    @Override
    public final void putValue(String name, Object value) {
	shared.putValue(name, value);
    }

    // interface Action
    @Override
    public Object getValue(String key) {
	return shared.getValue(key);
    }

    // interface HelpCtx.Provider
    @Override
    public final HelpCtx getHelpCtx() {
	return shared.getHelpCtx();
    }

    //
    // Gack! For some reason setRollOver() isn't being honerd for the toolbar
    // so we emulate it ourselves. This is what the analyzer does
    //
    private static final MouseInputAdapter sharedMouseListener =
        new MouseInputAdapter() {
        @Override
            public void mouseEntered(MouseEvent evt) {
                JButton btn = (JButton) evt.getSource();
                if (btn.isEnabled()) {
                    btn.setBorderPainted(true);
                    btn.setContentAreaFilled(true);
                }
            }
        @Override
            public void mouseExited(MouseEvent evt) {
                JButton btn = (JButton) evt.getSource();
                if (btn.isEnabled()) {
                    btn.setBorderPainted(false);
                    btn.setContentAreaFilled(false);
                }
            }
        };

    private Component fixButtonLF(Component c) {
	if (c instanceof JButton) {
	    JButton b = (JButton) c;
	    b.setBorderPainted(false);
	    b.setContentAreaFilled(false);
	    b.setFocusable(false);
	    b.addMouseListener(sharedMouseListener);
	}
	return c;
    }

    // interface Presenter.Toolbar
    @Override
    public Component getToolbarPresenter() {
	JButton button = new JButton();
	fixButtonLF(button);
	if (shared.iconResource() != null)
	    button.putClientProperty("hideActionText", Boolean.TRUE);//NOI18N

	// need to assign Action after setting hideActionText
	button.setAction(this);
	if (getAccelerator() != null) {
	    String ttt = button.getToolTipText();
	    KeyStroke ks = getAccelerator();
	    String ksText = " ("; // NOI18N
	    int modifiers = ks.getModifiers();
	    if (modifiers > 0) {
		ksText += KeyEvent.getKeyModifiersText(modifiers);
		// NB buttons use +
		// NB menu items use - (but sometimes +)
		// JLF uses - always
		ksText += "+";	// NOI18N
	    }
	    ksText += KeyEvent.getKeyText(ks.getKeyCode());
	    ksText += ")"; // NOI18N

	    ttt += ksText;
	    button.setToolTipText(ttt);
	    button.setMnemonic(0);
	}

	return button;
    }

    // interface Presenter.Popup
    @Override
    public JMenuItem getPopupPresenter() {
	JMenuItem menuItem = new JMenuItem();
	menuItem.setAction(this);
	if (shared.iconResource() != null)
	    menuItem.setIcon(null);
	return menuItem;
    }

    // like SystemAction
    public final Icon getIcon() {
	return shared.getIcon(false);
    }

    // like SystemAction
    public String getName() {
	return (String) shared.getValue(Action.NAME);
    }

    public KeyStroke getAccelerator() {
	return shared.getAccelerator();
    }


    /**
     * Update action state, like enabledness, based on instance accessible data.
     */
    public void update() {
    }
}
