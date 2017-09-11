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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static org.netbeans.core.startup.logging.MessagesHandler.MAX_REPEAT_COUNT_FLUSH;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MessagesHandlerTest extends NbTestCase {
    
    public MessagesHandlerTest(String s) {
        super(s);
    }
    
    public void testLimitOfMessagesLogFiles() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
        File logs = new File(getWorkDir(), "logs");
        logs.mkdirs();
        
        MessagesHandler mh = new MessagesHandler(logs, 2, 16);
        
        mh.publish(new LogRecord(Level.INFO, "Hi"));
        mh.flush();
        assertEquals("One file", 1, logs.list().length);
        mh.publish(new LogRecord(Level.INFO, "Message that is longer than 16 bytes"));
        mh.flush();
        assertEquals("Two files", 2, logs.list().length);
        
        mh.publish(new LogRecord(Level.INFO, "Hello!"));
        File ml = new File(logs, "messages.log");
        mh.flush();
        String msg = readLog(ml);
        
        if (msg.indexOf("Hello!") == -1) {
            fail("Contains the Hello! message:\n" + msg);
        }
        if (msg.indexOf("16 bytes!") != -1) {
            fail("Contains no '16 bytes' message:\n" + msg);
        }
    }
    
    public void testMessageRepeat() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
        File logs = new File(getWorkDir(), "logs");
        logs.mkdirs();
        
        MessagesHandler mh = new MessagesHandler(logs, 2, 10000);
        
        mh.publish(new LogRecord(Level.INFO, "Hi"));
        mh.publish(new LogRecord(Level.INFO, "Hello"));
        mh.publish(new LogRecord(Level.INFO, "Hello"));
        mh.flush();
        File log = logs.listFiles()[0];
        List<String> allLines = Files.readAllLines(log.toPath(), Charset.defaultCharset());
        assertTrue(allLines.get(allLines.size() - 1), allLines.get(allLines.size() - 1).endsWith(MessagesHandler.getRepeatingMessage(1, 1)));
        assertTrue(allLines.get(allLines.size() - 2), allLines.get(allLines.size() - 2).endsWith("Hello"));
        assertTrue(allLines.get(allLines.size() - 3), allLines.get(allLines.size() - 3).endsWith("Hi"));
        
        mh.publish(new LogRecord(Level.INFO, "Hello"));
        mh.publish(new LogRecord(Level.INFO, "Hello"));
        mh.flush();
        allLines = Files.readAllLines(log.toPath(), Charset.defaultCharset());
        assertTrue(allLines.get(allLines.size() - 1), allLines.get(allLines.size() - 1).endsWith(MessagesHandler.getRepeatingMessage(2, 2)));
    }
    
    public void testMessageAllRepeat() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
        File logs = new File(getWorkDir(), "logs");
        logs.mkdirs();
        
        MessagesHandler mh = new MessagesHandler(logs, 2, 10000);
        
        mh.publish(new LogRecord(Level.INFO, "Hi"));
        mh.publish(new LogRecord(Level.INFO, "Hello"));
        int repeats = 1;
        for (; repeats <= MAX_REPEAT_COUNT_FLUSH/2; repeats++) {
            mh.publish(new LogRecord(Level.INFO, "Hello"));
        }
        mh.flush();
        for (; repeats <= (MAX_REPEAT_COUNT_FLUSH+1); repeats++) {
            mh.publish(new LogRecord(Level.INFO, "Hello"));
        }
        mh.flush();
        for (; repeats <= (MAX_REPEAT_COUNT_FLUSH+100); repeats++) {
            mh.publish(new LogRecord(Level.INFO, "Hello"));
        }
        mh.flush();
        File log = logs.listFiles()[0];
        List<String> allLines = Files.readAllLines(log.toPath(), Charset.defaultCharset());
        //assertTrue(allLines.get(allLines.size() - 3), allLines.get(allLines.size() - 3).endsWith());
        assertTrue(allLines.get(allLines.size() - 1), allLines.get(allLines.size() - 1).endsWith(MessagesHandler.getRepeatingMessage(MAX_REPEAT_COUNT_FLUSH+1-MAX_REPEAT_COUNT_FLUSH/2, MAX_REPEAT_COUNT_FLUSH+1)));
        assertTrue(allLines.get(allLines.size() - 2), allLines.get(allLines.size() - 2).endsWith(MessagesHandler.getRepeatingMessage(MAX_REPEAT_COUNT_FLUSH/2, MAX_REPEAT_COUNT_FLUSH/2)));
        assertTrue(allLines.get(allLines.size() - 3), allLines.get(allLines.size() - 3).endsWith("Hello"));
        assertTrue(allLines.get(allLines.size() - 4), allLines.get(allLines.size() - 4).endsWith("Hi"));
        
        mh.publish(new LogRecord(Level.INFO, "Hello2"));
        mh.flush();
        allLines = Files.readAllLines(log.toPath(), Charset.defaultCharset());
        assertTrue(allLines.get(allLines.size() - 2), allLines.get(allLines.size() - 2).endsWith(MessagesHandler.getAllRepeatsMessage(repeats)));
    }
    
    private String readLog(File log) throws IOException {
        DataInputStream is = new DataInputStream(new FileInputStream(log));

        byte[] arr = new byte[(int) log.length()];
        is.readFully(arr);
        is.close();

        return new String(arr);
    }
    
}
