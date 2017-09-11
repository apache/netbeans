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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.masterfs.filebasedfs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.LogRecord;
import org.openide.filesystems.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.MasterURLMapper;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

public class MIMESupportLoggingTest extends NbTestCase {
    private TestLookup lookup;
    public MIMESupportLoggingTest(String testName) {
        super(testName);
    }

    static {
        System.setProperty("org.openide.util.Lookup", MIMESupportLoggingTest.TestLookup.class.getName());
        assertEquals(MIMESupportLoggingTest.TestLookup.class, Lookup.getDefault().getClass());
        
    }
    
    protected @Override void setUp() throws Exception {
        lookup = (MIMESupportLoggingTest.TestLookup)Lookup.getDefault();
        lookup.init();
    }


    public void testLogging() throws Exception {
        clearWorkDir();
        MIMESupportLoggingTest.TestResolver testR = new MIMESupportLoggingTest.TestResolver("a/a");
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).isEmpty());
        lookup.setLookups(testR, new MasterURLMapper(), new FileChangedManager());
        
        File f = new File(getWorkDir(), "Test1.java");
        FileObject fo = FileUtil.toFileObject(copyStringToFile(f, "Fajl1"));
        f = new File(getWorkDir(), "Test2.java");
        FileObject fo2 = FileUtil.toFileObject(copyStringToFile(f, "Fajl2"));

        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).contains(testR));
        Logger log = Logger.getLogger("org.openide.filesystems.MIMESupport");
        log.setLevel(Level.FINE);
        ReadingHandler handler = new ReadingHandler();
        log.addHandler(handler);

        assertEquals(testR.getMime(),fo2.getMIMEType());
        assertFalse("File read " + fo2, handler.wasRead());
        
        testR.read = true;
        assertEquals(testR.getMime(),fo.getMIMEType());
        assertTrue("File read " + fo, handler.wasRead());
    }

    private static final class TestResolver extends MIMEResolver {
        private final String mime;
        private boolean read;
        
        private TestResolver(String mime) {            
            this.mime = mime;
        }
        
        public String findMIMEType(FileObject fo) {
            if (read) {
                try {
                    fo.getInputStream().read(new byte[10]);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (fo.canRead()) {
                return mime;
            } else {
                return "unreadable";
            }
        }        
        
        private String getMime() {
            return mime;
        }
    }

    public static class TestLookup extends ProxyLookup {
        public TestLookup() {
            super();
            init();
        }
        
        private void init() {
            setLookups(new Lookup[] {});
        }
        
        private void setLookups(Object... instances) {
            setLookups(new Lookup[] {getInstanceLookup(instances)});
        }
        
        private Lookup getInstanceLookup(final Object... instances) {
            InstanceContent instanceContent = new InstanceContent();
            for(Object i : instances) {
                instanceContent.add(i);
            }
            Lookup instanceLookup = new AbstractLookup(instanceContent);
            return instanceLookup;
        }        
    }    

    static class ReadingHandler extends Handler {
        
        private boolean read = false;
        
        @Override
        public void publish(LogRecord record) {
            if ("MSG_CACHED_INPUT_STREAM".equals(record.getMessage())) {
                read = true;
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public boolean wasRead() {
            return read;
        }
        
    }

    public final static File copyStringToFile (File f, String content) throws Exception {
        FileOutputStream os = new FileOutputStream(f);
        InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
        FileUtil.copy(is, os);
        os.close ();
        is.close();

        return f;
    }
}
