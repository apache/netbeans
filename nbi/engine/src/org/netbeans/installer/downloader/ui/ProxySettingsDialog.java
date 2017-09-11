/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.downloader.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.downloader.connector.MyProxy;
import org.netbeans.installer.downloader.connector.MyProxyType;
import org.netbeans.installer.downloader.connector.URLConnector;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.utils.helper.swing.NbiDialog;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiTextField;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;

/**
 *
 * @author Danila_Dugurov
 */
public class ProxySettingsDialog extends NbiDialog {
    private URLConnector connector =
            URLConnector.getConnector();
    
    private NbiTextPane  messagePane;
    
    private NbiLabel     proxyHostLabel;
    private NbiTextField proxyHostField;
    private NbiLabel     proxyPortLabel;
    private NbiTextField proxyPortField;
    private NbiLabel     ignoreListLabel;
    private NbiTextField ignoreListField;
    
    private NbiCheckBox  useProxyCheckBox;
    
    private NbiButton    applyButton;
    private NbiButton    closeButton;
    
    public ProxySettingsDialog() {
        super();
        
        initComponents();
    }
    
    public void execute() {
        setVisible(true);
        
        while (isVisible()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                ErrorManager.notifyDebug("Interrupted", e);
            }
        }
    }
    
    private void initComponents() {
        Proxy proxy = connector.getProxy(MyProxyType.HTTP);
        InetSocketAddress address = proxy != null ?
            (InetSocketAddress) proxy.address() : null;
        
        setTitle("Connectivity Problems");
        setLayout(new GridBagLayout());
        
        messagePane = new NbiTextPane();
        messagePane.setText("The installation wizard failed to " +
                "connect to the registry server. Most likely this is " +
                "caused by proxies misconfiguration. Please check the " +
                "HTTP proxy settings below and click Apply to change " +
                "them. Click Close to exit the installer.");
        
        useProxyCheckBox = new NbiCheckBox();
        useProxyCheckBox.setText("Use proxy");
        useProxyCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (useProxyCheckBox.isSelected()) {
                    proxyHostField.setEnabled(true);
                    proxyPortField.setEnabled(true);
                    ignoreListField.setEnabled(true);
                } else {
                    proxyHostField.setEnabled(false);
                    proxyPortField.setEnabled(false);
                    ignoreListField.setEnabled(false);
                }
            }
        });
        useProxyCheckBox.setSelected(connector.getUseProxy());
        
        proxyHostField = new NbiTextField();
        if (address != null) {
            proxyHostField.setText(address.getHostName());
        }
        
        proxyHostLabel = new NbiLabel();
        proxyHostLabel.setLabelFor(proxyHostField);
        proxyHostLabel.setText("Host:");
        
        proxyPortField = new NbiTextField();
        if (address != null) {
            proxyPortField.setText(Integer.toString(address.getPort()));
        }
        
        proxyPortLabel = new NbiLabel();
        proxyPortLabel.setLabelFor(proxyPortField);
        proxyPortLabel.setText("Port:");
        
        ignoreListField = new NbiTextField();
        
        if (address != null) {
            ignoreListField.setText(StringUtils.asString(
                    connector.getByPassHosts(),
                    ","));
        }
        
        ignoreListLabel = new NbiLabel();
        ignoreListLabel.setLabelFor(ignoreListField);
        ignoreListLabel.setText("Bypass proxy for:");
        
        applyButton = new NbiButton();
        applyButton.setText("&Apply");
        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Proxy proxy = null;
                
                if (useProxyCheckBox.isSelected()) {
                    proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(
                            proxyHostField.getText(),
                            Integer.parseInt(proxyPortField.getText())));
                    
                    connector.addProxy(new MyProxy(proxy, MyProxyType.HTTP));
                    
                    connector.clearByPassList();
                    for (String host: StringUtils.asList(
                            ignoreListField.getText(), ",")) {
                        connector.addByPassHost(host);
                    }
                }
                
                connector.setUseProxy(useProxyCheckBox.isSelected());
                
                setVisible(false);
            }
        });
        
        closeButton = new NbiButton();
        closeButton.setText("&Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                DownloadManager.getInstance().getFinishHandler().criticalExit();
            }
        });
        
        if (useProxyCheckBox.isSelected()) {
            proxyHostField.setEnabled(true);
            proxyPortField.setEnabled(true);
            ignoreListField.setEnabled(true);
        } else {
            proxyHostField.setEnabled(false);
            proxyPortField.setEnabled(false);
            ignoreListField.setEnabled(false);
        }
        
        add(messagePane, new GridBagConstraints(
                0, 0,                             // x, y
                3, 1,                             // width, height
                1.0, 0.0,                         // weight-x, weight-y
                GridBagConstraints.LINE_START,    // anchor
                GridBagConstraints.HORIZONTAL,    // fill
                new Insets(11, 11, 0, 11),        // padding
                0, 0));                           // padx, pady - ???
        
        add(useProxyCheckBox, new GridBagConstraints(
                0, 1,                             // x, y
                3, 1,                             // width, height
                1.0, 0.0,                         // weight-x, weight-y
                GridBagConstraints.LINE_START,    // anchor
                GridBagConstraints.HORIZONTAL,    // fill
                new Insets(11, 11, 0, 11),        // padding
                0, 0));                           // padx, pady - ???
        
        add(proxyHostLabel, new GridBagConstraints(
                0, 2,                             // x, y
                1, 1,                             // width, height
                0.0, 0.0,                         // weight-x, weight-y
                GridBagConstraints.LINE_START,    // anchor
                GridBagConstraints.HORIZONTAL,    // fill
                new Insets(6, 11, 0, 0),          // padding
                0, 0));                           // padx, pady - ???
        
        add(proxyHostField, new GridBagConstraints(
                1, 2,                             // x, y
                2, 1,                             // width, height
                1.0, 0.0,                         // weight-x, weight-y
                GridBagConstraints.LINE_START,    // anchor
                GridBagConstraints.HORIZONTAL,    // fill
                new Insets(6, 6, 0, 11),          // padding
                0, 0));                           // padx, pady - ???
        
        add(proxyPortLabel, new GridBagConstraints(
                0, 3,                             // x, y
                1, 1,                             // width, height
                0.0, 0.0,                         // weight-x, weight-y
                GridBagConstraints.LINE_START,    // anchor
                GridBagConstraints.HORIZONTAL,    // fill
                new Insets(6, 11, 0, 0),          // padding
                0, 0));                           // padx, pady - ???
        
        add(proxyPortField, new GridBagConstraints(
                1, 3,                             // x, y
                2, 1,                             // width, height
                1.0, 0.0,                         // weight-x, weight-y
                GridBagConstraints.LINE_START,    // anchor
                GridBagConstraints.HORIZONTAL,    // fill
                new Insets(6, 6, 0, 11),          // padding
                0, 0));                           // padx, pady - ???
        
        add(ignoreListLabel, new GridBagConstraints(
                0, 4,                             // x, y
                1, 1,                             // width, height
                0.0, 0.0,                         // weight-x, weight-y
                GridBagConstraints.LINE_START,    // anchor
                GridBagConstraints.HORIZONTAL,    // fill
                new Insets(6, 11, 0, 0),          // padding
                0, 0));                           // padx, pady - ???
        
        add(ignoreListField, new GridBagConstraints(
                1, 4,                             // x, y
                2, 1,                             // width, height
                1.0, 0.0,                         // weight-x, weight-y
                GridBagConstraints.LINE_START,    // anchor
                GridBagConstraints.HORIZONTAL,    // fill
                new Insets(6, 6, 0, 11),          // padding
                0, 0));                           // padx, pady - ???
        
        add(applyButton, new GridBagConstraints(
                1, 5,                             // x, y
                1, 1,                             // width, height
                1.0, 1.0,                         // weight-x, weight-y
                GridBagConstraints.SOUTHEAST,     // anchor
                GridBagConstraints.NONE,          // fill
                new Insets(17, 11, 11, 0),        // padding
                0, 0));                           // padx, pady - ???
        add(closeButton, new GridBagConstraints(
                2, 5,                             // x, y
                1, 1,                             // width, height
                0.0, 1.0,                         // weight-x, weight-y
                GridBagConstraints.SOUTHEAST,     // anchor
                GridBagConstraints.NONE,          // fill
                new Insets(17, 6, 11, 11),        // padding
                0, 0));                           // padx, pady - ???
    }
}
