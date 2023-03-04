/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.browser.api;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.web.browser.ui.picker.BrowserCombo;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Support for web browser selection in the UI.
 * @since 1.9
 */
public final class BrowserUISupport {

    private static final Logger LOGGER = Logger.getLogger(BrowserUISupport.class.getName());

    private BrowserUISupport() {
    }

    /**
     * Create model for component with browsers, possibly with the
     * {@link BrowserComboBoxModel#getSelectedBrowserId() selected browser identifier}.
     * <p>
     * If the browser identifier is {@code null} (likely not set yet?), then the
     * selected browser will depend on whether {@code showIDEGlobalBrowserOption} is set
     * to true or not. If it is set to true then {@link #getDefaultBrowserId() IDE default}
     * browser is selected; otherwise a browser with NetBeans Connector will be selected.
     * @param selectedBrowserId browser identifier, can be {@code null} if e.g. not set yet
     * @param showIDEGlobalBrowserOption show "IDE's Default Browser" option
     * @return model for component with browsers
     * @see #createBrowserRenderer()
     */
    public static BrowserComboBoxModel createBrowserModel(@NullAllowed String selectedBrowserId, boolean showIDEGlobalBrowserOption) {
        return createBrowserModel(selectedBrowserId, showIDEGlobalBrowserOption, false);
    }

    /**
     * Create model for component with browsers, possibly with the
     * {@link BrowserComboBoxModel#getSelectedBrowserId() selected browser identifier}.
     * <p>
     * If the browser identifier is {@code null} (likely not set yet?), then the
     * selected browser will depend on whether {@code showIDEGlobalBrowserOption} is set
     * to true or not. If it is set to true then {@link #getDefaultBrowserId() IDE default}
     * browser is selected; otherwise a browser with NetBeans Connector will be selected.
     * @param selectedBrowserId browser identifier, can be {@code null} if e.g. not set yet
     * @param showIDEGlobalBrowserOption show "IDE's Default Browser" option
     * @param includePhoneGap show PhoneGap browser
     * @return model for component with browsers
     * @see #createBrowserRenderer()
     */
    public static BrowserComboBoxModel createBrowserModel(@NullAllowed String selectedBrowserId,
            boolean showIDEGlobalBrowserOption, boolean includePhoneGap) {
        List<WebBrowser> browsers = WebBrowsers.getInstance().getAll(false, showIDEGlobalBrowserOption, includePhoneGap, true);
        if (selectedBrowserId == null) {
            selectedBrowserId = getDefaultBrowserChoice(showIDEGlobalBrowserOption).getId();
        }
        return createBrowserModel(selectedBrowserId, browsers);
    }

    /**
     * Create model for component with given browsers.
     * <p>
     * If the browser identifier is {@code null} (likely not set yet?), then the first browser in the list
     * will be selected.
     * @param selectedBrowserId browser identifier, can be {@code null} if e.g. not set yet
     * @param browsers list of browsers
     * @return model for component with browsers
     * @see WebBrowsers#getAll(boolean, boolean, boolean, boolean)
     * @since 1.40
     */
    public static BrowserComboBoxModel createBrowserModel(@NullAllowed String selectedBrowserId, List<WebBrowser> browsers) {
        Parameters.notNull("browsers", browsers); // NOI18N
        BrowserComboBoxModel model = new BrowserComboBoxModel(browsers);
        for (int i = 0; i < model.getSize(); i++) {
            WebBrowser browser = model.getElementAt(i);
            assert browser != null;
            if (browser.getId().equals(selectedBrowserId)) {
                model.setSelectedItem(browser);
                break;
            }
        }
        return model;
    }

    /**
     * Create renderer for component with browsers.
     * @return renderer for component with browsers
     * @see #createBrowserModel(String)
     */
    public static ListCellRenderer<WebBrowser> createBrowserRenderer() {
        return new BrowserRenderer();
    }

    /**
     * Returns default recommended browser for project.
     * @param isIDEGlobalBrowserValidOption can "IDE Global Browser" browser
     *   be considered as acceptable default browser choice
     * @return
     */
    public static WebBrowser getDefaultBrowserChoice(boolean isIDEGlobalBrowserValidOption) {
        if (isIDEGlobalBrowserValidOption) {
            return findWebBrowserById(getDefaultBrowserId());
        } else {
            BrowserFamilyId preferredBrowser = WebBrowsers.getIDEOptionsBrowserFamily();
            List<WebBrowser> browsers = WebBrowsers.getInstance().getAll(false, false, true, true);
            // first try to find preferred browser if it has NB integration:
            for (WebBrowser bw : browsers) {
                if (bw.getBrowserFamily() == preferredBrowser && bw.hasNetBeansIntegration()) {
                    return bw;
                }
            }
            // try to find first browser with NB integration - either chrome or chromium:
            for (WebBrowser bw : browsers) {
                if (bw.getBrowserFamily() == BrowserFamilyId.CHROME && bw.hasNetBeansIntegration()) {
                    return bw;
                }
                if (bw.getBrowserFamily() == BrowserFamilyId.CHROMIUM && bw.hasNetBeansIntegration()) {
                    return bw;
                }
            }
            assert !browsers.isEmpty();
            return browsers.get(0);
        }
    }

    /**
     * Returns browser name with possible "with NetBeans Connector" suffix (does not apply for embedded
     * or mobile browsers).
     * @param browser browser to get name of
     * @return browser name with possible "with NetBeans Connector" suffix
     * @since 1.36
     */
    @NbBundle.Messages({
        "# {0} - browser name",
        "BrowserUISupport.browser.name.integrated={0} with NetBeans Connector",
    })
    public static String getLongDisplayName(WebBrowser browser) {
        String name = browser.getName();
        switch (browser.getBrowserFamily()) {
            case JAVAFX_WEBVIEW:
                // no suffix for embedded browser
                return name;
            default:
            if (browser.hasNetBeansIntegration()) {
                return Bundle.BrowserUISupport_browser_name_integrated(name);
            }
            return name;
        }
    }

    /**
     * Returns an ID of default IDE's browser, that is not really a browser instance
     * but an artificial browser item representing whatever is IDE's default browser.
     * @since 1.11
     */
    private static String getDefaultBrowserId() {
        return WebBrowsers.DEFAULT;
    }

    /**
     * Get browser for the given {@link BrowserComboBoxModel#getSelectedBrowserId() browser identifier}.
     * @param browserId browser identifier, cannot be {@code null}
     * @return browser for the given browser identifier; can be null if no browser
     *    corresponds to the given ID
     */
    @CheckForNull
    public static WebBrowser getBrowser(@NonNull String browserId) {
        assert browserId != null;
        return findWebBrowserById(browserId);
    }

    /**
     * Creates combo box listing all available browsers. The combo box has a special
     * drop-down list that uses the same multi-column layout as <code>BrowserPickerPopup</code>
     * @param selectedBrowserId browser identifier, can be {@code null} if e.g. not set yet
     * @param showIDEGlobalBrowserOption show "IDE's Default Browser" option
     * @param includePhoneGap show PhoneGap browser
     * @return Combo box for browser selection.
     * @see BrowserPickerPopup
     */
    public static JComboBox createBrowserPickerComboBox( @NullAllowed String selectedBrowserId,
            boolean showIDEGlobalBrowserOption, boolean includePhoneGap,
            ComboBoxModel model) {
        return new BrowserCombo( selectedBrowserId, showIDEGlobalBrowserOption, includePhoneGap, model);
    }

    public static JComboBox createBrowserPickerComboBox( @NullAllowed String selectedBrowserId,
            boolean showIDEGlobalBrowserOption, boolean includePhoneGap ) {
        return createBrowserPickerComboBox(selectedBrowserId, showIDEGlobalBrowserOption, includePhoneGap,
            BrowserUISupport.createBrowserModel( selectedBrowserId, showIDEGlobalBrowserOption, includePhoneGap ));
    }

    private static WebBrowser findWebBrowserById(String id) {
        for (WebBrowser wb : WebBrowsers.getInstance().getAll(false, true, true, false)) {
            if (wb.getId().equals(id)) {
                return wb;
            }
        }
            return null;
        }

    //~ Inner classes

    /**
     * Model for component with browsers.
     */
    public static final class BrowserComboBoxModel extends AbstractListModel<WebBrowser> implements ComboBoxModel<WebBrowser> {

        private static final long serialVersionUID = -65798754321321L;

        private final List<WebBrowser> browsers = new CopyOnWriteArrayList<WebBrowser>();

        private volatile WebBrowser selectedBrowser = null;


        BrowserComboBoxModel(List<WebBrowser> browsers) {
            assert browsers != null;
            assert !browsers.isEmpty();
            this.browsers.addAll(browsers);
            selectedBrowser = browsers.get(0);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getSize() {
            return browsers.size();
        }

        /**
         * {@inheritDoc}
         */
        @CheckForNull
        @Override
        public WebBrowser getElementAt(int index) {
            try {
                return browsers.get(index);
            } catch (IndexOutOfBoundsException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setSelectedItem(Object browser) {
            selectedBrowser = (WebBrowser) browser;
            fireContentsChanged(this, -1, -1);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public WebBrowser getSelectedItem() {
            assert selectedBrowser != null;
            return selectedBrowser;
        }

        /**
         * Get selected browser or {@code null} if the IDE default browser selected.
         * @return selected browser or {@code null} if the IDE default browser selected
         */
        @CheckForNull
        public WebBrowser getSelectedBrowser() {
            assert selectedBrowser != null;
            return selectedBrowser;
        }

        /**
         * Get selected browser identifier.
         * @return selected browser identifier
         */
        public String getSelectedBrowserId() {
            assert selectedBrowser != null;
            return selectedBrowser.getId();
        }

    }

    /**
     * Renderer for component with browsers.
     */
    private static final class BrowserRenderer implements ListCellRenderer<WebBrowser> {

        // @GuardedBy("EDT")
        private final ListCellRenderer<Object> defaultRenderer = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList<? extends WebBrowser> list, WebBrowser value, int index, boolean isSelected, boolean cellHasFocus) {
            assert EventQueue.isDispatchThread();
            Component c = defaultRenderer.getListCellRendererComponent(list, getLongDisplayName(value), index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                JLabel l = (JLabel)c;
                l.setIcon(ImageUtilities.image2Icon(value.getIconImage(true)));
            }
            return c;
        }

    }

}
