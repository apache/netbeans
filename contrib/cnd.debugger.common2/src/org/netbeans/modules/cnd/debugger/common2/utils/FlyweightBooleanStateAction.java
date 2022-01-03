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

import java.awt.Component;

import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;

import org.openide.util.actions.Presenter;

/**
 * FlyweightBooleanStateAction is FlyweightAction as
 * BooleanStateAction is to SystemAction.
 *
 * The property booleanState is per instance.
 */

abstract public class FlyweightBooleanStateAction extends FlyweightAction
    implements Presenter.Popup, Presenter.Toolbar {

    public static final String PROP_BOOLEAN_STATE = "booleanState"; // NOI18N

    private boolean booleanState;

    protected FlyweightBooleanStateAction(Class<? extends Shared> sharedClass) {
	super(sharedClass);
	booleanState = true;
    }

    private JRadioButton button;
    private JRadioButtonMenuItem menuItem;

    // interface Presenter.Toolbar
    // override FlyweightAction
    @Override
    public Component getToolbarPresenter() {
	if (button == null) {
	    button = new JRadioButton();
	    // OLD fixButtonLF(button);
	    if (shared().iconResource() != null)
		button.putClientProperty("hideActionText", Boolean.TRUE);//NOI18N

	    // need to assign Action after setting hideActionText
	    button.setAction(this);
	    button.setSelected(booleanState);
	}

	return button;
    }

    // interface Presenter.Popup
    // override FlyweightAction
    @Override
    public JMenuItem getPopupPresenter() {
	if (menuItem == null) {
	    menuItem = new JRadioButtonMenuItem();
	    menuItem.setAction(this);
	    if (shared().iconResource() != null)
		menuItem.setIcon(null);
	    menuItem.setSelected(booleanState);
	}
	return menuItem;
    }

    // like BooleanStateAction
    public boolean getBooleanState() {
	return booleanState;
    }

    // like BooleanStateAction
    public void setBooleanState(boolean value) {
	boolean oldValue = booleanState;
	this.booleanState = value;
	if (button != null)
	    button.setSelected(booleanState);
	if (menuItem != null)
	    menuItem.setSelected(booleanState);
	firePropertyChange(PROP_BOOLEAN_STATE, oldValue, booleanState);
    }

    // interface Action
    // like BooleanStateAction
    @Override
    public void actionPerformed(java.awt.event.ActionEvent ev) {
	setBooleanState(!getBooleanState());
    }
}
