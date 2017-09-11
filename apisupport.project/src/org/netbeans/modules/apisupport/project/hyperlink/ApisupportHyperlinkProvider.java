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

package org.netbeans.modules.apisupport.project.hyperlink;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class ApisupportHyperlinkProvider implements HyperlinkProviderExt {

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        return getIdentifierSpan(doc, offset, null);
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        Line ln = getLine(doc, offset);
        if (ln != null) {
            ln.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        Line ln = getLine(doc, offset);
        if (ln != null) {
            return ln.getText();
        }
        return null;
    }
    
    private Line getLine(Document doc, int offset) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(th, offset);
        
        if (ts == null)
            return null;
        
        ts.move(offset);
        if (!ts.moveNext())
            return null;
        
        Token<JavaTokenId> t = ts.token();
        FileObject fo = getFileObject(doc);
        String name = t.text().toString();
        if (name.startsWith("\"")) {
            name = name.substring(1);
        }
        if (name.endsWith("\"")) {
            name = name.substring(0, name.length() - 1);
        }
        FileObject props = findBundle(fo); // XXX incorrect in case arg is in another package
        if (props != null) {
            try {
                DataObject dobj = DataObject.find(props);
                EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
                try {
                    ec.openDocument();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                LineCookie lc = dobj.getLookup().lookup(LineCookie.class);
                if (lc != null) {
                    Line.Set ls = lc.getLineSet();
                    for (Line line : ls.getLines()) {
                        if (line.getText().contains(name + "=")) {
                            return line;
                        }
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
    
    private @CheckForNull FileObject findBundle(FileObject javaFile) {
        ClassPath cp = ClassPath.getClassPath(javaFile, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }
        String name = cp.getResourceName(javaFile);
        if (name != null) {
            int index = name.lastIndexOf('/');
            name = name.substring(0, index) + "/Bundle.properties"; //NOI18N
            return cp.findResource(name);
        }
        return null;
    }
    
    private static final Set<JavaTokenId> USABLE_TOKEN_IDS = EnumSet.of(JavaTokenId.STRING_LITERAL);
    
    public static int[] getIdentifierSpan(Document doc, int offset, Token<JavaTokenId>[] token) {
        FileObject fo = getFileObject(doc);
        if (fo == null) {
            //do nothing if FO is not attached to the document - the goto would not work anyway:
            return null;
        }
        Project prj = FileOwnerQuery.getOwner(fo);
        if (prj == null) {
            return null;
        }
        
        NbModuleProvider module = prj.getLookup().lookup(NbModuleProvider.class);
        if (module == null) {
            return null;
        }
        
        ((AbstractDocument) doc).readLock();
        
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<JavaTokenId> ts = null;
        try {
            ts = SourceUtils.getJavaTokenSequence(th, offset);
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }
        
        if (ts == null)
            return null;
        
        ts.move(offset);
        if (!ts.moveNext())
            return null;
        
        Token<JavaTokenId> t = ts.token();
        boolean hasMessage = false;
        boolean hasNbBundle = false;
        if (USABLE_TOKEN_IDS.contains(t.id())) {
            for (int i = 0; i < 10; i++) {
                if (!ts.movePrevious()) {
                    break;
                }
                Token<JavaTokenId> tk = ts.token();
                if (TokenUtilities.equals("getMessage", tk.text())) {
                    hasMessage = true;
                }
                else if (TokenUtilities.equals("NbBundle", tk.text())) {
                    hasNbBundle = true;
                }
            }
            if (hasNbBundle && hasMessage) {
                ts.move(offset);
                ts.moveNext();
                return new int [] {ts.offset(), ts.offset() + t.length()};
            }
        }
        return null;
        
    }
    
    private static FileObject getFileObject(Document doc) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        return od != null ? od.getPrimaryFile() : null;
    }
    

}
