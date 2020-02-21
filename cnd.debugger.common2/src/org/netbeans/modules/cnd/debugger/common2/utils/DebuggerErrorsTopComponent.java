/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cnd.debugger.common2.utils;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.netbeans.modules.cnd.debugger.common2.utils//DebuggerErrors//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "DebuggerErrorsTopComponent",//NOI18N
        iconBase = "org/netbeans/modules/cnd/debugger/common2/icons/debugger_errors.png",//NOI18N
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false, position = 1455)
@ActionID(category = "Window", id = "org.netbeans.modules.cnd.debugger.common2.utils.DebuggerErrorsTopComponent")
@ActionReference(path = "Menu/Window/Debug" , position = 1550)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DebuggerErrorsAction", //NOI18N
        preferredID = "DebuggerErrorsTopComponent"//NOI18N
)
@Messages({
    "CTL_DebuggerErrorsAction=Debugger Errors",
    "CTL_DebuggerErrorsTopComponent=Debugger Errors",
    "HINT_DebuggerErrorsTopComponent=This is a Debugger Errors window"
})
public final class DebuggerErrorsTopComponent extends TopComponent {
    static final String PREFERRED_ID = "DebuggerErrorsTopComponent";//NOI18N

    public DebuggerErrorsTopComponent() {
        initComponents();
        initTextArea();
        setName(Bundle.CTL_DebuggerErrorsTopComponent());
        setToolTipText(Bundle.HINT_DebuggerErrorsTopComponent());
    }
    
    
    public static synchronized DebuggerErrorsTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win instanceof DebuggerErrorsTopComponent) {
            return (DebuggerErrorsTopComponent) win;
        }
        if (win == null) {
            Logger.getLogger(DebuggerErrorsTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");//NOI18N
        } else {
            Logger.getLogger(DebuggerErrorsTopComponent.class.getName()).warning(
                    "There seem to be multiple components with the '" + PREFERRED_ID//NOI18N
                    + "' ID. That is a potential source of errors and unexpected behavior.");//NOI18N
        }

        DebuggerErrorsTopComponent result = new DebuggerErrorsTopComponent();
        Mode outputMode = WindowManager.getDefault().findMode("output");//NOI18N

        if (outputMode != null) {
            outputMode.dockInto(result);
        }
        return result;
    }    
    
    /*package*/ void setErrorDoc(Document errorDoc) {
        textArea.setDocument(errorDoc);
        //revalidate();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                open();
                //requestActive();
                requestAttention(false);

            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();

        textArea.setColumns(20);
        textArea.setRows(5);
        jScrollPane1.setViewportView(textArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");//NOI18N
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");//NOI18N
        // TODO read your settings according to their version
    }

    private void initTextArea() {
        textArea.setEditable(false);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setBackground((java.awt.Color) javax.swing.UIManager.getDefaults().get("Label.background")); // NOI18N
        textArea.setBorder(BorderFactory.createEmptyBorder());        
        JPopupMenu menu = new JPopupMenu();
        menu.add(new AbstractAction("clear") {//NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textArea.getDocument() == null) {
                    return;
                }
                try {
                    textArea.getDocument().remove(textArea.getDocument().getStartPosition().getOffset(), 
                            textArea.getDocument().getEndPosition().getOffset() -1);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        textArea.setComponentPopupMenu(menu);
    }
}
