/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.netbeans.modules.javaee.wildfly.ide;

import org.netbeans.junit.NbTestCase;

public class WildflyStartLineParserTest extends NbTestCase {

    public WildflyStartLineParserTest(String testName) {
        super(testName);
    }
    
    public void testEap6StartingSingleMajorMinorVersion() {
        boolean result = WildflyStartLineParser.isStarting("14:56:23,970 INFO  [org.jboss.as] (MSC service thread 1-5) JBAS015899: JBoss EAP 6.0.GA (AS 7.5.23.Final-redhat-SNAPSHOT) startet");
        assertTrue(result);
    }
    
    public void testEap6StartingSingleMajorDoubleMinorVersion() {
        boolean result = WildflyStartLineParser.isStarting("14:56:23,970 INFO  [org.jboss.as] (MSC service thread 1-5) JBAS015899: JBoss EAP 6.14.GA (AS 7.5.23.Final-redhat-SNAPSHOT) startet");
        assertTrue(result);
    }
    
    public void testEap6StartingSingleMajorMinorPatchVersion() {
        boolean result = WildflyStartLineParser.isStarting("14:56:23,970 INFO  [org.jboss.as] (MSC service thread 1-5) JBAS015899: JBoss EAP 6.4.2.GA (AS 7.5.23.Final-redhat-SNAPSHOT) startet");
        assertTrue(result);
    }
    
    public void testEap6StartingSingleMajorMinorDoublePatchVersion() {
        boolean result = WildflyStartLineParser.isStarting("14:56:23,970 INFO  [org.jboss.as] (MSC service thread 1-5) JBAS015899: JBoss EAP 6.4.23.GA (AS 7.5.23.Final-redhat-SNAPSHOT) startet");
        assertTrue(result);
    }
    
    public void testEap6StartingSingleMajorDoubleMinorSinglePatchVersion() {
        boolean result = WildflyStartLineParser.isStarting("14:56:23,970 INFO  [org.jboss.as] (MSC service thread 1-5) JBAS015899: JBoss EAP 6.14.2.GA (AS 7.5.23.Final-redhat-SNAPSHOT) startet");
        assertTrue(result);
    }
    
    public void testEap6StartingSingleMajorDoubleMinorPatchVersion() {
        boolean result = WildflyStartLineParser.isStarting("14:56:23,970 INFO  [org.jboss.as] (MSC service thread 1-5) JBAS015899: JBoss EAP 6.14.23.GA (AS 7.5.23.Final-redhat-SNAPSHOT) startet");
        assertTrue(result);
    }
    
    public void testEap6StartedSingleMajorMinorVersion() {
        boolean result = WildflyStartLineParser.isStarted("14:46:48,572 INFO  [org.jboss.as] (Controller Boot Thread) JBAS015874: JBoss EAP 6.0.GA (AS 7.5.23.Final-redhat-SNAPSHOT) wurde gestartet in 73289ms - 3187 von 3223 Diensten gestartet (60 Services sind \"lazy\", passiv oder werden bei Bedarf geladen)");
        assertTrue(result);
    }
    
    public void testEap6StartedSingleMajorDoubleMinorVersion() {
        boolean result = WildflyStartLineParser.isStarted("14:46:48,572 INFO  [org.jboss.as] (Controller Boot Thread) JBAS015874: JBoss EAP 6.14.GA (AS 7.5.23.Final-redhat-SNAPSHOT) wurde gestartet in 73289ms - 3187 von 3223 Diensten gestartet (60 Services sind \"lazy\", passiv oder werden bei Bedarf geladen)");
        assertTrue(result);
    }
    
    public void testEap6StartedSingleMajorMinorPatchVersion() {
        boolean result = WildflyStartLineParser.isStarted("14:46:48,572 INFO  [org.jboss.as] (Controller Boot Thread) JBAS015874: JBoss EAP 6.4.2.GA (AS 7.5.23.Final-redhat-SNAPSHOT) wurde gestartet in 73289ms - 3187 von 3223 Diensten gestartet (60 Services sind \"lazy\", passiv oder werden bei Bedarf geladen)");
        assertTrue(result);
    }
    
    public void testEap6StartedSingleMajorMinorDoublePatchVersion() {
        boolean result = WildflyStartLineParser.isStarted("14:46:48,572 INFO  [org.jboss.as] (Controller Boot Thread) JBAS015874: JBoss EAP 6.4.23.GA (AS 7.5.23.Final-redhat-SNAPSHOT) wurde gestartet in 73289ms - 3187 von 3223 Diensten gestartet (60 Services sind \"lazy\", passiv oder werden bei Bedarf geladen)");
        assertTrue(result);
    }
    
    public void testEap6StartedSingleMajorDoubleMinorSinglePatchVersion() {
        boolean result = WildflyStartLineParser.isStarted("14:46:48,572 INFO  [org.jboss.as] (Controller Boot Thread) JBAS015874: JBoss EAP 6.14.2.GA (AS 7.5.23.Final-redhat-SNAPSHOT) wurde gestartet in 73289ms - 3187 von 3223 Diensten gestartet (60 Services sind \"lazy\", passiv oder werden bei Bedarf geladen)");
        assertTrue(result);
    }
    
    public void testEap6StartedSingleMajorDoubleMinorPatchVersion() {
        boolean result = WildflyStartLineParser.isStarted("14:46:48,572 INFO  [org.jboss.as] (Controller Boot Thread) JBAS015874: JBoss EAP 6.14.23.GA (AS 7.5.23.Final-redhat-SNAPSHOT) wurde gestartet in 73289ms - 3187 von 3223 Diensten gestartet (60 Services sind \"lazy\", passiv oder werden bei Bedarf geladen)");
        assertTrue(result);
    }
    
    public void testEap7StartingSingleMajorMinorVersion() {
        boolean result = WildflyStartLineParser.isStarting("14:49:31,335 INFO  [org.jboss.as] (MSC service thread 1-2) WFLYSRV0049: JBoss EAP 7.4.GA (WildFly Core 15.0.6.Final-redhat-00003) wird gestartet");
        assertTrue(result);
    }
    
    public void testEap7StartingSingleMajorDoubleMinorVersion() {
        boolean result = WildflyStartLineParser.isStarting("14:49:31,335 INFO  [org.jboss.as] (MSC service thread 1-2) WFLYSRV0049: JBoss EAP 7.14.GA (WildFly Core 15.0.6.Final-redhat-00003) wird gestartet");
        assertTrue(result);
    }
    
    public void testEap7StartingSingleMajorMinorPatchVersion() {
        boolean result = WildflyStartLineParser.isStarting("14:49:31,335 INFO  [org.jboss.as] (MSC service thread 1-2) WFLYSRV0049: JBoss EAP 7.4.3.GA (WildFly Core 15.0.6.Final-redhat-00003) wird gestartet");
        assertTrue(result);
    }
    
    public void testEap7StartingSingleMajorMinorDoublePatchVersion() {
        boolean result = WildflyStartLineParser.isStarting("14:49:31,335 INFO  [org.jboss.as] (MSC service thread 1-2) WFLYSRV0049: JBoss EAP 7.4.13.GA (WildFly Core 15.0.6.Final-redhat-00003) wird gestartet");
        assertTrue(result);
    }
    
    public void testEap7StartingSingleMajorDoubleMinorSinglePatchVersion() {
        boolean result = WildflyStartLineParser.isStarting("14:49:31,335 INFO  [org.jboss.as] (MSC service thread 1-2) WFLYSRV0049: JBoss EAP 7.14.3.GA (WildFly Core 15.0.6.Final-redhat-00003) wird gestartet");
        assertTrue(result);
    }
    
    public void testEap7StartingSingleMajorDoubleMinorPatchVersion() {
        boolean result = WildflyStartLineParser.isStarting("14:49:31,335 INFO  [org.jboss.as] (MSC service thread 1-2) WFLYSRV0049: JBoss EAP 7.14.13.GA (WildFly Core 15.0.6.Final-redhat-00003) wird gestartet");
        assertTrue(result);
    }
    
     public void testEap7StartedSingleMajorMinorVersion() {
        boolean result = WildflyStartLineParser.isStarted("14:50:17,938 ERROR [org.jboss.as] (Controller Boot Thread) WFLYSRV0025: JBoss EAP 7.4.GA (WildFly Core 15.0.6.Final-redhat-00003) wurde in 50723 ms gestartet – 353 von 595 Diensten gestartet (347 Dienste sind verzögert, passiv oder werden bei Bedarf geladen)");
        assertTrue(result);
    }
    
    public void testEap7StartedSingleMajorDoubleMinorVersion() {
        boolean result = WildflyStartLineParser.isStarted("14:50:17,938 ERROR [org.jboss.as] (Controller Boot Thread) WFLYSRV0025: JBoss EAP 7.14.GA (WildFly Core 15.0.6.Final-redhat-00003) wurde in 50723 ms gestartet – 353 von 595 Diensten gestartet (347 Dienste sind verzögert, passiv oder werden bei Bedarf geladen)");
        assertTrue(result);
    }
    
    public void testEap7StartedSingleMajorMinorPatchVersion() {
        boolean result = WildflyStartLineParser.isStarted("14:50:17,938 ERROR [org.jboss.as] (Controller Boot Thread) WFLYSRV0025: JBoss EAP 7.4.3.GA (WildFly Core 15.0.6.Final-redhat-00003) wurde in 50723 ms gestartet – 353 von 595 Diensten gestartet (347 Dienste sind verzögert, passiv oder werden bei Bedarf geladen)");
        assertTrue(result);
    }
    
    public void testEap7StartedSingleMajorMinorDoublePatchVersion() {
        boolean result = WildflyStartLineParser.isStarted("14:50:17,938 ERROR [org.jboss.as] (Controller Boot Thread) WFLYSRV0025: JBoss EAP 7.4.13.GA (WildFly Core 15.0.6.Final-redhat-00003) wurde in 50723 ms gestartet – 353 von 595 Diensten gestartet (347 Dienste sind verzögert, passiv oder werden bei Bedarf geladen)");
        assertTrue(result);
    }
    
    public void testEap7StartedSingleMajorDoubleMinorSinglePatchVersion() {
        boolean result = WildflyStartLineParser.isStarted("14:50:17,938 ERROR [org.jboss.as] (Controller Boot Thread) WFLYSRV0025: JBoss EAP 7.14.3.GA (WildFly Core 15.0.6.Final-redhat-00003) wurde in 50723 ms gestartet – 353 von 595 Diensten gestartet (347 Dienste sind verzögert, passiv oder werden bei Bedarf geladen)");
        assertTrue(result);
    }
    
    public void testEap7StartedSingleMajorDoubleMinorPatchVersion() {
        boolean result = WildflyStartLineParser.isStarted("14:50:17,938 ERROR [org.jboss.as] (Controller Boot Thread) WFLYSRV0025: JBoss EAP 7.14.13.GA (WildFly Core 15.0.6.Final-redhat-00003) wurde in 50723 ms gestartet – 353 von 595 Diensten gestartet (347 Dienste sind verzögert, passiv oder werden bei Bedarf geladen)");
        assertTrue(result);
    }
}


