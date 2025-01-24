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

package org.netbeans.modules.url;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import org.openide.awt.Mnemonics;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUIUtils;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 * Presenter which creates actual components on demand.
 *
 * @author  Ian Formanek
 * @author  Marian Petras
 */
final class URLPresenter implements Presenter.Menu,
                                    Presenter.Toolbar,
                                    Presenter.Popup,
                                    ActionListener {

    /** <code>URLDataObject</code> this presenter presents */
    private final URLDataObject dataObject;
    
    /**
     * Creates a new presenter for a specified <code>URLDataObject</code>.
     *
     * @param  dataObject  <code>URLDataObject</code> to represent
     */
    URLPresenter(URLDataObject dataObject) {
        this.dataObject = dataObject;
    }

    /* implements interface Presenter.Menu */
    public JMenuItem getMenuPresenter() {
        JMenuItem menuItem = new JMenuItem();
        initialize(menuItem, false);
        return menuItem;
    }

    /* implements interface Presenter.Popup */
    public JMenuItem getPopupPresenter() {
        JMenuItem menuItem = new JMenuItem();
        initialize(menuItem, false);
        return menuItem;
    }

    /* implements interface Presenter.Toolbar */
    public Component getToolbarPresenter() {
        JButton toolbarButton = new JButton();
        initialize(toolbarButton, true);
        return toolbarButton;
    }

    /**
     * Initializes a specified presenter.
     *
     * @param  presenter  presenter to initialize
     */
    private void initialize(AbstractButton presenter, boolean useIcons) {

        if (useIcons) {
            // set the presenter's icon:
            Image icon = ImageUtilities.loadImage(
                    "org/netbeans/modules/url/urlObject.png");              //NOI18N
            try {
                FileObject file = dataObject.getPrimaryFile();
                icon = FileUIUtils.getImageDecorator(file.getFileSystem()).
                        annotateIcon(icon,
                            BeanInfo.ICON_COLOR_16x16,
                            dataObject.files());
            } catch (FileStateInvalidException fsie) {
                // OK, so we use the default icon
            }
            presenter.setIcon(ImageUtilities.image2Icon(icon));
        }

        /* set the presenter's text and ensure it is maintained up-to-date: */
        NameChangeListener listener = new NameChangeListener(presenter);
        presenter.addPropertyChangeListener(
                WeakListeners.propertyChange(listener, dataObject));
        updateName(presenter);
        /*
         * The above code works with the assumption that it is called
         * from the AWT event dispatching thread (it manipulates
         * the presenter's display name). The same applies to
         * NameChangeListener's method propertyChange(...).
         *
         * At least, both mentioned parts of code should be called from
         * the same thread since method updateText(...) is not thread-safe.
         */

        presenter.addActionListener(this);
        HelpCtx.setHelpIDString(presenter,
                                dataObject.getHelpCtx().getHelpID());
    }

    /**
     * Updates display text and tooltip of a specified presenter.
     *
     * @param  presenter  presenter whose name is to be updated
     */
    private void updateName(AbstractButton presenter) {
        String name = dataObject.getName();

        try {
            FileObject file = dataObject.getPrimaryFile();
            name = file.getFileSystem().getDecorator().annotateName(name, dataObject.files());
        } catch (FileStateInvalidException fsie) {
            /* OK, so we use the default name */
        }

        Mnemonics.setLocalizedText(presenter, name);
    }

    /* implements interface ActionListener */
    /**
     * Performs operation <em>open</em> of the <code>DataObject</code>.
     */
    public void actionPerformed(ActionEvent evt) {
        Node.Cookie open = dataObject.getCookie(OpenCookie.class);
        if (open != null) {
            ((OpenCookie) open).open();
        }
    }

    /**
     */
    private class NameChangeListener implements PropertyChangeListener {

        /** */
        private final AbstractButton presenter;

        /**
         */
        NameChangeListener(AbstractButton presenter) {
            this.presenter = presenter;
        }

        /* Implements interface PropertyChangeListener. */
        public void propertyChange(PropertyChangeEvent evt) {
            if (DataObject.PROP_NAME.equals(evt.getPropertyName())) {
                URLPresenter.this.updateName(presenter);
            }
        }

    }

}
