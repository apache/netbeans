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

package djava.security;

import java.security.MessageDigest;
import org.netbeans.performance.Benchmark;

// Results on a P6/1200 JDK 1.4.1rc for MD5 (rough numbers; magnitude is in bytes):
//  100 - 10us
// 1000 - 45us
// For SHA-1:
//  100 - 10-15us
// 1000 - 70-75us

/**
 * Benchmark measuring SHA-1 digestion.
 *
 * @author Jesse Glick
 */
public class MessageDigestTest extends Benchmark {

    public static void main(String[] args) {
        simpleRun(MessageDigestTest.class);
    }

    public MessageDigestTest(String name) {
        super(name, new Integer[] {new Integer(100), new Integer(1000), new Integer(10000)});
    }
    
    private byte[] buf;
    private MessageDigest dig;
    protected void setUp() throws Exception {
        int magnitude = ((Integer)getArgument()).intValue();
        buf = new byte[magnitude];
        for (int i = 0; i < magnitude; i++) {
            buf[i] = (byte)i;
        }
        dig = MessageDigest.getInstance("SHA-1");
    }
    
    public void testSHA1() throws Exception {
        int count = getIterationCount();
        for (int i = 0; i < count; i++) {
            dig.reset();
            dig.digest(buf);
        }
    }
    
}
