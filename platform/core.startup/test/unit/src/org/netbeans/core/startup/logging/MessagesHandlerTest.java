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
