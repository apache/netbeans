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
package org.netbeans.modules.java.editor.base.semantic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jan Lahoda
 */
public class ShowGoldenFiles extends NbTestCase {
    
    /** Creates a new instance of ShowGoldenFiles */
    public ShowGoldenFiles(String name) {
        super(name);
    }
    
    public void testX() throws Exception {
        main(null);
    }

    private static List<HighlightImpl> parse(StyledDocument doc, File highlights) throws IOException, ParseException, BadLocationException {
        if (!highlights.exists())
            return Collections.emptyList();
        
        BufferedReader bis = new BufferedReader(new InputStreamReader(new FileInputStream(highlights)));
        List<HighlightImpl> result = new ArrayList<HighlightImpl>();
        String line = null;
        
        while ((line = bis.readLine()) != null) {
            result.add(HighlightImpl.parse(doc, line));
        }
        
        return result;
    }
    
    private static StyledDocument loadDocument(File file) throws IOException, BadLocationException {
        StringBuffer sb = new StringBuffer();
        Reader r = new FileReader(file);
        int c;
        
        while ((c = r.read()) != (-1)) {
            sb.append((char) c);
        }
        
        StyledDocument result = new HTMLDocument();//new DefaultStyledDocument();
        
        result.insertString(0, sb.toString(), null);
        
        return result;
    }
    
    public static void main(String[] args) throws Exception {
        String className = "DetectorTest";
        String testName  = "testReadWriteUseArgumentOfAbstractMethod";
    }
    
    public static void run(String className, String testName, String fileName) throws Exception {
        final File golden = new File("/space/nm/java/editor/test/unit/data/goldenfiles/org/netbeans/modules/java/editor/semantic/" + className + "/" + testName + ".pass");
        final File test   = new File("/tmp/tests/org.netbeans.modules.java.editor.semantic." + className + "/" + testName + "/" + testName + ".out");
        final File source = new File("/tmp/tests/org.netbeans.modules.java.editor.semantic." + className + "/" + testName + "/test/" + fileName + ".java");
        
        final StyledDocument doc = loadDocument(source);
        final List<HighlightImpl> goldenHighlights = parse(doc, golden);
        final List<HighlightImpl> testHighlights = parse(doc, test);

        Runnable show = new Runnable() {
            public void run() {
                JDialog d = new JDialog();
                
                d.setModal(true);
                
                ShowGoldenFilesPanel panel = new ShowGoldenFilesPanel(d);
                
                panel.setDocument(doc, goldenHighlights, testHighlights, golden, test);
                
                d.getContentPane().add(panel);
                
                d.show();
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            show.run();
        } else {
            SwingUtilities.invokeAndWait(show);
        }
    }
    
}
