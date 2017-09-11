/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.hyperlinks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.grammar.POMDataObject;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.EditCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.maven.hyperlinks.Bundle.*;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;

/**
 * adds hyperlinking support to pom.xml files..
 * @author mkleint
 */
@MimeRegistrations({
    @MimeRegistration(mimeType=Constants.POM_MIME_TYPE, service=HyperlinkProviderExt.class, position = 1000),
    @MimeRegistration(mimeType=POMDataObject.SETTINGS_MIME_TYPE, service=HyperlinkProviderExt.class, position = 1000)
})
public class HyperlinkProviderImpl implements HyperlinkProviderExt {
    private static final Logger LOG = Logger.getLogger(HyperlinkProviderImpl.class.getName());
    
    @Override
    public boolean isHyperlinkPoint(final Document doc, final int offset, HyperlinkType type) {
        final boolean[] isText = new boolean[1];
        final int[] ftokenOff = new int[1];
        final String[] ftext = new String[1];
        doc.render(new Runnable() {

            @Override
            public void run() {
                isText[0] = false;
                TokenHierarchy th = TokenHierarchy.get(doc);
                TokenSequence<XMLTokenId> xml = th.tokenSequence(XMLTokenId.language());
                xml.move(offset);
                xml.moveNext();
                Token<XMLTokenId> token = xml.token();

                // when it's not a value -> do nothing.
                if (token == null) {
                   
                    return;
                }
                if (token.id() == XMLTokenId.TEXT) {
                    isText[0] = true;
                    ftokenOff[0] = xml.offset();
                    ftext[0] = token.text().toString();
                }
            }
        });
           
        if (isText[0]) {
            //we are in element text
            FileObject fo = getProjectDir(doc);
            String text = ftext[0];
            int tokenOff = ftokenOff[0];
            if (fo != null && getPath(fo, text) != null) {
                return true;
            }
            // urls get opened..
            if (text != null &&
                    (text.startsWith("http://") || //NOI18N
                    (text.startsWith("https://")))) { //NOI18N
                return true;
            }
            if (text != null) {
                int ff = offset - tokenOff;
                if (ff > -1 && ff < text.length()) {
                    String before = text.substring(0, ff);
                    String after = text.substring(ff, text.length());
                    int bo = before.lastIndexOf("${");//NOI18N
                    int bc = before.lastIndexOf("}");//NOI18N
                    int ao = after.indexOf("${");//NOI18N
                    int ac = after.indexOf("}");//NOI18N
                    if (bo > bc && ac > -1 && (ac < ao || ao == -1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int[] getHyperlinkSpan(final Document doc, final int offset, HyperlinkType type) {
        final boolean[] isText = new boolean[1];
        final int[] ftokenOff = new int[1];
        final String[] ftext = new String[1];
        
        doc.render(new Runnable() {

            @Override
            public void run() {
                isText[0] = false;
                TokenHierarchy th = TokenHierarchy.get(doc);
                TokenSequence<XMLTokenId> xml = th.tokenSequence(XMLTokenId.language());
                xml.move(offset);
                xml.moveNext();
                Token<XMLTokenId> token = xml.token();

                // when it's not a value -> do nothing.
                if (token == null) {
                   
                    return;
                }
                if (token.id() == XMLTokenId.TEXT) {
                    isText[0] = true;
                    ftokenOff[0] = xml.offset();
                    ftext[0] = token.text().toString();
                }
            }
        });
               
        if (isText[0]) {
            //we are in element text
            FileObject fo = getProjectDir(doc);
            int tokenOff = ftokenOff[0];
            String text = ftext[0];
            if (fo != null && getPath(fo, text) != null) {
                return new int[] { tokenOff, tokenOff + text.length() };
            }
            // urls get opened..
            if (text != null &&
                    (text.startsWith("http://") || //NOI18N
                    (text.startsWith("https://")))) { //NOI18N
                return new int[] { tokenOff, tokenOff + text.length() };
            }
            if (text != null) {
                Tuple prop = findProperty(text, tokenOff, offset);
                if (prop != null) {
                    return new int[] { prop.spanStart, prop.spanEnd};
                }
            }            
        }
        return null;
    }

    @Override
    public void performClickAction(final Document doc, final int offset, HyperlinkType type) {
        final boolean[] isText = new boolean[1];
        final int[] ftokenOff = new int[1];
        final String[] ftext = new String[1];
        final FileObject fo = getProjectDir(doc);
        doc.render(new Runnable() {

            @Override
            public void run() {
                isText[0] = false;
                TokenHierarchy th = TokenHierarchy.get(doc);
                TokenSequence<XMLTokenId> xml = th.tokenSequence(XMLTokenId.language());
                xml.move(offset);
                xml.moveNext();
                Token<XMLTokenId> token = xml.token();

                // when it's not a value -> do nothing.
                if (token == null) {
                   
                    return;
                }
                if (token.id() == XMLTokenId.TEXT) {
                    isText[0] = true;
                    ftokenOff[0] = xml.offset();
                    ftext[0] = token.text().toString();
                    if (fo != null && getPath(fo, ftext[0]) != null) {
                        xml.movePrevious();
                        token = xml.token();
                        if (token != null && token.id().equals(XMLTokenId.TAG) && TokenUtilities.equals(token.text(), ">")) {//NOI18N
                            xml.movePrevious();
                            token = xml.token();
                            if (token != null && token.id().equals(XMLTokenId.TAG) && TokenUtilities.equals(token.text(), "<module")) {//NOI18N
                                if (!ftext[0].endsWith("/pom.xml")) {
                                    ftext[0] = ftext[0] + "/pom.xml"; //NOI18N
                                }
                            }
                        }
                    }
                }
            }
        });

        if (isText[0]) {
            //we are in element text
            int tokenOff = ftokenOff[0];
            String text = ftext[0];
            
            if (fo != null && getPath(fo, text) != null) {
                FileObject file = getPath(fo, text);
                NodeUtils.openPomFile(file);
            }
            // urls get opened..
            if (text != null &&
                    (text.startsWith("http://") || //NOI18N
                    (text.startsWith("https://")))) { //NOI18N
                try {
                    String urlText = text;
                    if (urlText.contains("${")) {//NOI18N
                        //special case, need to evaluate expression
                        Project nbprj = getProject(doc);
                        if (nbprj != null) {
                            Object exRes;
                            try {
                                exRes = PluginPropertyUtils.createEvaluator(nbprj).evaluate(urlText);
                                if (exRes != null) {
                                    urlText = exRes.toString();
                                }
                            } catch (ExpressionEvaluationException ex) {
                                //just ignore
                                LOG.log(Level.FINE, "Expression evaluation failed", ex);
                            }

                        }
                    }
                    URL url = new URL(urlText);
                    HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                } catch (MalformedURLException ex) {
                    LOG.log(Level.FINE, "malformed url for hyperlink", ex);
                }
            }
            else if (text != null) {
                Tuple tup = findProperty(text, tokenOff, offset);
                if (tup != null) {
                    String prop = tup.value.substring("${".length(), tup.value.length() - 1); //remove the brackets//NOI18N
                    NbMavenProject nbprj = getNbMavenProject(doc);
                    if (nbprj != null) {
                        if (prop != null && (prop.startsWith("project.") || prop.startsWith("pom."))) {//NOI18N
                            String val = prop.substring(prop.indexOf('.') + 1, prop.length());//NOI18N
                            //TODO eventually we want to process everything through an evaluation engine..
                            InputLocation iloc = nbprj.getMavenProject().getModel().getLocation(val);
                            if (iloc != null) {
                                ModelUtils.openAtSource(iloc);
                                return;
                            }
                        }
                        InputLocation propLoc = nbprj.getMavenProject().getModel().getLocation("properties");
                        if (propLoc != null) { //#212984
                            InputLocation location = propLoc.getLocation(prop);
                            if (location != null) {
                                ModelUtils.openAtSource(location);
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return Collections.singleton(HyperlinkType.GO_TO_DECLARATION);
    }


    @Override
    @Messages({
        "# {0} - property name",
        "# {1} - resolved value", 
        "Hint_prop_resolution={0} resolves to ''{1}''\nNavigate to definition.", 
        "Hint_prop_cannot=Cannot resolve expression\nNavigates to definition."})
    public String getTooltipText(final Document doc, final int offset, HyperlinkType type) {

        final boolean[] isText = new boolean[1];
        final int[] ftokenOff = new int[1];
        final String[] ftext = new String[1];
        
        doc.render(new Runnable() {

            @Override
            public void run() {
                isText[0] = false;
                TokenHierarchy th = TokenHierarchy.get(doc);
                TokenSequence<XMLTokenId> xml = th.tokenSequence(XMLTokenId.language());
                xml.move(offset);
                xml.moveNext();
                Token<XMLTokenId> token = xml.token();

                // when it's not a value -> do nothing.
                if (token == null) {
                   
                    return;
                }
                if (token.id() == XMLTokenId.TEXT) {
                    isText[0] = true;
                    ftokenOff[0] = xml.offset();
                    ftext[0] = token.text().toString();
                }
            }
        });
 
        if (isText[0]) {
            //we are in element text
            String text = ftext[0];
            int tokenOff = ftokenOff[0];
            Tuple tup = findProperty(text, tokenOff, offset);

            if (tup != null) {
               String prop = tup.value.substring("${".length(), tup.value.length() - 1); //remove the brackets
                try {
                    Project nbprj = getProject(doc);
                    if (nbprj != null) {
                        Object exRes = PluginPropertyUtils.createEvaluator(nbprj).evaluate(tup.value);
                        if (exRes != null) {
                            return Hint_prop_resolution(prop, exRes);
                        } else {
                        }
                    } else {
                        //pom file in repository or settings file.
                    }
                } catch (ExpressionEvaluationException ex) {
                    return Hint_prop_cannot();
                }
            }  
        }
        return null;
    }
    
    public static void openAtSource(InputLocation location) {
        InputSource source = location.getSource();
        if (source != null && source.getLocation() != null) {
            FileObject fobj = FileUtilities.convertStringToFileObject(source.getLocation());
            if (fobj != null) {
                try {
                    DataObject dobj = DataObject.find(NodeUtils.readOnlyLocalRepositoryFile(fobj));
                    EditCookie edit = dobj.getLookup().lookup(EditCookie.class);
                    if (edit != null) {
                        edit.edit();
                    }
                    LineCookie lc = dobj.getLookup().lookup(LineCookie.class);
                    lc.getLineSet().getOriginal(location.getLineNumber() - 1).show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS, location.getColumnNumber() - 1);
                } catch (DataObjectNotFoundException ex) {
                    LOG.log(Level.FINE, "dataobject not found", ex);
                }
            }
        }
    }
    
    private static class Tuple {
        final int spanStart;
        final int spanEnd;
        final String value;
        public Tuple(String val, int start, int end) {
            this.value = val;
            this.spanStart = start;
            this.spanEnd = end; 
        }
    } 
    
    
    private Tuple findProperty(String textToken, int tokenOffset, int currentOffset) {
        if (textToken == null) {
            return null;
        }
        int ff = currentOffset - tokenOffset;

        if (ff > -1 && ff < textToken.length()) {
            String before = textToken.substring(0, ff);
            String after = textToken.substring(ff, textToken.length());
            int bo = before.lastIndexOf("${");
            int bc = before.lastIndexOf("}");
            int ao = after.indexOf("${");
            int ac = after.indexOf("}");
            if (bo > bc && ac > -1 && (ac < ao || ao == -1)) { //case where currentOffset is on property
                return new Tuple(textToken.substring(bo, before.length() + ac + 1), tokenOffset + bo, tokenOffset + ff + ac + 1);
            }
         
            if (before.length() == 0 && ao == 0 && ac > 0) { //case where currentOffset is at beginning
                return new Tuple(textToken.substring(0, ac + 1), tokenOffset, tokenOffset +  ac + 1);
            }
            
        }
        return null;
    }
    
    private FileObject getProjectDir(Document doc) {
        DataObject dObject = NbEditorUtilities.getDataObject(doc);
        if (dObject != null) {
            return dObject.getPrimaryFile().getParent();
        }
        return null;
    }
    
    private NbMavenProject getNbMavenProject(Document doc) {
        Project prj = getProject(doc);
        if (prj != null) {
            return prj.getLookup().lookup(NbMavenProject.class);
        }
        return null;
    }

    private Project getProject(Document doc) {
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        if (dobj != null) {
            return FileOwnerQuery.getOwner(dobj.getPrimaryFile());
        }
        return null;
    }
    private FileObject getPath(FileObject parent, String path) {
        // TODO more substitutions necessary probably..
        if (path.startsWith("${basedir}/")) { //NOI18N
            path = path.substring("${basedir}/".length()); //NOI18N
        }
        while (path.startsWith("../") && parent.getParent() != null) { //NOI18N
            path = path.substring("../".length()); //NOI18N
            parent = parent.getParent();
        }
        return parent.getFileObject(path);
    }

}
