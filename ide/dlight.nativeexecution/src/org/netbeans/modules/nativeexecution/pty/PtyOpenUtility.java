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
package org.netbeans.modules.nativeexecution.pty;

import java.io.IOException;
import java.io.InputStream;
import org.netbeans.modules.nativeexecution.api.util.HelperUtility;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
final class PtyOpenUtility extends HelperUtility {

    private static final PtyOpenUtility instance = new PtyOpenUtility();

    private PtyOpenUtility() {
        super("bin/nativeexecution/$osname-${platform}$_isa/pty_open"); // NOI18N
    }

    public static PtyOpenUtility getInstance() {
        return instance;
    }

    PtyInfo readSatelliteOutput(InputStream input) {
        int pid = -1;
        String tty = null;

        boolean stop = false;
        int state = 0;
        StringBuilder sb = new StringBuilder();

        try {
            while (!stop) {
                char c = (char) input.read();
                if (c == 0xFFFF) {
                    break;
                }

                switch (c) {
                    case '\n':
                        if (state == 1) {
                            pid = Integer.parseInt(sb.toString());
                            state++;
                        } else if (state == 2) {
                            tty = sb.toString();
                            state++;
                        } else {
                            stop = true;
                        }
                        break;
                    case 'P':
                        state = 1;
                        break;
                    case 'T':
                        state = 2;
                        break;
                    case ' ':
                        sb.setLength(0);
                        break;
                    default:
                        sb.append(c);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (tty == null || pid < 0) {
            return null;
        }
        
        return new PtyInfo(pid, tty);
    }

    static final class PtyInfo {

        final int pid;
        final String tty;

        public PtyInfo(int pid, String tty) {
            this.pid = pid;
            this.tty = tty;
        }
    }
}
