/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.html.palette;
import java.awt.Component;
import java.awt.Container;
import java.util.StringTokenizer;
import javax.swing.JTree;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 *
 * @author Libor Kotouc
 */
public final class HtmlPaletteUtilities {
    
    public static SourceGroup[] getSourceGroups(FileObject fObj) {
    
        Project proj = FileOwnerQuery.getOwner(fObj);
        SourceGroup[] sg = new SourceGroup[] {};
        if (proj != null) {
            Sources sources = ProjectUtils.getSources(proj);
            sg = sources.getSourceGroups("doc_root");
//            if (sg.length == 0)
//                sg = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }
        
        return sg;
    }

    public static JTree findTreeComponent(Component component) {
        if (component instanceof JTree) {
            return (JTree) component;
        }
        if (component instanceof Container) {
            Component[] components = ((Container) component).getComponents();
            for (int i = 0; i < components.length; i++) {
                JTree tree = findTreeComponent(components[i]);
                if (tree != null) {
                    return tree;
                }
            }
        }
        return null;
    }

    public static String getRelativePath(FileObject base, FileObject target) {
        
        final String DELIM = "/";
        final String PARENT = ".." + DELIM;
        
        String targetPath = target.getPath();
        String basePath = base.getPath();

        //paths begin either with '/' or with '<letter>:/' - ensure that in the latter case the <letter>s equal
        String baseDisc = basePath.substring(0, basePath.indexOf(DELIM));
        String targetDisc = targetPath.substring(0, targetPath.indexOf(DELIM));
        if (!baseDisc.equals(targetDisc))
            return ""; //different disc letters, thus returning an empty string to signalize this fact

        //cut a filename at the end taking last index for case of the same dir name as file name, really obscure but possible ;)
        basePath = basePath.substring(0, basePath.lastIndexOf(base.getNameExt()));
        targetPath = targetPath.substring(0, targetPath.lastIndexOf(target.getNameExt()));

        //iterate through prefix dirs until difference occurres
        StringTokenizer baseST = new StringTokenizer(basePath, DELIM);
        StringTokenizer targetST = new StringTokenizer(targetPath, DELIM);
        String baseDir = "";
        String targetDir = "";
        while (baseST.hasMoreTokens() && targetST.hasMoreTokens() && baseDir.equals(targetDir)) {
            baseDir = baseST.nextToken();
            targetDir = targetST.nextToken();
        }
        //create prefix consisting of parent dirs ("..")
        StringBuffer parentPrefix = new StringBuffer(!baseDir.equals(targetDir) ? PARENT : "");
        while (baseST.hasMoreTokens()) {
            parentPrefix.append(PARENT);
            baseST.nextToken();
        }
        //append remaining dirs with delimiter ("/")
        StringBuffer targetSB = new StringBuffer(!baseDir.equals(targetDir) ? targetDir + DELIM : "");
        while (targetST.hasMoreTokens())
            targetSB.append(targetST.nextToken() + DELIM);

        //resulting path
        targetPath = parentPrefix.toString() + targetSB.toString() + target.getNameExt();
        
        return targetPath;
    }

    public static void insert(String s, JTextComponent target) 
    throws BadLocationException 
    {
        insert(s, target, true);
    }
   
    public static void insert(final String s, final JTextComponent target, final boolean reformat)
            throws BadLocationException {
        final Document _doc = target.getDocument();
        if (!(_doc instanceof BaseDocument)) {
            return;
        }

        BaseDocument doc = (BaseDocument) _doc;
        final Reformat reformatter = Reformat.get(doc);
        reformatter.lock();
        try {
            doc.runAtomic(new Runnable() {

                public void run() {
                    try {
                        String s2 = s == null ? "" : s;
                        int start = insert(s2, target, _doc);
                        if (reformat && start >= 0 && _doc instanceof BaseDocument) {
                            // format the inserted text
                            int end = start + s2.length();
                            reformatter.reformat(start, end);
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });

        } finally {
            reformatter.unlock();
        }

    }
    
    private static int insert(String s, JTextComponent target, Document doc) 
    throws BadLocationException 
    {

        int start = -1;
        try {
            //at first, find selected text range
            Caret caret = target.getCaret();
            int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
            doc.remove(p0, p1 - p0);
            
            //replace selected text by the inserted one
            start = caret.getDot();
            doc.insertString(start, s, null);
        }
        catch (BadLocationException ble) {}
        
        return start;
    }

    
}
