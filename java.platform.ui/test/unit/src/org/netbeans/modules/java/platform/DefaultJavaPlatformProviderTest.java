/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.platform;

import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Tomas Zezula
 */
public class DefaultJavaPlatformProviderTest extends NbTestCase {

    private static final int TIMEOUT = 500;
    private static FileSystem fs;
    private static Repository rp;


    public DefaultJavaPlatformProviderTest(final String name) {
        super(name);
        fs = FileUtil.createMemoryFileSystem();
        fs.getRoot();
        rp = new Repository(fs);
        System.setProperty("org.openide.util.Lookup", DefaultJavaPlatformProviderTest.Lkp.class.getName()); //NOI18N
        Assert.assertEquals(DefaultJavaPlatformProviderTest.Lkp.class, Lookup.getDefault().getClass());
    }

    public void testMissingLayer202479() throws Exception {
        final FileObject services = fs.getRoot().createFolder("Services");    //NOI18N
        final Listener l = new Listener();
        final JavaPlatformProvider jp = new DefaultJavaPlatformProvider();
        jp.addPropertyChangeListener(l);
        assertEquals(0,jp.getInstalledPlatforms().length);
        fs.getRoot().createFolder("Platforms"); //NOI18N
        assertFalse(l.expect(1, TIMEOUT));
        services.createFolder("Test");  //NOI18N
        assertFalse(l.expect(1, TIMEOUT));
        final FileObject platforms = services.createFolder("Platforms");  //NOI18N
        assertTrue(l.expect(1, TIMEOUT));
        assertEquals(0,jp.getInstalledPlatforms().length);
        platforms.createFolder("Test");  //NOI18N
        assertFalse(l.expect(1, TIMEOUT));
        final FileObject j2se = platforms.createFolder("org-netbeans-api-java-Platform");  //NOI18N
        assertTrue(l.expect(1, TIMEOUT));
        assertEquals(0,jp.getInstalledPlatforms().length);
        InstanceDataObject.create(
             DataFolder.findFolder(j2se),
             "DummyPlatform",   //NOI18N
             ConvertAsJavaBeanPlatformTest.TestPlatform.class);
        assertTrue(l.expect(1, TIMEOUT));
        assertEquals(1,jp.getInstalledPlatforms().length);
    }


    public static class Lkp extends ProxyLookup {
        public Lkp() {
            setLookups(Lookups.fixed(rp));
        }
    }

    public static class Listener implements PropertyChangeListener {

        private final Semaphore sem = new Semaphore(0);

        public boolean expect(int permits, long timeOut) throws InterruptedException {
            return sem.tryAcquire(permits, timeOut, TimeUnit.MILLISECONDS);
        }


        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            sem.release();
        }

    }
}
