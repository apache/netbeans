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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.url;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import org.openide.awt.Mnemonics;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUIUtils;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
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
            presenter.setIcon(new ImageIcon(icon));
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
