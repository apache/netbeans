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
