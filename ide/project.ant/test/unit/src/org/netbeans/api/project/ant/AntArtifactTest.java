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

package org.netbeans.api.project.ant;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;

public class AntArtifactTest extends NbTestCase {

    public AntArtifactTest(String n) {
        super(n);
    }

    protected @Override Level logLevel() {
        return Level.WARNING;
    }

    public void testMethodOverride() throws Exception { // #72308
        final URI nowhere = URI.create("nowhere:man");
        class Bogus1 extends AntArtifact {
            @SuppressWarnings("deprecation")
            public @Override URI getArtifactLocation() {
                return nowhere;
            }
            public @Override String getType() {return null;}
            public @Override File getScriptLocation() {return null;}
            public @Override String getTargetName() {return null;}
            public @Override String getCleanTargetName() {return null;}
        }
        CharSequence log = Log.enable(AntArtifact.class.getName(), Level.WARNING);
        assertEquals(Collections.singletonList(nowhere), Arrays.asList(new Bogus1().getArtifactLocations()));
        assertTrue(log.toString(), log.toString().contains(Bogus1.class.getName()));
        class Bogus2 extends AntArtifact {
            public @Override String getType() {return null;}
            public @Override File getScriptLocation() {return null;}
            public @Override String getTargetName() {return null;}
            public @Override String getCleanTargetName() {return null;}
        }
        try {
            new Bogus2().getArtifactLocations();
            fail();
        } catch (IllegalStateException ise) {
            // OK, this is what we want now.
        }
        class OK extends AntArtifact {
            public @Override URI[] getArtifactLocations() {
                return new URI[] {nowhere};
            }
            public @Override String getType() {return null;}
            public @Override File getScriptLocation() {return null;}
            public @Override String getTargetName() {return null;}
            public @Override String getCleanTargetName() {return null;}
        }
        new OK();
    }

}
