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
