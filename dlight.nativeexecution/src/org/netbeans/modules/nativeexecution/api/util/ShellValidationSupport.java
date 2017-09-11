/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.util.Shell.ShellType;
import org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification;;
//import org.netbeans.modules.dlight.nativeexecution.ui.ShellValidationStatusPanel;
//import org.openide.DialogDescriptor;
//import org.openide.DialogDisplayer;
//import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author ak119685
 */
public final class ShellValidationSupport {
    
    protected final static ShellValidationStatus NOSHELL = new ShellValidationStatus(null, Arrays.asList("No shell"), null); // NOI18N
    protected final static ShellValidationStatus VALID = new ShellValidationStatus(null, null, null);

    private ShellValidationSupport() {
    }

    public static ShellValidationStatus getValidationStatus(final Shell shell) {

        if (shell == null) {
            return NOSHELL;
        }

        if (shell.type == ShellType.CYGWIN) {
            return validateCygwinShell(shell);
        }

        return VALID;
    }

    private static ShellValidationStatus validateCygwinShell(final Shell shell) {
        assert shell != null && shell.type == ShellType.CYGWIN;

        ArrayList<String> validationErrors = new ArrayList<>();
        ArrayList<String> validationWarnings = new ArrayList<>();

        String jdkBitness = System.getProperty("os.arch"); // NOI18N
        if (jdkBitness == null) {
            jdkBitness = ""; // NOI18N
        }

        String cygwinBitness = "";
        File uname_exe = new File(shell.bindir, "uname.exe"); // NOI18N
        if (uname_exe.exists()) {
            // Should not use NativeProcess here as it needs expander, which is
            // not available yet ...
            ProcessUtils.ExitStatus exitStatus = ProcessUtils.execute(new ProcessBuilder(uname_exe.getPath(), "-m")); // NOI18N
            if (exitStatus.isOK()) {
                cygwinBitness = exitStatus.getOutputString().trim();
            }
        }

        if (cygwinBitness.equals("i686") && !jdkBitness.equals("x86")) { // NOI18N
            validationWarnings.add(NbBundle.getMessage(ShellValidationSupport.class, "ShellValidationSupport.ValidationWarning.Cygwin32OnJDK64")); // NOI18N
        } else if (cygwinBitness.equals("x86_64") && jdkBitness.equals("x86")) { // NOI18N
            validationWarnings.add(NbBundle.getMessage(ShellValidationSupport.class, "ShellValidationSupport.ValidationWarning.Cygwin64OnJDK32")); // NOI18N
        }

        File mount_util = new File(shell.bindir, "mount.exe"); // NOI18N
        File cygpath_util = new File(shell.bindir, "cygpath.exe"); // NOI18N

        if (!mount_util.exists()) {
            validationErrors.add(loc("ShellValidationSupport.ValidationError.fileNotFound", mount_util.getAbsolutePath())); // NOI18N
        }

        if (!cygpath_util.exists()) {
            validationErrors.add(loc("ShellValidationSupport.ValidationError.fileNotFound", cygpath_util.getAbsolutePath())); // NOI18N
        }

        ProcessBuilder pb = new ProcessBuilder(mount_util.getAbsolutePath());
        ProcessUtils.ExitStatus exitStatus = ProcessUtils.execute(pb);
        List<String> output = exitStatus.getOutputLines();
        if (!exitStatus.isOK()) {
            validationErrors.add(loc("ShellValidationSupport.ValidationError.validationFailed", shell.bindir.getAbsolutePath())); // NOI18N
        }

        Pattern pattern = Pattern.compile("(.*) on (/.*) type .*"); // NOI18N

        boolean rootIsMounted = false;

        for (String line : output) {
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
                String winpath = m.group(1);
                File winfile = new File(winpath);
                String cygpath = m.group(2);

                if (cygpath.equals("/")) { // NOI18N
                    rootIsMounted = true;
                }

                if (!winfile.exists()) {
                    validationErrors.add(loc("ShellValidationSupport.ValidationError.absentMountPoint", winpath, cygpath)); // NOI18N
                    continue;
                }

                if (cygpath.startsWith("/usr")) { // NOI18N
                    String p1 = (winfile.getParentFile().getAbsolutePath() + '\\').toLowerCase();
                    String p2 = (shell.bindir.getParentFile().getAbsolutePath() + '\\').toLowerCase();

                    if (!p2.startsWith(p1)) {
                        validationWarnings.add(loc("ShellValidationSupport.ValidationError.wrongMountPoint", winpath, cygpath)); // NOI18N
                        continue;
                    }
                }
            }
        }

        if (!rootIsMounted) {
            validationErrors.add(loc("ShellValidationSupport.ValidationError.rootIsNotMounted")); // NOI18N
        }

        return new ShellValidationStatus(shell, validationErrors, validationWarnings);
    }

    public static boolean confirm(final ShellValidationStatus status) {
        return confirm(null, null, status);
    }

    public static boolean confirm(final String header, final String footer, final ShellValidationStatus status) {
        if (status == null || status == NOSHELL) {
            if (Boolean.getBoolean("nativeexecution.mode.unittest") || "true".equals(System.getProperty("cnd.command.line.utility"))) { // NOI18N
                System.err.println(loc("ShellValidationSupport.ValidationError.NoShell"));
            } else {
                NativeExecutionUserNotification.getDefault().notify(loc("ShellValidationSupport.ValidationError.NoShell"), // NOI18N
                        NativeExecutionUserNotification.Descriptor.ERROR);
//                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
//                        loc("ShellValidationSupport.ValidationError.NoShell"), // NOI18N
//                        NotifyDescriptor.ERROR_MESSAGE));
            }
            return false;
        }

        if (status.isValid() && !status.hasWarnings()) {
            return true;
        }

        final String key = status.shell.toString();

        String allowBroken = NbPreferences.forModule(WindowsSupport.class).get(key, "no"); // NOI18N

        if ("yes".equals(allowBroken)) { // NOI18N
            return true;
        }

        Object response = null;
        if (Boolean.getBoolean("nativeexecution.mode.unittest")) {
            System.err.println(loc("ShellValidationSupport.ValidationError.ErrorDialogTitle", "cygwin"));
            System.err.println(header);
            for (String error : status.shell.getValidationStatus().getErrors()) {
                System.err.println(error);
            }
            System.err.println(footer);
            return true;
        } else {
            return NativeExecutionUserNotification.getDefault().confirmShellStatusValiation(
                    loc("ShellValidationSupport.ValidationError.ErrorDialogTitle", "cygwin"),//NOI18N
                    header, footer, status.shell);
//            final ShellValidationStatusPanel errorPanel = new ShellValidationStatusPanel(header, footer, status.shell);
//
//            final JButton noButton = new JButton("No"); // NOI18N
//            errorPanel.setActionListener(new ActionListener() {
//
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    noButton.setEnabled(!errorPanel.isRememberDecision());
//                }
//            });
//
//            DialogDescriptor dd = new DialogDescriptor(errorPanel,
//                    loc("ShellValidationSupport.ValidationError.ErrorDialogTitle", "cygwin"), // NOI18N
//                    true,
//                    new Object[]{DialogDescriptor.YES_OPTION, noButton},
//                    noButton,
//                    DialogDescriptor.DEFAULT_ALIGN, null, null);
//
//            Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
//            
//            try {
//                dialog.setVisible(true);
//            } catch (Throwable th) {
//                if (!(th.getCause() instanceof InterruptedException)) {
//                    throw new RuntimeException(th);
//                }
//                dd.setValue(DialogDescriptor.CANCEL_OPTION);
//            } finally {
//                dialog.dispose();
//            }
//
//            response = dd.getValue();
//
//            if (response == DialogDescriptor.YES_OPTION && errorPanel.isRememberDecision()) {
//                NbPreferences.forModule(WindowsSupport.class).put(key, "yes"); // NOI18N
//            }
        }

       // return (response == DialogDescriptor.YES_OPTION);
    }

    public static class ShellValidationStatus {

        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final Shell shell;

        private ShellValidationStatus(Shell shell, List<String> errors, List<String> warnings) {
            if (errors != null) {
                this.errors.addAll(errors);
            }

            if (warnings != null) {
                this.warnings.addAll(warnings);
            }

            this.shell = shell;
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        public List<String> getWarnings() {
            return Collections.unmodifiableList(warnings);
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(ShellValidationSupport.class, key, params);
    }    
}
