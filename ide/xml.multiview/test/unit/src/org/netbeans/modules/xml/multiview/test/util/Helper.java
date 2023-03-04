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
package org.netbeans.modules.xml.multiview.test.util;

import java.io.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;

import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.test.BookDataObject;
import org.netbeans.modules.xml.multiview.test.bookmodel.Chapter;

import javax.swing.*;
import javax.swing.text.Document;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

public class Helper {

    public static File getBookFile(File dataDir, File workDir) throws IOException {
        final File source = new File(dataDir, "sample.book");
        final File target = new File(workDir, "sample.book");
        if (target.exists()) {
            return target;
        }
        FileUtil.runAtomicAction(new FileSystem.AtomicAction() {

            @Override
            public void run() throws IOException {
                BufferedInputStream is = new BufferedInputStream(new FileInputStream(source));
                try {
                    BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(target));
                    try {
                        FileUtil.copy(is, os);
                    } finally {
                        os.close();
                    }
                } finally {
                    is.close();
                }
            }
        });

        FileUtil.refreshFor(target);
        return target;
    }

    public static JTextField getChapterTitleTF(final BookDataObject dObj, Chapter chapter) {
        final ToolBarMultiViewElement multiViewElement = new StepIterator() {
            ToolBarMultiViewElement multiViewElement;

            public boolean step() throws Exception {
                return (multiViewElement = dObj.getActiveMultiViewElement0()) != null;
            }
        }.multiViewElement;
        SectionView sectionView = new StepIterator() {
            SectionView sectionView;
            public boolean step() throws Exception {
                return (sectionView = multiViewElement.getSectionView()) != null;

            }
        }.sectionView;
        JPanel sectionPanel = sectionView.findSectionPanel(chapter).getInnerPanel();
        Component[] children = sectionPanel.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof JTextField) {
                return (JTextField) children[i];
            }
        }
        return null;
    }

    public static boolean isTextInFile(String text, File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line=reader.readLine())!=null) {
            if (line.indexOf(text) >= 0) {
                return true;
            }
        }
        return false;
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex){
            // ignore
        }
    }

    public static void waitForDispatchThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            return;
        }
        final AtomicBoolean finished = new AtomicBoolean();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                finished.set(true);
            }
        });
        new StepIterator() {
            public boolean step() throws Exception {
                return finished.get();
            }
        };
    }

    public static SaveCookie getSaveCookie(final DataObject dataObject) {
        return new StepIterator() {
            SaveCookie cookie;

            public boolean step() throws Exception {
                return ((cookie = (SaveCookie) dataObject.getCookie(SaveCookie.class)) != null);
            }
        }.cookie;
    }

    public static Document getDocument(final XmlMultiViewEditorSupport editor) {
        return new StepIterator() {
            Document document;

            public boolean step() throws Exception {
                document = editor.getDocument();
                return (document.getLength() > 0);
            }
        }.document;
    }
}
