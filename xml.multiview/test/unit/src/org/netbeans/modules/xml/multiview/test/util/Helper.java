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
