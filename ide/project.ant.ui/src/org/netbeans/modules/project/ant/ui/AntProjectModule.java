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

package org.netbeans.modules.project.ant.ui;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.CharArrayWriter;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Checks for buggy versions of Xalan.
 * @author Jan Lahoda
 * @see "issue #70130"
 */
public class AntProjectModule extends ModuleInstall {
    
    public @Override void restored() {
        super.restored();
        
        if (Boolean.getBoolean("netbeans.do.not.check.xalan")) // NOI18N
            return ;
        
        long start = System.currentTimeMillis();
        boolean isBuggyXalan = checkForXalan();
        long end = System.currentTimeMillis();
        Logger.getLogger(AntProjectModule.class.getName()).log(Level.FINE, "check for buggy xalan took %d", end - start); // NOI18N
        
        if (isBuggyXalan) {
            showWarning();
        }
    }
    
    private boolean checkForXalan() {
        //check for a buggy xalan on the classpath and warn if necessary:
        //try to load org.apache.xalan.Version class, OK if it does not exist:
        try {
            try {
                XMLUtil.class.getClassLoader().loadClass("org.apache.xalan.Version"); // NOI18N
            } catch (ClassNotFoundException ex) {
                //ok, no xalan, everything is OK.
                return false;
            }
            return !verifyWriterCorrect();
        } catch (Exception ex) {
            //should not happen, but probably OK:
            Logger.getLogger(AntProjectModule.class.getName()).log(Level.INFO, "Cannot run JAXP", ex);
        } catch (Error e) {
            Logger.getLogger(AntProjectModule.class.getName()).log(Level.SEVERE, "Cannot run JAXP", e);
        }
        
        return false;
    }
    
    private boolean verifyWriterCorrect() throws Exception {
        final String IDENTITY_XSLT_WITH_INDENT =
                "<xsl:stylesheet version='1.0' " + // NOI18N
                "xmlns:xsl='http://www.w3.org/1999/XSL/Transform' " + // NOI18N
                "xmlns:xalan='http://xml.apache.org/xslt' " + // NOI18N
                "exclude-result-prefixes='xalan'>" + // NOI18N
                "<xsl:output method='xml' indent='yes' xalan:indent-amount='4'/>" + // NOI18N
                "<xsl:template match='@*|node()'>" + // NOI18N
                "<xsl:copy>" + // NOI18N
                "<xsl:apply-templates select='@*|node()'/>" + // NOI18N
                "</xsl:copy>" + // NOI18N
                "</xsl:template>" + // NOI18N
                "</xsl:stylesheet>"; // NOI18N
        String data = "<root xmlns='root'/>"; // NOI18N
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(data)));
        doc.getDocumentElement().appendChild(doc.createElementNS("child", "child")); // NOI18N
        Transformer t = TransformerFactory.newInstance().newTransformer(
                new StreamSource(new StringReader(IDENTITY_XSLT_WITH_INDENT)));
        Source source = new DOMSource(doc);
        CharArrayWriter output = new CharArrayWriter();
        Result result = new StreamResult(output);
        t.transform(source, result);
        
        output.close();
        
        String text = output.toString();
        
        return text.indexOf("\"child\"") != (-1) || text.indexOf("'child'") != (-1); // NOI18N
    }

    private void showWarning() {
        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(AntProjectModule.class, "LBL_Incompatible_Xalan")); // NOI18N
        
        DialogDisplayer.getDefault().notify(nd);
        
        //the IDE cannot be closed here (the window system data are corrupted then), wait until the main window appears
        //and close then:
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Frame f = WindowManager.getDefault().getMainWindow();
                
                if (f == null || f.isShowing()) {
                    LifecycleManager.getDefault().exit();
                } else {
                    f.addWindowListener(new WindowAdapter() {
                        public @Override void windowOpened(WindowEvent e) {
                            LifecycleManager.getDefault().exit();
                        }
                    });
                }
            }
        });
    }
    
}
