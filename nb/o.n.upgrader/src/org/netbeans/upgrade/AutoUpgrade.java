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

package org.netbeans.upgrade;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.netbeans.util.Util;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;

/**
 * NetBeans configuration migration.
 * 
 * <p>Copies some files of the old user dir to the new user dir.
 *
 * @author  Jiri Rechtacek, Jiri Skrivanek
 */
public final class AutoUpgrade {

    private static final Logger LOGGER = Logger.getLogger(AutoUpgrade.class.getName());

    public static void main(String[] args) throws Exception {
        File sourceFolder = findPreviousUserDir(APACHE_VERSION_TO_CHECK);
        if (sourceFolder != null) {
            if (!showUpgradeDialog(sourceFolder)) {
                throw new UserCancelException();
            }
        }
    }

    static final Comparator<String> APACHE_VERSION_COMPARATOR = (v1, v2) -> new SpecificationVersion(v1).compareTo(new SpecificationVersion(v2));
    
    static final List<String> APACHE_VERSION_TO_CHECK =
            Stream.of(NbBundle.getMessage(AutoUpgrade.class, "apachenetbeanspreviousversion")
                  .split(",")).sorted(APACHE_VERSION_COMPARATOR.reversed()).collect(Collectors.toList());
    
    private static File findPreviousUserDir(final List<String> versionsToCheck) {
        String defaultUserdirRoot = System.getProperty("netbeans.default_userdir_root"); // NOI18N
        if (defaultUserdirRoot != null) {
            File userHomeFile = new File(defaultUserdirRoot);
            for (String ver : versionsToCheck) {
                File sourceFolder = new File(userHomeFile.getAbsolutePath(), ver);
                if (sourceFolder.exists() && sourceFolder.isDirectory()) {
                    return sourceFolder;
                }
            }
        }
        return null;
    }
    
    private static boolean showUpgradeDialog(final File source) {
        Util.setDefaultLookAndFeel();

	JPanel panel = new JPanel(new BorderLayout());
	panel.add(new AutoUpgradePanel(source.getAbsolutePath()), BorderLayout.CENTER);
	JProgressBar progressBar = new JProgressBar(0, 100);
	progressBar.setValue(0);
	progressBar.setStringPainted(true);
	progressBar.setIndeterminate(true);
	panel.add(progressBar, BorderLayout.SOUTH);
	progressBar.setVisible(false);
	
	JButton bYES = new JButton("Yes");
	bYES.setMnemonic(KeyEvent.VK_Y);
	JButton bNO = new JButton("No");
	bNO.setMnemonic(KeyEvent.VK_N);
	JButton[] options = new JButton[] {bYES, bNO};
        JOptionPane p = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, bYES);
        JDialog d = Util.createJOptionProgressDialog(p, NbBundle.getMessage(AutoUpgrade.class, "MSG_Confirmation_Title"), source, progressBar);
        d.setVisible (true);

        return Integer.valueOf(JOptionPane.YES_OPTION).equals (p.getValue ());
    }

    /* Copy files from source folder to current userdir according to include/exclude
     * patterns in etc/netbeans.import file. */
    private static void copyToUserdir(File source) throws IOException, PropertyVetoException {
        File userdir = new File(System.getProperty("netbeans.user", "")); // NOI18N
        File netBeansDir = InstalledFileLocator.getDefault().locate("modules", null, false).getParentFile().getParentFile();  //NOI18N
        File importFile = new File(netBeansDir, "etc/netbeans.import");  //NOI18N
        LOGGER.fine("Import file: " + importFile);
        LOGGER.info("Importing from " + source + " to " + userdir); // NOI18N
        CopyFiles.copyDeep(source, userdir, importFile);
    }

    public static void doCopyToUserDir(File source) throws IOException, PropertyVetoException {
	copyToUserdir(source);
    }
}
