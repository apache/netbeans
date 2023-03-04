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

package org.netbeans.modules.apisupport.refactoring;

import java.io.IOException;
import java.io.StringReader;
import javax.swing.text.Document;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.xml.EntityCatalog;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Milos Kleint - inspired by j2eerefactoring
 */
public class NbWhereUsedRefactoringPlugin extends AbstractRefactoringPlugin {
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    public void cancelRequest() {
        
    }
    
    public Problem fastCheckParameters() {
        return null;
    }
    
    
    /**
     * Creates a new instance of NbWhereUsedRefactoringPlugin
     */
    public NbWhereUsedRefactoringPlugin(AbstractRefactoring refactoring) {
        super(refactoring);
    }
    
    
    /** Collects refactoring elements for a given refactoring.
     * @param refactoringElements Collection of refactoring elements - the implementation of this method
     * should add refactoring elements to this collections. It should make no assumptions about the collection
     * content.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (semafor.get() != null) {
            return null;
        }
        semafor.set(new Object());
        try {
            WhereUsedQuery whereUsedRefactor = ((WhereUsedQuery)refactoring);
            
            if (!whereUsedRefactor.getBooleanValue(WhereUsedQuery.FIND_REFERENCES)) {
                return null;
            }
            
            Problem problem = null;
            Lookup lkp = whereUsedRefactor.getRefactoringSource();
            InfoHolder infoholder = examineLookup(lkp);
            final TreePathHandle handle = lkp.lookup(TreePathHandle.class);
            
            Project project = FileOwnerQuery.getOwner(handle.getFileObject());
            if (project == null || project.getLookup().lookup(NbModuleProvider.class) == null) {
                // take just netbeans module development into account..
                return null;
            }
            
            if (infoholder.isClass) {
                checkManifest(project, infoholder.fullName, refactoringElements);
                checkLayer(project, infoholder.fullName, refactoringElements);
            }
            if (infoholder.isMethod) {
                checkMethodLayer(infoholder, handle.getFileObject(), refactoringElements);
            }
            if (infoholder.isConstructor) {
                checkConstructorLayer(infoholder, handle.getFileObject(), refactoringElements);
            }
            return problem;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        finally {
            semafor.set(null);
        }
    }

    protected RefactoringElementImplementation createManifestRefactoring(
            String fqname,
            FileObject manifestFile,
            String attributeKey,
            String attributeValue,
            String section) {
        return new ManifestWhereUsedRefactoringElement(attributeValue, manifestFile,
                attributeKey, section);
    }

    protected RefactoringElementImplementation createLayerRefactoring(String fqname,
            LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        FileObject fo = handle.getLayerFile();
        return fo != null ? new LayerWhereUsedRefactoringElement(fo, layerFileObject, layerAttribute) : null;
    }
    
    protected RefactoringElementImplementation createMethodLayerRefactoring(String method, String fqname,
            LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        FileObject fo = handle.getLayerFile();
        return fo != null ? new LayerWhereUsedRefactoringElement(fo, layerFileObject, layerAttribute) : null;
    }
    
    protected RefactoringElementImplementation createConstructorLayerRefactoring(String constructor, String fqname,
            LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        FileObject fo = handle.getLayerFile();
        return fo != null ? new LayerWhereUsedRefactoringElement(fo, layerFileObject, layerAttribute) : null;
    }
        
    public final class LayerWhereUsedRefactoringElement extends AbstractRefactoringElement {
        private String attr;
        private String path;
        private String attrValue;
        public LayerWhereUsedRefactoringElement(FileObject fo, FileObject layerFo, String attribute) {
            super(fo);
            attr = attribute;
            this.path = layerFo.getPath();
            if (attr != null) {
                Object vl = layerFo.getAttribute("literal:" + attr); //NOI18N
                if (vl instanceof String) {
                    attrValue = ((String) vl).replaceFirst("^(new|method):", ""); // NOI18N
                }
            }
        }
        public String getDisplayText() {
            if (attr != null && attrValue != null) {
                return NbBundle.getMessage(NbWhereUsedRefactoringPlugin.class, "TXT_LayerAttrValueWhereUsed", path, attr, attrValue);
            }
            if (attr != null) {
                return NbBundle.getMessage(NbWhereUsedRefactoringPlugin.class, "TXT_LayerAttrWhereUsed", path, attr);
            }
            return NbBundle.getMessage(NbWhereUsedRefactoringPlugin.class, "TXT_LayerWhereUsed", path);
        }
        protected int[] location() {
            try {
                DataObject d = DataObject.find(parentFile);
                EditorCookie ec = (EditorCookie) d.getCookie(EditorCookie.class);
                Document doc = ec.openDocument();
                String text = doc.getText(0, doc.getLength());
                assert text.indexOf('\r') == -1; // should be in newline format only when a Document
                InputSource in = new InputSource(new StringReader(text));
                in.setSystemId(parentFile.toURL().toExternalForm());
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                final int[] lineAndColStartAndEnd = new int[4];
                class Halt extends SAXException {
                    public Halt() {
                        super((String) null);
                    }
                }
                class Handler extends DefaultHandler {
                    private int state = -1; // -1 - not encountered (or already found it but Halt did not work), 0 - in matching element, 1+ - in nested element
                    private Locator locator;
                    private String runningPath = "";
                    public void setDocumentLocator(Locator l) {
                        locator = l;
                    }
                    public void startElement(String uri, String localname, String qname, Attributes attr) throws SAXException {
                        if (qname.equals("file") || qname.equals("folder")) { // NOI18N
                            String name = attr.getValue("name"); // NOI18N
                            if (name != null) {
                                if (runningPath.length() > 0) {
                                    runningPath += '/';
                                }
                                runningPath += name;
                            }
                        }
                        if (state == -1 && path.equals(runningPath)) {
                            lineAndColStartAndEnd[0] = locator.getLineNumber();
                            lineAndColStartAndEnd[1] = locator.getColumnNumber();
                            state = 0;
                        } else if (state != -1) {
                            state++;
                        }
                    }
                    public void endElement(String uri, String localname, String qname) throws SAXException {
                        if (qname.equals("file") || qname.equals("folder")) { // NOI18N
                            runningPath = runningPath.substring(0, Math.max(runningPath.lastIndexOf('/'), 0));
                        }
                        if (state > 0) {
                            state--;
                        } else if (state == 0) {
                            lineAndColStartAndEnd[2] = locator.getLineNumber();
                            lineAndColStartAndEnd[3] = locator.getColumnNumber();
                            state = -1;
                            throw new Halt();
                        }
                    }
                    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
                        try {
                            return EntityCatalog.getDefault().resolveEntity(publicId, systemId);
                        } catch (IOException e) {
                            throw new SAXException(e);
                        }
                    }
                }
                try {
                    parser.parse(in, new Handler());
                } catch (Halt h) {
                    // ignore
                }
                if (lineAndColStartAndEnd[0] == 0 || lineAndColStartAndEnd[1] == 0 || lineAndColStartAndEnd[2] == 0 || lineAndColStartAndEnd[3] == 0) {
                    return new int[] {0, 0};
                }
                int[] startAndEnd = new int[2];
                int line = 0;
                int col = 0;
                for (int i = 0; i < text.length(); i++) {
                    if (line == lineAndColStartAndEnd[0] - 1 && col == lineAndColStartAndEnd[1] - 1) {
                        startAndEnd[0] = i;
                    } else if (line == lineAndColStartAndEnd[2] - 1 && col == lineAndColStartAndEnd[3] - 1) {
                        startAndEnd[1] = i;
                    }
                    char c = text.charAt(i);
                    if (c == '\n') {
                        line++;
                        col = 0;
                    } else {
                        col++;
                    }
                }
                // Start position given by SAX locator may actually be *end* of open tag, which is not good.
                // Try to backtrack to opening '<'. Shouldn't be any other '<' in an XML element.
                startAndEnd[0] = Math.max(text.lastIndexOf('<', startAndEnd[0]), 0);
                if (startAndEnd[1] == 0) {
                    // Minimized tag. Guess that unescaped '>' will not occur in the value.
                    startAndEnd[1] = Math.max(text.indexOf('>', startAndEnd[0]), startAndEnd[0]);
                }
                // Right now we have the containing file object. Prefer to get the actual string.
                String match;
                if (attrValue != null) {
                    match = attrValue;
                } else {
                    match = path.substring(path.lastIndexOf('/') + 1);
                }
                int loc = text.indexOf(match, startAndEnd[0]);
                if (loc != -1 && loc < startAndEnd[1]) {
                    // Found it.
                    return new int[] {loc, loc + match.length()};
                } else {
                    // OK, just show the whole <file>.
                    return startAndEnd;
                }
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return new int[] {0, 0};
            }
        }
    }

    
    public final class ManifestWhereUsedRefactoringElement extends AbstractRefactoringElement {
        
        private String attrName;
        private String sectionName = null;
        public ManifestWhereUsedRefactoringElement(String name, FileObject parentFile, String attributeName) {
            super(parentFile);
            this.name = name;
            attrName = attributeName;
        }
        public ManifestWhereUsedRefactoringElement(String name, FileObject parentFile, String attributeName, String secName) {
            this(name, parentFile, attributeName);
            sectionName = secName;
        }
        
        public String getDisplayText() {
            if (sectionName != null) {
                return NbBundle.getMessage(NbWhereUsedRefactoringPlugin.class, "TXT_ManifestSectionWhereUsed", this.name, sectionName);
            }
            return NbBundle.getMessage(NbWhereUsedRefactoringPlugin.class, "TXT_ManifestWhereUsed", this.name, attrName);
        }

        protected int[] location() {
            try {
                DataObject d = DataObject.find(parentFile);
                EditorCookie ec = (EditorCookie) d.getCookie(EditorCookie.class);
                Document doc = ec.openDocument();
                String text = doc.getText(0, doc.getLength());
                assert text.indexOf('\r') == -1; // should be in newline format only when a Document
                int start = text.indexOf(name);
                if (start == -1) {
                    return new int[] {0, 0};
                } else {
                    return new int[] {start, start + name.length()};
                }
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return new int[] {0, 0};
            }
        }
        
    }
    
}
