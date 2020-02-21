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
package org.netbeans.modules.cnd.callgraph.support;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.netbeans.modules.cnd.callgraph.impl.CallGraphScene;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class ExportXmlAction extends AbstractAction implements Presenter.Popup {
    private static final String EXTENSION = "xml"; // NOI18N
    private final CallGraphScene scene;
    private final JComponent parent;
    private final JMenuItem menuItem;
    
    private enum Tag {
        GRAPHML("graphml"), // NOI18N
        GRAPH("graph"), // NOI18N
        NODE("node"), // NOI18N
        EDGE("edge"); // NOI18N
        
        private final String tag;
        
        private Tag(final String tag) {
            this.tag = tag;
        }
        
        @Override
        public String toString() {
            return tag;
        }
    }
    
    private enum TagAtribute {
        ID("id"), // NOI18N
        SOURCE("source"), // NOI18N
        TARGET("target"), // NOI18N
        EDGEDEFAULT("edgedefault"); // NOI18N
        
        private final String attr;
        
        private TagAtribute(final String attr) {
            this.attr = attr;
        }
        
        @Override
        public String toString() {
            return attr;
        }
    }
    
    public ExportXmlAction(final CallGraphScene scene, final JComponent parent) {
        this.scene = scene;
        this.parent = parent;
        putValue(Action.NAME, getString("ExportXmlAction"));  // NOI18N
        menuItem = new JMenuItem(this); 
        Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        print();
    }

    @Override
    public final JMenuItem getPopupPresenter() {
        return menuItem;
    }
    
    private String getString(final String key) {
        return NbBundle.getMessage(getClass(), key);
    }
    
    private void print() {
        if (scene != null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(getString("ExportGraphAsXml")); // NOI18N
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new ExportXmlAction.MyFileFilter());
            if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith("."+EXTENSION)) { // NOI18N
                file = new File(file.getParentFile(), file.getName() + "."+EXTENSION); // NOI18N
            }
            if (file.exists()) {
                String message = getString("FileExistsMessage"); // NOI18N
                DialogDescriptor descriptor = new DialogDescriptor(
                        MessageFormat.format(message, new Object[]{file.getAbsolutePath()}),
                        getString("FileExists"), true, DialogDescriptor.YES_NO_OPTION, DialogDescriptor.NO_OPTION, null); // NOI18N
                Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);

                try {
                    dialog.setVisible(true);
                } catch (Throwable th) {
                    if (!(th.getCause() instanceof InterruptedException)) {
                        throw new RuntimeException(th);
                    }
                    descriptor.setValue(DialogDescriptor.CLOSED_OPTION);
                } finally {
                    dialog.dispose();
                }

                if (descriptor.getValue() != DialogDescriptor.YES_OPTION) {
                    return;
                }
            }
            
            try {
                Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element graphRoot = initXml(xmlDocument);
                
                for (Function func : scene.getNodes()) {
                    Element node = xmlDocument.createElement(Tag.NODE.toString());
                    node.setAttribute(TagAtribute.ID.toString(), func.getScopeName()+func.getName());
                    graphRoot.appendChild(node);
                }
                
                for (Call call : scene.getEdges()) {
                    Element edge = xmlDocument.createElement(Tag.EDGE.toString());
                    edge.setAttribute(TagAtribute.SOURCE.toString(), call.getCallee().getScopeName()+call.getCallee().getName());
                    edge.setAttribute(TagAtribute.TARGET.toString(), call.getCaller().getScopeName()+call.getCaller().getName());
                    graphRoot.appendChild(edge);
                }
                
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                DOMSource source = new DOMSource(xmlDocument);
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);
            } catch (ParserConfigurationException e) {
                e.printStackTrace(System.err);
            } catch (TransformerException e) {
                e.printStackTrace(System.err);
            }
        }
    }
    
    private Element initXml(Document xmlDocument) {
        Element rootElement = xmlDocument.createElement(Tag.GRAPHML.toString());
        rootElement.setAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns"); // NOI18N
        rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        rootElement.setAttribute("xsi:schemaLocation", "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd"); // NOI18N
        xmlDocument.appendChild(rootElement);
        
        Element graphRoot = xmlDocument.createElement(Tag.GRAPH.toString());
        graphRoot.setAttribute(TagAtribute.ID.toString(), parent.getName());
        graphRoot.setAttribute(TagAtribute.EDGEDEFAULT.toString(), "directed"); // NOI18N
        rootElement.appendChild(graphRoot);
        
        return graphRoot;
    }
    
    private static class MyFileFilter extends FileFilter {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            return file.getName().toLowerCase().endsWith("."+EXTENSION); // NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(ExportXmlAction.class, "GraphML"); // NOI18N
        }
    }
    
}
