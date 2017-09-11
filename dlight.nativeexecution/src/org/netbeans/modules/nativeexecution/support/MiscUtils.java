/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.support;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification;
import org.openide.util.NbBundle;

public class MiscUtils {
    private static boolean wasShown = false;

    public static boolean isJSCHTooLongException(Exception ex) {
        final String message = "Received message is too long: "; //NOI18N
        
        boolean jschEx = ex instanceof JSchException || ex.getCause() instanceof JSchException;
        boolean longMessage = ex.getMessage().contains(message) || (ex.getCause() != null && ex.getCause().getMessage().contains(message));
        return jschEx && longMessage;
    }

    public static void showJSCHTooLongNotification(String envName) {
        String title = NbBundle.getMessage(MiscUtils.class, "JSCHReceivedMessageIsTooLong.error.title", envName);
        String shortText = NbBundle.getMessage(MiscUtils.class, "JSCHReceivedMessageIsTooLong.error.shorttext");
        String details = NbBundle.getMessage(MiscUtils.class, "JSCHReceivedMessageIsTooLong.error.text");
        showNotification(title, shortText, details);
    }
    
    public static void showNotification(String title, String shortText, String longText) {
        if (wasShown) {
            return;
        }
        wasShown = true;
        NativeExecutionUserNotification.getDefault().showErrorNotification(title, shortText, longText);
    }
    
    /**
     * If an sftp exception occurs, a question arises, whether we should call ChannelSftp.quit().
     * On one hane, *not* calling quit can lead to infinite 4: errors.
     * On the other hand, there might be quite standard situations like file not exist or permission denied,
     * which do not require quitting the channel.
     * This method distinguishes the former and the latter cases.
     */
    public static boolean mightBrokeSftpChannel(SftpException e) {
        // Or should it be just  return e.id == ChannelSftp.SSH_FX_FAILURE ?
        // well, let's be conservative
        return e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE && e.id != ChannelSftp.SSH_FX_PERMISSION_DENIED;
    }

    public static List<String> getMessageAsList(Throwable ex) {
        String msg = ex.getMessage();
        if (msg == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(msg.split("\n")); //NOI18N
        }
    }
    
    public static boolean isDebugged() {
        return DebugChecker.DEBUGGED;
    }
    
    private static class DebugChecker {

        public static final boolean DEBUGGED = checkIfDebugged();

        private static boolean checkIfDebugged() {
            try {
                RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
                List<String> args = runtime.getInputArguments();
                for (String arg : args) {
                    if ("-Xdebug".equals(arg)) { // NOI18N
                        return true;                        
                    } else if ("-agentlib:jdwp".equals(arg)) { // NOI18N
                        // The idea of checking -agentlib:jdwp 
                        // is taken from org.netbeans.modules.sampler.InternalSampler
                        return true;
                    } else if (arg.startsWith("-agentlib:jdwp=")) { // NOI18N
                        return true;
                    }
                }
            } catch (SecurityException ex) {                
            }
            return false;
        }
    }    
}
