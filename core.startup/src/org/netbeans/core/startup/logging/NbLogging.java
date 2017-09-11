/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.core.startup.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class NbLogging {
    /** stream to send debug messages from logging to */
    public static final PrintStream DEBUG;
    
    
    static final Pattern unwantedMessages;
    static {
        PrintStream _D = null;
        String uMS = System.getProperty("TopLogging.unwantedMessages"); // NOI18N
        if (uMS != null || Boolean.getBoolean("TopLogging.DEBUG")) { // NOI18N
            try {
                File debugLog = new File(System.getProperty("java.io.tmpdir"), "TopLogging.log"); // NOI18N
                System.err.println("Logging sent to: " + debugLog); // NOI18N
                _D = new PrintStream(new FileOutputStream(debugLog), true);
            } catch (FileNotFoundException x) {
                x.printStackTrace();
            }
        }
        DEBUG = _D;
        Pattern uMP = null;
        if (uMS != null) {
            try {
                uMP = Pattern.compile(uMS);
                DEBUG.println("On the lookout for log messages matching: " + uMS); // NOI18N
            } catch (PatternSyntaxException x) {
                x.printStackTrace();
            }
        }
        unwantedMessages = uMP;
    }

    /** @return true if the message is wanted */
    public static boolean wantsMessage(String s) {
        return unwantedMessages == null || !unwantedMessages.matcher(s).find();
    }

    /** Factory to create non-closing, dispatch handler.
     */
    public static Handler createDispatchHandler(Handler handler, int flushDelay) {
        return new DispatchingHandler(handler, flushDelay);
    }
    
    /** Factory that creates <em>messages.log</em> handler in provided directory.
     * @param dir directory to store logs in
     */
    public static Handler createMessagesHandler(File dir) {
        return new MessagesHandler(dir);
    }

    /** Does its best to close provided handler. Can close handlers created by
     * {@link #createDispatchHandler(java.util.logging.Handler, int)} as well.
     */
    public static void close(Handler h) {
        if (h == null) {
            return;
        }
        if (h instanceof DispatchingHandler) {
            ((DispatchingHandler)h).doClose();
        } else {
            h.close();
        }
    }
}
