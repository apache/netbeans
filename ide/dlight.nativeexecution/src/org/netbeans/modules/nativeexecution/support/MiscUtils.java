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
