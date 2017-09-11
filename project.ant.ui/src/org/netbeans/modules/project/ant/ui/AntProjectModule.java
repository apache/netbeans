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
