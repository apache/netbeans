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
package org.netbeans.modules.j2ee.weblogic9.optional;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;


/**
 * This class is stateful : it keeps information about previously red lines.  
 * @author ads
 *
 */
public class ErrorLineConvertor implements LineConvertor {

    /* (non-Javadoc)
     * @see org.netbeans.api.extexecution.print.LineConvertor#convert(java.lang.String)
     */
    @Override
    public List<ConvertedLine> convert( String line ) {
        String path = null;
        int lineNo = -1;
        String message = null;
        boolean accessible = false;

        String logLine = line.trim();
        int lineLenght = logLine.length();

        // look for unix file links (e.g. /foo/bar.java:51: 'error msg')
        if (logLine.startsWith("/")) {
            int colonIdx = logLine.indexOf(':');
            if (colonIdx > -1) {
                path = logLine.substring(0, colonIdx);
                if (lineLenght > colonIdx) {
                    int nextColonIdx = logLine.indexOf(':', colonIdx + 1);
                    if (nextColonIdx > -1) {
                        String lineNum = logLine.substring(colonIdx + 1,
                                nextColonIdx);
                        try {
                            lineNo = Integer.valueOf(lineNum).intValue();
                            accessible = true;
                        }
                        catch (NumberFormatException nfe) {
                            /* This means that line has wrong pattern.
                             * Ignore exception and skip this line 
                             */
                        }
                        if (lineLenght > nextColonIdx) {
                            message = logLine.substring(nextColonIdx + 1,
                                    lineLenght);
                        }
                    }
                }
            }
        }
        // look for windows file links (e.g. c:\foo\bar.java:51: 'error msg')
        else if (lineLenght > 3 && Character.isLetter(logLine.charAt(0))
                && (logLine.charAt(1) == ':') && (logLine.charAt(2) == '\\'))
        {
            int secondColonIdx = logLine.indexOf(':', 2);
            if (secondColonIdx > -1) {
                path = logLine.substring(0, secondColonIdx);
                if (lineLenght > secondColonIdx) {
                    int thirdColonIdx = logLine
                            .indexOf(':', secondColonIdx + 1);
                    if (thirdColonIdx > -1) {
                        String lineNum = logLine.substring(secondColonIdx + 1,
                                thirdColonIdx);
                        try {
                            lineNo = Integer.valueOf(lineNum).intValue();
                            accessible = true;
                        }
                        catch (NumberFormatException nfe) {
                            /* This means that line has wrong pattern.
                             * Ignore exception and skip this line 
                             */
                        }
                        if (lineLenght > thirdColonIdx) {
                            message = logLine.substring(thirdColonIdx + 1,
                                    lineLenght);
                        }
                    }
                }
            }
        }
        // look for stacktrace links (e.g. at
        // java.lang.Thread.run(Thread.java:595)
        // at t.HyperlinkTest$1.run(HyperlinkTest.java:24))
        else if (logLine.startsWith("at ") && lineLenght > 3) {
            int parenthIdx = logLine.indexOf('(');
            if (parenthIdx > -1) {
                String classWithMethod = logLine.substring(3, parenthIdx);
                int lastDotIdx = classWithMethod.lastIndexOf('.');
                if (lastDotIdx > -1) {
                    int lastParenthIdx = logLine.lastIndexOf(')');
                                            String content = null;
                    if (lastParenthIdx > -1) {
                        content = logLine.substring(parenthIdx + 1, lastParenthIdx);
                    }
                    if (content != null) {
                        int lastColonIdx = content.lastIndexOf(':');
                        if (lastColonIdx > -1) {
                            String lineNum = content.substring(lastColonIdx + 1);
                            try {
                                lineNo = Integer.valueOf(lineNum).intValue();
                            } catch(NumberFormatException nfe) {
                                // ignore it
                                Logger.getLogger(ErrorLineConvertor.class.getName()).log(Level.INFO, null, nfe);
                            }
                            message = previousMessage;
                        }
                    }
                    int firstDolarIdx = classWithMethod.indexOf('$'); // > -1
                                                                      // for
                                                                      // inner
                                                                      // classes
                    String className = classWithMethod.substring(0,
                            firstDolarIdx > -1 ? firstDolarIdx : lastDotIdx);
                    path = className.replace('.', '/') + ".java"; // NOI18N
                    accessible = GlobalPathRegistry.getDefault().findResource(path) != null;
                }
            }
        }
        // every other message treat as normal info message
        else {
            previousMessage = logLine;
        }
        if ( accessible ){
            return Collections.singletonList( 
                ConvertedLine.forText(line, new LinkOutputListener(message, 
                        lineNo, path)));
        }
        else {
            return null;
        }
    }
    
    private static class LinkOutputListener implements OutputListener {
        
        private final String message; 
        private final int line;
        private final String path;
        
        LinkOutputListener (String msg , int lineNo , String filePath) {
            message = msg;
            line = lineNo;
            path = filePath;
        }

        public void outputLineAction(OutputEvent ev) {
            FileObject sourceFile = GlobalPathRegistry.getDefault().findResource(
                    path);
            if (sourceFile == null) {
                sourceFile = FileUtil.toFileObject(FileUtil.normalizeFile(
                        new File(path)));
            }
            DataObject dataObject = null;
            if (sourceFile != null) {
                try {
                    dataObject = DataObject.find(sourceFile);
                } catch(DataObjectNotFoundException ex) {
                    Logger.getLogger(ErrorLineConvertor.class.getName()).log(
                            Level.INFO, null, ex);
                }
            }
            if (dataObject != null) {
                EditorCookie editorCookie = (EditorCookie)dataObject.getCookie(
                        EditorCookie.class);
                if (editorCookie == null) {
                    return;
                }
                editorCookie.open();
                Line errorLine = null;
                try {
                    errorLine = editorCookie.getLineSet().getCurrent(line - 1);
                } catch (IndexOutOfBoundsException iobe) {
                    return;
                }
                errorLine.show(ShowOpenType.OPEN, ShowVisibilityType.NONE);
            }
        }
        
        public void outputLineCleared(OutputEvent ev) {
        }
        
        public void outputLineSelected(OutputEvent ev) {           
        }
        
    }

    private String previousMessage ;
}
