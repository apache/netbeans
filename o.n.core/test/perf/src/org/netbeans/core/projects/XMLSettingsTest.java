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

package org.netbeans.core.projects;

import java.io.*;
import java.util.*;

import org.netbeans.performance.Benchmark;

import org.openide.loaders.*;

import org.netbeans.core.NbTopManager;
import org.netbeans.core.projects.FixedFileSystem;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileUtil;

/**
 * Benchmark measuring how fast .settings files can be recognized and probed for instances.
 * Uses FolderInstance as the means of collecting them together.
 * @author Jesse Glick
 */
public class XMLSettingsTest extends Benchmark {
    
    public static void main(String[] args) {
        simpleRun(XMLSettingsTest.class);
    }
    
    public XMLSettingsTest(String name) {
        // Each arg is an Integer[2] of # of unmodified & modified .settings files to make
        super(name, new int[][] {
            {10, 0},
            {100, 0},
            {1000, 0},
            {5, 5},
            {50, 50},
            {500, 500},
        });
    }
    
    private static byte[] unmodifiedData = null;
    private static byte[] getUnmodifiedData() {
        if (unmodifiedData == null) {
            StringBuffer b = new StringBuffer();
            b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            b.append("<!DOCTYPE settings PUBLIC \"-//NetBeans//DTD Session settings 1.0//EN\" \"http://www.netbeans.org/dtds/sessionsettings-1_0.dtd\">\n");
            b.append("<settings version=\"1.0\">\n");
            b.append("    <instanceof class=\"org.netbeans.core.projects.XMLSettingsTest$SuperClazz\"/>\n");
            b.append("    <instanceof class=\"org.netbeans.core.projects.XMLSettingsTest$Clazz\"/>\n");
            b.append("    <instance class=\"org.netbeans.core.projects.XMLSettingsTest$Clazz\"/>\n");
            b.append("</settings>\n");
            unmodifiedData = b.toString().getBytes();
        }
        return unmodifiedData;
    }
    
    private static List modifiedData = new ArrayList(1000);
    private static char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static byte[] getModifiedData(int i) throws IOException {
        while (modifiedData.size() <= i) modifiedData.add(null);
        if (modifiedData.get(i) == null) {
            StringBuffer b = new StringBuffer();
            b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            b.append("<!DOCTYPE settings PUBLIC \"-//NetBeans//DTD Session settings 1.0//EN\" \"http://www.netbeans.org/dtds/sessionsettings-1_0.dtd\">\n");
            b.append("<settings version=\"1.0\">\n");
            b.append("    <instanceof class=\"org.netbeans.core.projects.XMLSettingsTest$SuperClazz\"/>\n");
            b.append("    <instanceof class=\"org.netbeans.core.projects.XMLSettingsTest$Clazz\"/>\n");
            b.append("    <serialdata class=\"org.netbeans.core.projects.XMLSettingsTest$Clazz\">\n");
            b.append("        ");
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(new Clazz(i));
            oos.close();
            byte[] ser = baos.toByteArray();
            for (int j = 0; j < ser.length; j++) {
                int x = ser[j] + 256;
                b.append(HEX[(x & 0xF0) >> 4]);
                b.append(HEX[x & 0x0F]);
            }
            b.append("\n");
            b.append("    </serialdata>\n");
            b.append("</settings>\n");
            modifiedData.set(i, b.toString().getBytes());
        }
        return (byte[])modifiedData.get(i);
    }
    
    private DataFolder[] folders;
    protected void setUp() throws Exception {
        NbTopManager.get();
        int count = getIterationCount();
        int[] x = (int[])getArgument();
        int unmodified = x[0];
        int modified = x[1];
        //System.out.println("setUp: count=" + count + " unmodified=" + unmodified + " modified=" + modified);
        folders = new DataFolder[count];
        for (int i = 0; i < count; i++) {
            FixedFileSystem ffs = FixedFileSystem.getDefault();
            for (int j = 0; j < unmodified; j++) {
                FixedFileSystem.Instance inst = new FixedFileSystem.Instance(false, "text/xml", getUnmodifiedData(), null, (String)null);
                ffs.add("folder" + i + "/unmodified" + j + ".settings", inst);
            }
            for (int j = 0; j < modified; j++) {
                FixedFileSystem.Instance inst = new FixedFileSystem.Instance(false, "text/xml", getModifiedData(j + 1), null, (String)null);
                ffs.add("folder" + i + "/modified" + j + ".settings", inst);
            }
            folders[i] = DataFolder.findFolder(FileUtil.getConfigFile("folder" + i));
        }
    }
    protected void tearDown() throws Exception {
        folders = null;
        int count = getIterationCount();
        int[] x = (int[])getArgument();
        int unmodified = x[0];
        int modified = x[1];
        for (int i = 0; i < count; i++) {
            FixedFileSystem ffs = FixedFileSystem.getDefault();
            for (int j = 0; j < unmodified; j++) {
                ffs.remove("folder" + i + "/unmodified" + j + ".settings");
            }
            for (int j = 0; j < modified; j++) {
                ffs.remove("folder" + i + "/modified" + j + ".settings");
            }
            ffs.remove("folder" + i);
        }
    }
    
    public void testSettings() throws Exception {
        int count = getIterationCount();
        int[] x = (int[])getArgument();
        int unmodified = x[0];
        int modified = x[1];
        for (int i = 0; i < count; i++) {
            try {
                /*
                System.out.println("folder=" + folders[i]);
                DataObject[] kids = folders[i].getChildren();
                System.out.println("folder.children=" + Arrays.asList(kids));
                InstanceCookie ic = (InstanceCookie)kids[0].getCookie(InstanceCookie.class);
                System.out.println("folder.children[0].instanceCreate=" + (ic == null ? "null" : ic.instanceCreate()));
                 */
                InstanceCookie cf = new ClazzFolder(folders[i]);
                /*
                FolderInstance cf = new ClazzFolder(folders[i]);
                cf.run();
                cf.waitFinished();
                 */
                SuperClazz[] clazzes = (SuperClazz[])cf.instanceCreate();
                //System.out.println("clazzes=" + Arrays.asList(clazzes));
                int unmodifiedCount = 0;
                int modifiedCount = 0;
                for (int j = 0; j < clazzes.length; j++) {
                    int number = clazzes[j].x();
                    if (number == 0) {
                        unmodifiedCount++;
                    } else {
                        modifiedCount += number;
                    }
                }
                assertEquals(unmodified, unmodifiedCount);
                // 0, 1, 3, 6, 10, 15, ...
                assertEquals((modified * (modified + 1)) / 2, modifiedCount);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                throw e;
            } catch (Error e) {
                e.printStackTrace(System.out);
                throw e;
            }
        }
    }
    
    private static final class ClazzFolder extends FolderInstance {
        public ClazzFolder(DataFolder f) {
            super(f);
        }
        protected Object createInstance(InstanceCookie[] cookies) throws IOException, ClassNotFoundException {
            //System.out.println("createInstance: " + Arrays.asList(cookies));
            SuperClazz[] clazzes = new SuperClazz[cookies.length];
            for (int i = 0; i < clazzes.length; i++) {
                clazzes[i] = (SuperClazz)cookies[i].instanceCreate();
            }
            return clazzes;
        }
        protected InstanceCookie acceptCookie(InstanceCookie cookie) throws IOException, ClassNotFoundException {
            if (cookie instanceof InstanceCookie.Of) {
                if (((InstanceCookie.Of)cookie).instanceOf(SuperClazz.class)) {
                    //System.out.println("acceptCookie: OK");
                    return cookie;
                }
                System.out.println("acceptCookie: not IC.Of");
            }
            System.out.println("acceptCookie: strange class: " + cookie.instanceName());
            return null;
        }
    }
    
    public static abstract class SuperClazz implements Serializable {
        public abstract int x();
    }
    public static final class Clazz extends SuperClazz {
        private static final long serialVersionUID = 24356298473569L;
        private final int x;
        public Clazz() {x = 0;}
        public Clazz(int _x) {x = _x;}
        public final int x() {return x;}
        public String toString() {return "Clazz[" + x + "]";}
    }
    
}
