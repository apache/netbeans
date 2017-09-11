/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
