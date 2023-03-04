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
