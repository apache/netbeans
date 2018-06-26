/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * InterceptorPanel.java
 *
 * Created on 06.04.2011, 11:10:22
 */
package org.netbeans.modules.web.beans.actions;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author den
 */
public class InterceptorPanel extends javax.swing.JPanel {
    
    private static final long serialVersionUID = 984990919698645497L;
    
    private static final String JAVA = "java";              // NOI18N

    
    public InterceptorPanel( JButton approveButton, String bindingName, 
            FileObject bindingFileObject ) 
    {
        initComponents();
        myBindingName = bindingName;
        myBindingFileObject = bindingFileObject;
        
        myInterceptorName.getDocument().addDocumentListener( 
                createValidationListener( approveButton ) );
        myInterceptorName.setText( getProposedName() );
        URL errorUrl;
        try {
            errorUrl = new URL("nbresloc:/org/netbeans/modules/dialogs/error.gif");
            myStatusLbl.setIcon( new ImageIcon( errorUrl ));
        }
        catch (MalformedURLException e) {
            assert false;
        }
        
        myStatusLbl.setVisible( false );
    }
    
    String getInterceptorName(){
        return myInterceptorName.getText();
    }
    
    private DocumentListener createValidationListener(final JButton button ) {
        DocumentListener listener = new DocumentListener() {
            
            @Override
            public void removeUpdate( DocumentEvent e ) {
                checkName(e);
            }
            
            @Override
            public void insertUpdate( DocumentEvent e ) {
                checkName(e);
            }
            
            @Override
            public void changedUpdate( DocumentEvent e ) {
                checkName(e);                
            }
            
            private void checkName(DocumentEvent e){
                try {
                    String text = e.getDocument().getText(0, 
                            e.getDocument().getLength());
                    if ( text == null || text.trim().length() == 0 ||
                            !Utilities.isJavaIdentifier( text ))
                    {
                        myStatusLbl.setText(NbBundle.getMessage(
                                InterceptorGenerator.class, 
                                "LBL_InvalidInterceptorName", text ));
                        myStatusLbl.setVisible( true );
                        button.setEnabled( false );
                        return;
                    }
                    FileObject packageFolder = myBindingFileObject.getParent();
                    if ( packageFolder == null ){
                        return;
                    }
                    FileObject file = packageFolder.getFileObject( text,JAVA);
                    if ( file != null ){
                        myStatusLbl.setText(NbBundle.getMessage(
                                InterceptorGenerator.class, 
                                "LBL_FileExists", file.getNameExt() ));
                        myStatusLbl.setVisible( true );
                        button.setEnabled( false );
                        return;
                    }
                    
                    myStatusLbl.setText("");
                    myStatusLbl.setVisible( false );
                    button.setEnabled( true );
                }
                catch (BadLocationException ex ) {
                    /*
                     *  should be never appear because text access is done inside
                     *  event handling
                     */
                    assert false;
                }
            }
        };
        return listener;
    }
    
    private String getProposedName() {
        StringBuilder result = new StringBuilder();
        if ( myBindingName.endsWith(InterceptorFactory.INTERCEPTOR_BINDING)){
            result.append( myBindingName.substring(0, myBindingName.length() -
                    InterceptorFactory.INTERCEPTOR_BINDING.length() ));
        }
        else {
            result.append( myBindingName );
        }
        result.append("Interceptor");               // NOI18N
        
        FileObject packageFolder = myBindingFileObject.getParent();
        if ( packageFolder == null ){
            return result.toString();
        }
        int index = 1;
        String next = result.toString();
        while( packageFolder.getFileObject( next, JAVA) != null ){
            next = result.toString()+index;
            index++;
        }
        return result.toString();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myInterceptorNameLbl = new javax.swing.JLabel();
        myInterceptorName = new javax.swing.JTextField();
        myStatusLbl = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(myInterceptorNameLbl, org.openide.util.NbBundle.getMessage(InterceptorPanel.class, "LBL_InterceptorName")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(myInterceptorNameLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(myInterceptorName, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE))
                    .addComponent(myStatusLbl))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(myInterceptorNameLbl)
                    .addComponent(myInterceptorName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(myStatusLbl)
                .addContainerGap())
        );

        myInterceptorNameLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InterceptorPanel.class, "ACSN_InterceptorName")); // NOI18N
        myInterceptorNameLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InterceptorPanel.class, "ACSD_InterceptorName")); // NOI18N
        myInterceptorName.getAccessibleContext().setAccessibleName(myInterceptorNameLbl.getAccessibleContext().getAccessibleName());
        myInterceptorName.getAccessibleContext().setAccessibleDescription(getAccessibleContext().getAccessibleDescription());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField myInterceptorName;
    private javax.swing.JLabel myInterceptorNameLbl;
    private javax.swing.JLabel myStatusLbl;
    // End of variables declaration//GEN-END:variables
    
    private String myBindingName; 
    private FileObject myBindingFileObject;
    
}
