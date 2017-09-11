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

package org.netbeans.modules.apisupport.project.layers;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.openide.xml.EntityCatalog;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Open the layer file or annotation declaring the SFS node.
 */
public class OpenLayerFilesAction extends CookieAction {

    private static RequestProcessor RP = new RequestProcessor(OpenLayerFilesAction.class.getName());
    
    protected @Override void performAction(final Node[] activatedNodes) {
        RP.post(new Runnable() {
            public @Override void run() {
                    FileObject f = activatedNodes[0].getCookie(DataObject.class).getPrimaryFile();
                    openLayersForFile(f);
            }
        });
    }

    private void openLayersForFile(FileObject f) {
        URL[] location = (URL[]) f.getAttribute("layers"); // NOI18N
        if (location != null) {
            for (URL u : location) {
                FileObject layer = URLMapper.findFileObject(u);
                if (layer != null) {
                    try {
                        openLayerFileAndFind(DataObject.find(layer), f);
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    private static void openLayerFileAndFind(DataObject layerDataObject, final FileObject originalF) {
        try {
            InputSource in = new InputSource(layerDataObject.getPrimaryFile().toURL().toExternalForm());
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            final AtomicInteger line = new AtomicInteger();
            final AtomicReference<String> originatingElement = new AtomicReference<String>("");
            class Handler extends DefaultHandler2 {
                private Locator locator;
                private String path;
                public @Override void setDocumentLocator(Locator l) {
                    locator = l;
                }
                public @Override void startElement(String uri, String localname, String qname, Attributes attr) throws SAXException {
                    if (!qname.matches("file|folder")) { // NOI18N
                        return;
                    }
                    String n = attr.getValue("name"); // NOI18N
                    path = path == null ? n : path + '/' + n;
                    if (line.get() == 0 && originalF.getPath().equals(path)) {
                        line.set(locator.getLineNumber());
                    }
                }
                public @Override void endElement(String uri, String localname, String qname) throws SAXException {
                    if (!qname.matches("file|folder")) { // NOI18N
                        return;
                    }
                    int slash = path.lastIndexOf('/');
                    path = slash == -1 ? null : path.substring(0, slash);
                }
                public @Override void comment(char[] ch, int start, int length) throws SAXException {
                    if (originatingElement.get().isEmpty() && originalF.getPath().equals(path)) {
                        originatingElement.set(new String(ch, start, length));
                    }
                }
                public @Override InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
                    return EntityCatalog.getDefault().resolveEntity(publicId, systemId);
                }
            }
            DefaultHandler2 handler = new Handler();
            parser.getXMLReader().setProperty("http://xml.org/sax/properties/lexical-handler", handler); // NOI18N
            parser.parse(in, handler);
            if (line.get() < 1) {
                return;
            }
            String javaIdentifier = "(?:\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)"; //NOI18N
            if (originatingElement.get().matches(javaIdentifier + "([.]" + javaIdentifier + ")+(\\(\\))?")) {
                if (openOriginatingElement(layerDataObject.getPrimaryFile(), originatingElement.get())) {
                    return;
                }
            }
            EditorCookie editorCookie = layerDataObject.getCookie(EditorCookie.class);
            if (editorCookie != null) {
                editorCookie.open();
                final LineCookie lineCookie = layerDataObject.getCookie(LineCookie.class);
                if (lineCookie != null) {
                    EventQueue.invokeLater(new Runnable() {
                        public @Override void run() {
                            lineCookie.getLineSet().getCurrent(line.get() - 1).show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
                        }
                    });
                }
            }
        } catch (Exception x) {
            Exceptions.printStackTrace(x);
            return;
        }
    }        
    
    private static boolean openOriginatingElement(FileObject layer, final String origEl) throws Exception {
        final Project prj = FileOwnerQuery.getOwner(layer);
        if (prj == null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(OpenLayerFilesAction.class, "OpenLayerFilesAction.msg.no_project", FileUtil.getFileDisplayName(layer)), 1);
            return false;
        }
        final String prjName = ProjectUtils.getInformation(prj).getDisplayName();
        NbModuleProvider nbm = prj.getLookup().lookup(NbModuleProvider.class);
        if (nbm == null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(OpenLayerFilesAction.class, "OpenLayerFilesAction.msg.not_module", prjName), 1);
            return false;
        }
        FileObject src = nbm.getSourceDirectory();
        if (src == null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(OpenLayerFilesAction.class, "OpenLayerFilesAction.msg.no_src_dir", prjName), 1);
            return false;
        }
        final AtomicBoolean success = new AtomicBoolean();
        JavaSource.create(ClasspathInfo.create(src)).runWhenScanFinished(new Task<CompilationController>() {
            public @Override void run(CompilationController cc) throws Exception {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                Element el;
                TypeElement type = cc.getElements().getTypeElement(origEl);
                if (type != null) {
                    el = type;
                } else {
                    PackageElement pkg = cc.getElements().getPackageElement(origEl);
                    if (pkg != null) {
                        el = pkg;
                    } else {
                        int dot = origEl.lastIndexOf('.');
                        String clazz = origEl.substring(0, dot);
                        type = cc.getElements().getTypeElement(clazz);
                        if (type == null) {
                            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(OpenLayerFilesAction.class, "OpenLayerFilesAction.msg.class_not_found", origEl, prjName), 1);
                            return;
                        }
                        String member = origEl.substring(dot + 1);
                        el = null;
                        for (Element nested : type.getEnclosedElements()) {
                            if (nested.toString().equals(member)) {
                                el = nested;
                                break;
                            }
                        }
                        if (el == null) {
                            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(OpenLayerFilesAction.class, "OpenLayerFilesAction.msg.member_not_found", member, clazz), 1);
                            return;
                        }
                    }
                }
                if (ElementOpen.open(cc.getClasspathInfo(), el)) {
                    success.set(true);
                } else {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(OpenLayerFilesAction.class, "OpenLayerFilesAction.msg.could_not_open", origEl), 1);
                }
            }
        }, true).get();
        return success.get();
    }

    public @Override String getName() {
         return NbBundle.getMessage(OpenLayerFilesAction.class, "OpenLayerFilesAction.label");
    }
    
    protected @Override Class<?>[] cookieClasses() {
        return new Class<?>[] {DataObject.class};
    }
    
    protected @Override int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    public @Override HelpCtx getHelpCtx() {
        return null;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }

}
