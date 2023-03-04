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
package org.netbeans.modules.nativeexecution.api.execution;

import java.awt.Color;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.swing.UIManager;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.ProcessStatusEx;
import org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer.AbstractDisplayer.Colors;
//import org.netbeans.swing.plaf.LFCustoms;
import org.openide.util.NbBundle;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColors;
import org.openide.windows.InputOutput;

/**
 *
 * @author ak119685
 */
public interface PostMessageDisplayer {

    public String getPostMessage(NativeProcess process, long time);

    public String getPostStatusString(NativeProcess process);

    public String getPostMessage(NativeProcess.State state, int rc, long time);

    public static final class Default implements PostMessageDisplayer2 {

        private final String actionName;

        public Default(String actionName) {
            this.actionName = actionName;
        }

        @Override
        public String getPostMessage(State state, int rc, long timeMilliseconds) {
            StringBuilder res = new StringBuilder();

            switch (state) {
                case ERROR:
                    res.append(NbBundle.getMessage(PostMessageDisplayer.class, "FAILED", actionName.toUpperCase()));
                    break;
                case CANCELLED:
                    res.append(NbBundle.getMessage(PostMessageDisplayer.class, "TERMINATED", actionName.toUpperCase()));
                    break;
                case FINISHED:
                    if (rc == 0) {
                        res.append(NbBundle.getMessage(PostMessageDisplayer.class, "SUCCESSFUL", actionName.toUpperCase()));
                    } else {
                        res.append(NbBundle.getMessage(PostMessageDisplayer.class, "FAILED", actionName.toUpperCase()));
                    }
                    break;
                default:
                // should not happen
                }

            res.append(" ("); // NOI18N

            if (rc != 0) {
                res.append(NbBundle.getMessage(PostMessageDisplayer.class, "EXIT_VALUE", rc));
                res.append(", "); // NOI18N
            }

            res.append(NbBundle.getMessage(PostMessageDisplayer.class, "TOTAL_TIME", formatTime(timeMilliseconds)));

            res.append(')');

            return res.toString();
        }

        @Override
        public String getPostStatusString(NativeProcess process) {
            ProcessStatusEx exitStatusEx = process.getExitStatusEx();

            if (exitStatusEx != null) {
                return NbBundle.getMessage(PostMessageDisplayer.class, "MSG_FINISHED", actionName); // NOI18N
            }

            State state = process.getState();
            switch (state) {
                case ERROR:
                    return NbBundle.getMessage(PostMessageDisplayer.class, "MSG_FAILED", actionName); // NOI18N
                case CANCELLED:
                    return NbBundle.getMessage(PostMessageDisplayer.class, "MSG_TERMINATED", actionName); // NOI18N
                case FINISHED:
                    if (process.exitValue() == 0) {
                        return NbBundle.getMessage(PostMessageDisplayer.class, "MSG_SUCCESSFUL", actionName);
                    } else {
                        return NbBundle.getMessage(PostMessageDisplayer.class, "MSG_FAILED", actionName);
                    }
                default:
                    // should not happen
                    return ""; // NOI18N
            }
        }

        private static String formatTime(long millis) {
            if (millis == 0) {
                return " 0" + NbBundle.getMessage(PostMessageDisplayer.class, "MILLISECOND"); // NOI18N
            }

            StringBuilder res = new StringBuilder();
            long seconds = millis / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;

            if (hours > 0) {
                res.append(" ").append(hours).append(NbBundle.getMessage(PostMessageDisplayer.class, "HOUR")); // NOI18N
            }

            if (minutes > 0) {
                res.append(" ").append(minutes - hours * 60).append(NbBundle.getMessage(PostMessageDisplayer.class, "MINUTE")); // NOI18N
            }

            if (seconds > 0) {
                res.append(" ").append(seconds - minutes * 60).append(NbBundle.getMessage(PostMessageDisplayer.class, "SECOND")); // NOI18N
            } else {
                res.append(" ").append(millis).append(NbBundle.getMessage(PostMessageDisplayer.class, "MILLISECOND")); // NOI18N
            }

            return res.toString();
        }

        @Override
        public String getPostMessage(NativeProcess process, long time) {
            State state = process.getState();
            StringBuilder res = new StringBuilder();
            ProcessStatusEx statusEx = process.getExitStatusEx();
            int status = process.exitValue();

            if (statusEx != null) {
                if (state == State.ERROR) {
                    return NbBundle.getMessage(PostMessageDisplayer.class, "FAILED", actionName.toUpperCase()); // NOI18N
                }

                res.append(NbBundle.getMessage(PostMessageDisplayer.class, "FINISHED", actionName.toUpperCase())); // NOI18N

                res.append("; "); // NOI18N
                if (statusEx.ifExited()) {
                    res.append(NbBundle.getMessage(PostMessageDisplayer.class, "EXIT_VALUE", statusEx.getExitCode())); // NOI18N
                } else {
                    res.append(statusEx.termSignal());
                    if (statusEx.ifCoreDump()) {
                        res.append("; "); // NOI18N
                        res.append(NbBundle.getMessage(PostMessageDisplayer.class, "COREDUMPED")); // NOI18N
                    }
                }

                res.append("; "); // NOI18N
                res.append(NbBundle.getMessage(PostMessageDisplayer.class, "TOTAL_TIME_EX", // NOI18N
                        formatTime(statusEx.realTime(TimeUnit.MILLISECONDS)),
                        formatTime(statusEx.sysTime(TimeUnit.MILLISECONDS)),
                        formatTime(statusEx.usrTime(TimeUnit.MILLISECONDS))));
                return res.toString();

            }

            return getPostMessage(state, status, time);
        }

        @Override
        public void outPostMessage(InputOutput io, NativeProcess process, long time) {
            Color color;
            if (process.getState() == State.ERROR) {
                color = Colors.getColorError(io);
            } else {
                color = process.exitValue() == 0
                        ? Colors.getColorSuccess(io)
                        : Colors.getColorFailure(io);
            }
            try {
                IOColorLines.println(io, "\r", Color.BLACK); // NOI18N
                IOColorLines.println(io, getPostMessage(process, time) + "\r", color); // NOI18N
            } catch (IOException ex) {
            }
        }
    }

    /*
     * @since 1.30.1
     */
    public abstract static class AbstractDisplayer implements PostMessageDisplayer2 {

        private final Default defaultImpl;

        protected AbstractDisplayer(String actionName) {
            defaultImpl = new Default(null);
        }

        @Override
        public void outPostMessage(InputOutput io, NativeProcess process, long time) {
            defaultImpl.outPostMessage(io, process, time);
        }

        @Override
        public String getPostMessage(NativeProcess process, long time) {
            return defaultImpl.getPostMessage(process, time);
        }

        @Override
        public String getPostStatusString(NativeProcess process) {
            return defaultImpl.getPostStatusString(process);
        }

        @Override
        public String getPostMessage(State state, int rc, long time) {
            return defaultImpl.getPostMessage(state, rc, time);
        }

        protected final String formatTime(long millis) {
            return Default.formatTime(millis);
        }
      
        protected static class Colors {

            protected static Color getColorError(InputOutput io) {
                Color color = IOColors.getColor(io, IOColors.OutputType.ERROR);
                if (color == null) {
                    color = UIManager.getColor("nb.output.err.foreground"); // NOI18N
                    if (color == null) {
                       // color = LFCustoms.shiftColor(Color.RED);
                       color = Color.RED;
                    }
                }
                return color;
            }

            protected static Color getColorFailure(InputOutput io) {
                Color color = IOColors.getColor(io, IOColors.OutputType.LOG_FAILURE);
                if (color == null) {
                    color = UIManager.getColor("nb.output.failure.foreground"); // NOI18N
                    if (color == null) {
                        color = Color.RED.darker();
                    }
                }
                return color;
            }

            protected static Color getColorSuccess(InputOutput io) {
                Color color = IOColors.getColor(io, IOColors.OutputType.LOG_SUCCESS);
                if (color == null) {
                    color = UIManager.getColor("nb.output.success.foreground"); // NOI18N
                    if (color == null) {
                        color = Color.GREEN.darker().darker();
                    }
                }
                return color;
            }

            protected static Color getDefaultColorBackground() {
                Color back = UIManager.getColor("nb.output.backgorund");        //NOI18N
                if (back == null) {
                    back = UIManager.getColor("TextField.background");          //NOI18N
                    if (back == null) {
                        back = Color.WHITE;
                    } else if (isNimbus()) {
                        back = new Color(back.getRGB()); // #225829
                    }
                }
                return back;
            }

            static boolean isNimbus() {
                return "Nimbus".equals(UIManager.getLookAndFeel().getID());     //NOI18N
            }
        }
    }
}
