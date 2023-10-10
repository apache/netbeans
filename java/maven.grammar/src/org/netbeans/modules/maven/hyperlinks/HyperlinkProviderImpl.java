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
package org.netbeans.modules.maven.hyperlinks;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.apache.maven.project.MavenProject;
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
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
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
import org.openide.filesystems.FileUtil;

/**
 * adds hyperlinking support to pom.xml files..
 *
 * @author mkleint
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = Constants.POM_MIME_TYPE, service = HyperlinkProviderExt.class, position = 1000)
    ,
    @MimeRegistration(mimeType = POMDataObject.SETTINGS_MIME_TYPE, service = HyperlinkProviderExt.class, position = 1000)
})
public class HyperlinkProviderImpl implements HyperlinkProviderExt {

    private static final Logger LOG = Logger.getLogger(HyperlinkProviderImpl.class.getName());

    @Override
    public boolean isHyperlinkPoint(final Document doc, final int offset, HyperlinkType type) {
        final PomHyperlinkInfo hyperLinkInfo = new PomHyperlinkInfo(doc, offset);
        doc.render(new PomParserRunnable(hyperLinkInfo, doc, offset));

        return hyperLinkInfo.isHyperlinkPoint();
    }

    @Override
    public int[] getHyperlinkSpan(final Document doc, final int offset, HyperlinkType type) {
        final PomHyperlinkInfo hyperLinkInfo = new PomHyperlinkInfo(doc, offset);
        doc.render(new PomParserRunnable(hyperLinkInfo, doc, offset));

        return hyperLinkInfo.getHyperLinkSpan();
    }

    @Override
    public void performClickAction(final Document doc, final int offset, HyperlinkType type) {
        final PomHyperlinkInfo hyperLinkInfo = new PomHyperlinkInfo(doc, offset);
        doc.render(new PomParserRunnable(hyperLinkInfo, doc, offset));

        hyperLinkInfo.performClickAction();
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
        final PomHyperlinkInfo hyperLinkInfo = new PomHyperlinkInfo(doc, offset);
        doc.render(new PomParserRunnable(hyperLinkInfo, doc, offset));
        String[] tooltip = hyperLinkInfo.getTooltipText();

        if (tooltip == null) {
            return Hint_prop_cannot();
        } else if (tooltip.length == 2) {
            return Hint_prop_resolution(tooltip[0], tooltip[1]);
        } else {
            return tooltip[0];
        }
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
            String after = textToken.substring(ff);
            int bo = before.lastIndexOf("${");
            int bc = before.lastIndexOf("}");
            int ao = after.indexOf("${");
            int ac = after.indexOf("}");
            if (bo > bc && ac > -1 && (ac < ao || ao == -1)) { //case where currentOffset is on property
                return new Tuple(textToken.substring(bo, before.length() + ac + 1), tokenOffset + bo, tokenOffset + ff + ac + 1);
            }

            if (before.length() == 0 && ao == 0 && ac > 0) { //case where currentOffset is at beginning
                return new Tuple(textToken.substring(0, ac + 1), tokenOffset, tokenOffset + ac + 1);
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

    private class PomParserRunnable implements Runnable {

        private final PomHyperlinkInfo hyperLinkInfo;
        private final Document document;
        private final int offset;

        public PomParserRunnable(PomHyperlinkInfo hyperLinkInfo, Document document, int offset) {
            this.hyperLinkInfo = hyperLinkInfo;
            this.document = document;
            this.offset = offset;
        }

        @Override
        public void run() {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence<XMLTokenId> xml = th.tokenSequence(XMLTokenId.language());
            xml.move(offset);
            xml.moveNext();
            Token<XMLTokenId> token = xml.token();

            // when it's not a value -> do nothing.
            if (token == null) {
                return;
            }

            if (token.id() == XMLTokenId.TEXT) {
                hyperLinkInfo.calculateInfo(token, xml);
            }
        }
    }

    private class PomHyperlinkInfo {

        final Document doc;
        final int documentOffset;
        final FileObject projectFileObject;
        boolean isText;
        int ftokenOff;
        String ftext;

        String artifactId;
        String groupId;
        String version;
        String type;

        public PomHyperlinkInfo(Document doc, int documentOffset) {
            this.doc = doc;
            this.documentOffset = documentOffset;
            this.projectFileObject = getProjectDir(doc);
        }

        boolean isHyperlinkUrl() {
            return ftext != null
                    && (ftext.startsWith("http://")
                    || //NOI18N
                    ftext.startsWith("https://")); //NOI18N;
        }

        private FileObject getFileSystemLinkObject() {
            FileObject fo = getProjectDir(doc);
            if (fo != null && ftext != null) {
                return getPath(fo, ftext) ;
            }
            return null;
        }

        boolean isMavenProperty() {
            if (ftext != null) {
                int ff = documentOffset - ftokenOff;
                if (ff > -1 && ff < ftext.length()) {
                    String before = ftext.substring(0, ff);
                    String after = ftext.substring(ff);
                    int bo = before.lastIndexOf("${");//NOI18N
                    int bc = before.lastIndexOf("}");//NOI18N
                    int ao = after.indexOf("${");//NOI18N
                    int ac = after.indexOf("}");//NOI18N
                    if (bo > bc && ac > -1 && (ac < ao || ao == -1)) {
                        return true;
                    }
                }
            }
            return false;
        }

        boolean isMavenDependency() {
            return artifactId != null && groupId != null && version != null;
        }

        boolean isHyperlinkPoint() {
            return (isHyperlinkUrl() || getFileSystemLinkObject() != null || 
                    isMavenProperty() || (isMavenDependency() && getMavenArtifactAbsolutePomPath() != null));
        }

        private void calculateInfo(Token<XMLTokenId> token, TokenSequence<XMLTokenId> xml) {
            isText = token.id() == XMLTokenId.TEXT;
            if (isText) {
                ftokenOff = xml.offset();
                ftext = token.text().toString();

                if (projectFileObject != null && getPath(projectFileObject, ftext) != null) {
                    xml.movePrevious();
                    token = xml.token();
                    if (token != null && token.id().equals(XMLTokenId.TAG) && TokenUtilities.equals(token.text(), ">")) {//NOI18N
                        xml.movePrevious();
                        token = xml.token();
                        if (token != null && token.id().equals(XMLTokenId.TAG)) {
                            if (TokenUtilities.equals(token.text(), "<module")) {//NOI18N
                                if (!ftext.endsWith("/pom.xml")) {
                                    ftext += "/pom.xml"; //NOI18N
                                }
                            }
                        }
                    }
                } else {
                    xml.movePrevious();
                    token = xml.token();
                    if (token != null && token.id().equals(XMLTokenId.TAG) && TokenUtilities.equals(token.text(), ">")) { //NOI18N
                        xml.movePrevious();
                        String tokenString = xml.token().text().toString();
                        if ("<artifactId".equals(tokenString) || //NOI18N
                                "<groupId".equals(tokenString) || //NOI18N
                                "<type".equals(tokenString) || //NOI18N
                                ("<version".equals(tokenString) && !ftext.startsWith("${"))) { 
                            
                            resetSequenceToDependencytagToken(xml);
                            
                            if (TokenUtilities.equals(xml.token().text(), "<dependency")) {          //NOI18N
                                
                                while (!TokenUtilities.equals(xml.token().text(), "</dependency")) { //NOI18N
                                    
                                    switch (xml.token().text().toString()) {
                                        case "<artifactId":
                                            moveToXmlTokenById(xml, XMLTokenId.TEXT);
                                            token = xml.token();
                                            artifactId = token.text().toString();
                                            break;
                                        case "<groupId":
                                            moveToXmlTokenById(xml, XMLTokenId.TEXT);
                                            token = xml.token();
                                            groupId = token.text().toString();
                                            break;
                                        case "<version":
                                            moveToXmlTokenById(xml, XMLTokenId.TEXT);
                                            token = xml.token();
                                            if (TokenUtilities.startsWith(token.text(), "${")) { //NOI18N
                                                Project nbprj = getProject(doc);
                                                if (nbprj != null) {
                                                    try {
                                                        version = (String) PluginPropertyUtils.createEvaluator(nbprj).evaluate(token.text().toString());
                                                    } catch (ExpressionEvaluationException eee) {
                                                        LOG.log(Level.INFO, "Unable to evaluate property: " + token.text().toString(), eee);
                                                    }
                                                }
                                            } else {
                                                version = token.text().toString();
                                            }
                                            break;
                                        case "<type" :
                                            moveToXmlTokenById(xml, XMLTokenId.TEXT);
                                            token = xml.token();
                                            type = token.text().toString();
                                            break;
                                    }
                                    xml.moveNext();
                                    moveToXmlTokenById(xml, XMLTokenId.TAG);
                                }
                                // handle cases where the version element is covered in a 
                                // parent pom/dependenciesManagement
                                if (version == null) {
                                    NbMavenProject projectForDocument = getNbMavenProject(doc);
                                    if (projectForDocument != null) {
                                        MavenProject mavenProject = projectForDocument.getMavenProject();
                                        for (Artifact artifact : mavenProject.getArtifacts()) {
                                            if (artifact.getGroupId().equals(groupId)
                                                    && artifact.getArtifactId().equals(artifactId)) {
                                                version = artifact.getVersion();
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        String[] getTooltipText() {
            if (isText) {
                //we are in element text
                String text = ftext;
                int tokenOff = ftokenOff;
                if (isMavenProperty()) {
                    Tuple tup = findProperty(text, tokenOff, documentOffset);

                    if (tup != null) {
                        String prop = tup.value.substring("${".length(), tup.value.length() - 1); //remove the brackets
                        try {
                            Project nbprj = getProject(doc);
                            if (nbprj != null) {
                                Object exRes = PluginPropertyUtils.createEvaluator(nbprj).evaluate(tup.value);
                                if (exRes != null) {
                                    return new String[]{prop, (String) exRes};
                                }
                            } else {
                                //pom file in repository or settings file.
                            }
                        } catch (ExpressionEvaluationException ex) {
                            return null;
                        }
                    }
                } else if (isMavenDependency()) {
                    return new String[]{getMavenArtifactAbsolutePomPath()};
                }
            }
            return null;
        }

        String getMavenArtifactAbsolutePomPath() {
            if (!isMavenDependency()) {
                return null;
            } else {
                MavenEmbedder embedder = EmbedderFactory.getProjectEmbedder();
                Artifact mavenArtifact = embedder.createArtifact(groupId, artifactId, version, type == null ? "jar" : type);
                String mavenPomPath = embedder.getLocalRepository().find(mavenArtifact).getFile().getAbsolutePath().replace(".jar", ".pom");
                if (mavenPomPath == null) {
                    return null;
                }
                return new File(mavenPomPath).exists() ? mavenPomPath : null;
            }
        }

        private int[] getHyperLinkSpan() {
            if (isText) {
                //we are in element text
                FileObject fo = getProjectDir(doc);
                if (fo != null && getPath(fo, ftext) != null) {
                    return new int[]{ftokenOff, ftokenOff + ftext.length()};
                }
                // urls get opened..
                if (isHyperlinkUrl()) {
                    return new int[]{ftokenOff, ftokenOff + ftext.length()};
                }
                if (ftext != null) {
                    Tuple prop = findProperty(ftext, ftokenOff, documentOffset);
                    if (prop != null) {
                        return new int[]{prop.spanStart, prop.spanEnd};
                    }
                }
                if (isMavenDependency()) {
                    return new int[]{ftokenOff, ftokenOff + ftext.length()};
                }
            }
            return null;
        }

        private void resetSequenceToDependencytagToken(TokenSequence<XMLTokenId> xml) {
            while (!TokenUtilities.equals("<dependency", xml.token().text())             //NOI18N
                    && !TokenUtilities.equals("<plugin", xml.token().text())             //NOI18N
                    && !TokenUtilities.equals("<parent", xml.token().text())             //NOI18N
                    && !TokenUtilities.equals("<exclusion", xml.token().text())             //NOI18N
                    && !TokenUtilities.equals("<project", xml.token().text())) {         //NOI18N
                xml.movePrevious();
            }
        }

        private void performClickAction() {
            if (isText) {
                //we are in element text
                int tokenOff = ftokenOff;
                String text = ftext;
                FileObject fileSystemLinkObject = getFileSystemLinkObject();
                if (fileSystemLinkObject != null) {
                    NodeUtils.openPomFile(fileSystemLinkObject);
                } else if (isHyperlinkUrl()) {
                    // urls get opened..
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
                } else if (isMavenProperty()) {
                    Tuple tup = findProperty(text, tokenOff, documentOffset);
                    if (tup != null) {
                        String prop = tup.value.substring(2, tup.value.length() - 1);
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
                } else if (isMavenDependency()) {
                    File pomFile = new File(getMavenArtifactAbsolutePomPath());
                    FileObject fileToOpen = FileUtil.toFileObject(pomFile);
                    if (fileToOpen != null) {
                        NodeUtils.openPomFile(fileToOpen);
                    }
                }
            }
        }

        private void moveToXmlTokenById(TokenSequence<XMLTokenId> xml, XMLTokenId tokenId) {
            while (xml.token() != null && !xml.token().id().equals(tokenId)) {
                xml.moveNext();
            }
        }

    }
}
