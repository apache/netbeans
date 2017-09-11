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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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

    final static class PtyInfo {

        final int pid;
        final String tty;

        public PtyInfo(int pid, String tty) {
            this.pid = pid;
            this.tty = tty;
        }
    }
}
