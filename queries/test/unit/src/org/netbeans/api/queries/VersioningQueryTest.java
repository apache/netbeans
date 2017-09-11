/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.api.queries;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.api.queries.SharabilityQuery.Sharability;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.netbeans.spi.queries.VersioningQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Tomas Stupka
 */
public class VersioningQueryTest extends NbTestCase {
    
    public VersioningQueryTest(String testMethod) {
        super (testMethod);
    }
    
    private final File home = new File(System.getProperty("user.dir"));
    
    @Override
    public void setUp() throws IOException {
        clearWorkDir();
        MockServices.setServices(VersioningQueryImplementationImpl.class);
    }
    
    public void testIsManaged() throws IOException {
        File file = new File(home, "aFile.vcs");
        assertTrue(VersioningQuery.isManaged(BaseUtilities.toURI(file)));
    }
    
    public void testIsNotManaged() throws IOException {
        File file = new File(home, "aFile.txt");
        assertFalse(VersioningQuery.isManaged(BaseUtilities.toURI(file)));
    }
    
    public void testGetRemoteLocation() throws IOException {
        File file = new File(home, "aFile.vcs");
        assertEquals(BaseUtilities.toURI(file).toString(), VersioningQuery.getRemoteLocation(BaseUtilities.toURI(file)));
    }
    
    public void testNoRemoteLocation() throws IOException {
        File file = new File(home, "aFile.txt");
        assertNull(VersioningQuery.getRemoteLocation(BaseUtilities.toURI(file)));
    }
    
    public void testNormalized() throws IOException {
        File file = new File(home, "../aFile.txt");
        Exception exception = null;
        try {
            VersioningQuery.isManaged(BaseUtilities.toURI(file));
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
        URI uri = BaseUtilities.toURI(file);
        exception = null;
        try {
            VersioningQuery.getRemoteLocation(BaseUtilities.toURI(file));
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    public static class VersioningQueryImplementationImpl implements VersioningQueryImplementation {

        @Override
        public boolean isManaged(URI uri) {
            File file = BaseUtilities.toFile(uri);
            String path = file.getAbsolutePath();
            return path.endsWith(".vcs");
        }

        @Override
        public String getRemoteLocation(URI uri) {
            File file = BaseUtilities.toFile(uri);
            String path = file.getAbsolutePath();
            return path.endsWith(".vcs") ? uri.toString() : null;
        }
    }

}
