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

package org.netbeans.modules.diff.builtin.visualizer;

import java.awt.Component;
import java.io.*;

import org.openide.windows.CloneableOpenSupport;

import org.openide.util.NbBundle;

import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffVisualizer;

import org.netbeans.modules.diff.builtin.DiffPresenter;

/**
 * The textual visualizer of diffs.
 *
 * @author  Martin Entlicher
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.diff.DiffVisualizer.class)
public class TextDiffVisualizer extends DiffVisualizer implements Serializable {
    
    private boolean contextMode = true;
    private int contextNumLines = 3;

    static final long serialVersionUID =-2481513747957146261L;
    /** Creates a new instance of TextDiffVisualizer */
    public TextDiffVisualizer() {
    }

    /**
     * Get the display name of this diff visualizer.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(TextDiffVisualizer.class, "TextDiffVisualizer.displayName");
    }
    
    /**
     * Get a short description of this diff visualizer.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(TextDiffVisualizer.class, "TextDiffVisualizer.shortDescription");
    }
    
    /** Getter for property contextMode.
     * @return Value of property contextMode.
     */
    public boolean isContextMode() {
        return contextMode;
    }
    
    /** Setter for property contextMode.
     * @param contextMode New value of property contextMode.
     */
    public void setContextMode(boolean contextMode) {
        this.contextMode = contextMode;
    }
    
    /** Getter for property contextNumLines.
     * @return Value of property contextNumLines.
     */
    public int getContextNumLines() {
        return contextNumLines;
    }
    
    /** Setter for property contextNumLines.
     * @param contextNumLines New value of property contextNumLines.
     */
    public void setContextNumLines(int contextNumLines) {
        this.contextNumLines = contextNumLines;
    }
    
    /**
     * Some diff visualizers may have built-in the diff calculation. In such a case
     * the visualizer does not need any diff provider.
     * @return true when it relies on differences supplied, false if not.
     *
    public boolean needsProvider() {
        return true;
    }
     */
    
    /**
     * Show the visual representation of the diff between two sources.
     * @param diffs The list of differences (instances of {@link Difference}).
     *       may be <code>null</code> in case that it does not need diff provider.
     * @param name1 the name of the first source
     * @param title1 the title of the first source
     * @param r1 the first source
     * @param name2 the name of the second source
     * @param title2 the title of the second source
     * @param r2 the second resource compared with the first one.
     * @param MIMEType the mime type of these sources
     * @return The TopComponent representing the diff visual representation
     *        or null, when the representation is outside the IDE.
     * @throws IOException when the reading from input streams fails.
     */
    public Component createView(Difference[] diffs, String name1, String title1, Reader r1,
                                String name2, String title2, Reader r2, String MIMEType) throws IOException {
        /*
        TextDiffEditorSupport.DiffsListWithOpenSupport diff =
            new TextDiffEditorSupport.DiffsListWithOpenSupport(diffs, name1 + " <> " + name2, title1+" <> "+title2);
        diff.setContextMode(contextMode, contextNumLines);
        diff.setReaders(r1, r2);
        return ((TextDiffEditorSupport) diff.getOpenSupport()).createCloneableTopComponentForMe();
        //return null;
         */
        TextDiffInfo diff = new TextDiffInfo(name1, name2, title1, title2, r1, r2, diffs);
        diff.setContextMode(contextMode, contextNumLines);
        return ((TextDiffEditorSupport) diff.getOpenSupport()).createCloneableTopComponentForMe();
    }
    
    static InputStream differenceToLineDiffText(Difference[] diffs) {
        StringBuffer content = new StringBuffer();
        int n1, n2, n3, n4;
        for (int i = 0; i < diffs.length; i++) {
            Difference diff = diffs[i];
            switch (diff.getType()) {
                case Difference.ADD:
                    n3 = diff.getSecondStart();
                    n4 = diff.getSecondEnd();
                    if (n3 == n4) {
                        content.append(diff.getFirstStart()+"a"+n3+"\n");
                    } else {
                        content.append(diff.getFirstStart()+"a"+n3+","+n4+"\n");
                    }
                    appendText(content, "> ", diff.getSecondText());
                    break;
                case Difference.DELETE:
                    n1 = diff.getFirstStart();
                    n2 = diff.getFirstEnd();
                    if (n1 == n2) {
                        content.append(n1+"d"+diff.getSecondStart()+"\n");
                    } else {
                        content.append(n1+","+n2+"d"+diff.getSecondStart()+"\n");
                    }
                    appendText(content, "< ", diff.getFirstText());
                    break;
                case Difference.CHANGE:
                    n1 = diff.getFirstStart();
                    n2 = diff.getFirstEnd();
                    n3 = diff.getSecondStart();
                    n4 = diff.getSecondEnd();
                    if (n1 == n2 && n3 == n4) {
                        content.append(n1+"c"+n3+"\n");
                    } else if (n1 == n2) {
                        content.append(n1+"c"+n3+","+n4+"\n");
                    } else if (n3 == n4) {
                        content.append(n1+","+n2+"c"+n3+"\n");
                    } else {
                        content.append(n1+","+n2+"c"+n3+","+n4+"\n");
                    }
                    appendText(content, "< ", diff.getFirstText());
                    content.append("---\n");
                    appendText(content, "> ", diff.getSecondText());
                    break;
            }
        }
        return new ByteArrayInputStream(content.toString().getBytes());
    }
    
    private static void appendText(StringBuffer buff, String prefix, String text) {
        if (text == null) return ;
        int startLine = 0;
        do {
            int endLine = text.indexOf('\n', startLine);
            if (endLine < 0) endLine = text.length();
            buff.append(prefix + text.substring(startLine, endLine) + "\n");
            startLine = endLine + 1;
        } while (startLine < text.length());
    }
    
    /**
     * Produces textual diff output in unified format.
     * 
     * @param diffInfo encapsulates information needed to produce the diff
     * @return String textual diff output in unified format (unidiff)
     */
    public static String differenceToUnifiedDiffText(TextDiffInfo diffInfo) throws IOException {
        UnifiedDiff ud = new UnifiedDiff(diffInfo);
        return ud.computeDiff();
    }

    /**
     * Produces textual diff output in normal format.
     * 
     * @return String textual diff output in normal diff format
     */
    public static String differenceToNormalDiffText(TextDiffInfo diffInfo) throws IOException {
        InputStream is = differenceToLineDiffText(diffInfo.diffs);
        StringWriter sw = new StringWriter();
        copyStreamsCloseAll(sw, new InputStreamReader(is));
        return sw.toString();
    }
    
    private static void copyStreamsCloseAll(Writer writer, Reader reader) throws IOException {
        char [] buffer = new char[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }
    
    public static class TextDiffInfo extends DiffPresenter.Info {
        
        private Reader r1;
        private Reader r2;
        private Difference[] diffs;
        private CloneableOpenSupport openSupport;
        private boolean contextMode;
        private int contextNumLines;
        
        public TextDiffInfo(String name1, String name2, String title1, String title2,
                            Reader r1, Reader r2, Difference[] diffs) {
            super(name1, name2, title1, title2, null, false, false);
            this.r1 = r1;
            this.r2 = r2;
            this.diffs = diffs;
        }
        
        public String getName() {
            String componentName = getName1();
            String name2 = getName2();
            if (name2 != null && name2.length() > 0)  componentName += " <> "+name2;
            return componentName;
        }
        
        public String getTitle() {
            return getTitle1() + " <> " + getTitle2();
        }
        
        public Reader createFirstReader() {
            return r1;
        }
        
        public Reader createSecondReader() {
            return r2;
        }
        
        public Difference[] getDifferences() {
            return diffs;
        }
        
        public CloneableOpenSupport getOpenSupport() {
            if (openSupport == null) {
                openSupport = new TextDiffEditorSupport(this);
            }
            return openSupport;
        }
        
        /** Setter for property contextMode.
         * @param contextMode New value of property contextMode.
         */
        public void setContextMode(boolean contextMode, int contextNumLines) {
            this.contextMode = contextMode;
            this.contextNumLines = contextNumLines;
        }
        
        /** Getter for property contextMode.
         * @return Value of property contextMode.
         */
        public boolean isContextMode() {
            return contextMode;
        }
        
        public int getContextNumLines() {
            return contextNumLines;
        }
        
    }
}
