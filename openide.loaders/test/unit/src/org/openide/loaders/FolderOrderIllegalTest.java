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

package org.openide.loaders;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/** Trying to emulate issue 122295.
 *
 * @author Jaroslav Tulach
 */
public class FolderOrderIllegalTest extends NbTestCase {
    Logger LOG;
    
    public FolderOrderIllegalTest(String name) {
        super(name);
    }
    @Override
    protected Level logLevel() {
        return Level.FINEST;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        LOG = Logger.getLogger("org.openide.loaders.FolderList." + getName());
    }
    public void testRenameBehaviour() throws Exception {
        File dir = new File(getWorkDir(), "dir");
        dir.mkdirs();
        File old = new File(getWorkDir(), "old");
        old.mkdirs();
        for (int i = 0; i < 10; i++) {
            File fJava = new File(old, "F" + i + ".java");
            fJava.createNewFile();
        }

        FileObject root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("root found", root);
        
        final DataFolder f = DataFolder.findFolder(root.getFileObject("old"));
        final DataFolder target = DataFolder.findFolder(root.getFileObject("dir"));
        
        class R implements Runnable {
            public void run() {
                try {
                    LOG.info("runnable waiting");
                    DataObject obj = DataObject.find(f.getPrimaryFile().getFileObject("F5.java"));
                    obj.move(target);
                    LOG.info("done");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        R run = new R();
        RequestProcessor RP = new RequestProcessor("move");

        Logger listenTo = Logger.getLogger("org.openide.loaders.FolderList");
        String order = 
                "THREAD: move MSG:.*runnable waiting.*" +
                "THREAD: Folder recognizer MSG:.*carefullySort on.*" +
                "THREAD: Folder recognizer MSG:.*carefullySort before getOrder.*" +
                "THREAD: move MSG:.*done";
        Log.controlFlow(listenTo, Logger.getLogger("global"), order, 500);
        RP.post(run);
        
        DataObject[] arr = f.getChildren();
        
        String txt = Arrays.toString(arr).replaceAll(", ", "\n");
        assertEquals("All 10:\n" + txt, 10, arr.length);//fail("OK:\n" + txt);
    }
}
