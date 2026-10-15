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

/*
 * PoliciesVisualPanel.java
 *
 * Created on 22.07.2011, 15:11:27
 */
package org.netbeans.modules.websvc.owsm;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ListUI;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author den
 */
public class PoliciesVisualPanel extends javax.swing.JPanel {
    
    private static final long serialVersionUID = -1918005169365122961L;
    
    static final String OWSM_SECURITY_POLICY = 
        "weblogic.wsee.jws.jaxws.owsm.SecurityPolicy";      // NOI18N
    
    static final String OWSM_SECURITY_POLICIES = 
        "weblogic.wsee.jws.jaxws.owsm.SecurityPolicies";    // NOI18N
    
    static final String VALUE = "value";                    // NOI18N
    static final String URI = "uri";                        // NOI18N
    
    static final RequestProcessor JAVA_PROCESSOR = 
        new RequestProcessor( PoliciesVisualPanel.class);

    /** Creates new form PoliciesVisualPanel */
    public PoliciesVisualPanel( final List<String> policyIds , 
            FileObject javaFile ) 
    {
        myWsJavaFile = javaFile;
        
        final JavaSource javaSource = JavaSource.forFileObject(javaFile );
        if ( javaSource == null ){
            showLabel( NbBundle.getMessage( PoliciesVisualPanel.class, 
                    "ERR_NoWSAccess"));         // NOI18N
            return;
        }
        else {
            showLabel( NbBundle.getMessage( PoliciesVisualPanel.class, 
                "LBL_Wait"));         // NOI18N
        }
        
        JAVA_PROCESSOR.post( new Runnable() {
            
            @Override
            public void run() {
                final Set<String> chosenIds = getWsPolicyIds( javaSource );
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        initUI(policyIds, chosenIds );
                    }
                });
                
            }
        });
        
    }
    
    List<String> getPolicyIds(){
        return myRenderer.getPolicyIds();
    }
    
    String getWsFqn(){
        return wsFqn;
    }

    private void showLabel( String message ) {
        setLayout(new FlowLayout(FlowLayout.CENTER){
            private static final long serialVersionUID = -1572219951957864402L;

            /* (non-Javadoc)
             * @see java.awt.FlowLayout#layoutContainer(java.awt.Container)
             */
            @Override
            public void layoutContainer( Container target ) {
                super.layoutContainer(target);
                Component[] components = target.getComponents();
                double height = target.getSize().getHeight()/2;
                for (Component component : components) {
                    Point location = component.getLocation();
                    component.setLocation( (int)location.getX(), (int)height );
                }
            }
        });
        add( new JLabel( message ));
    }


    private Set<String> getWsPolicyIds(JavaSource wsJava ) {
        final Set<String> result = new HashSet<String>();
        try {
            wsJava.runUserActionTask( new Task<CompilationController>() {
                
                @Override
                public void run( CompilationController controller ) throws Exception {
                    controller.toPhase( Phase.ELEMENTS_RESOLVED);
                    List<? extends TypeElement> topLevelElements = 
                        controller.getTopLevelElements();
                    
                    if ( topLevelElements.size() == 1){
                        isWebService( topLevelElements.get( 0 ), controller , 
                                result );
                        wsFqn = topLevelElements.get( 0 ).getQualifiedName().toString();
                        return;
                    }
                    for (TypeElement typeElement : topLevelElements) {
                        Set<Modifier> modifiers = typeElement.getModifiers();
                        if ( !modifiers.contains( Modifier.PUBLIC )){
                            continue;
                        }
                        if ( isWebService(typeElement, controller ,result )){
                            wsFqn = typeElement.getQualifiedName().toString();
                            break;
                        }
                    }
                }
            } , true );
        }
        catch (IOException e) {
            Logger.getLogger( PoliciesVisualPanel.class.getName() ).log( 
                    Level.INFO , null,e);
        }
        return result;
    }

    private boolean isWebService( TypeElement typeElement, 
            CompilationController controller, Set<String> result )
    {
        boolean isWebService = false;
        List<? extends AnnotationMirror> allAnnotationMirrors = 
            controller.getElements().getAllAnnotationMirrors( typeElement );
        for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
            Element annotationElement = annotationMirror.getAnnotationType().asElement();
            if ( annotationElement instanceof TypeElement ){
                String fqn = ((TypeElement)annotationElement).getQualifiedName(). toString();
                if ( "javax.jws.WebService".equals( fqn )){
                    isWebService = true;
                }
                else if ( OWSM_SECURITY_POLICY.equals( fqn )){
                    addId(result, annotationMirror);
                }
                else if ( OWSM_SECURITY_POLICIES.equals( fqn)){
                    Map<? extends ExecutableElement, ? extends AnnotationValue> 
                        elementValues = annotationMirror.getElementValues();
                    for (Entry<? extends ExecutableElement, 
                            ? extends AnnotationValue> entry : elementValues.entrySet())
                    {
                        handleList(result, entry);
                    }
                }
            }
        }
        return isWebService;
    }


    private void handleList( Set<String> result,
            Entry<? extends ExecutableElement, ? extends AnnotationValue> entry )
    {
        ExecutableElement method = entry.getKey();
        AnnotationValue value = entry.getValue();
        if ( VALUE.contentEquals( method.getSimpleName())){
            Object policies = value.getValue();
            if ( policies instanceof List<?> ){
                List<?> policiesList = (List<?>) policies;
                for (Object policy : policiesList) {
                    if ( policy instanceof AnnotationMirror ){
                        AnnotationMirror annotation = 
                            (AnnotationMirror) policy;
                        Element annotationElement = 
                            annotation.getAnnotationType().asElement();
                        if ( annotationElement instanceof TypeElement){
                            String fqn = ((TypeElement)annotationElement).
                                getQualifiedName().toString();
                            if ( fqn.equals( OWSM_SECURITY_POLICY )){
                                addId( result , (AnnotationMirror)policy);
                            }
                        }
                    }
                }
            }
        }
    }


    private void addId( Set<String> result, AnnotationMirror annotationMirror ) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> 
            elementValues = annotationMirror.getElementValues();
        for (Entry<? extends ExecutableElement, 
                ? extends AnnotationValue> entry : elementValues.entrySet())
        {
            ExecutableElement method = entry.getKey();
            AnnotationValue value = entry.getValue();
            if ( URI.contentEquals( method.getSimpleName() )){
                if ( value.getValue() != null ){
                    result.add( value.getValue().toString());
                }
            }
        }
    }


    private void initUI( List<String> policyIds, Set<String> selectedIds ) {
        removeAll();
        initComponents();
        DefaultComboBoxModel model = new DefaultComboBoxModel( 
                policyIds.toArray(new String[0]));
        myPoliciesList.setModel( model );
        
        myRenderer = new PoliciesListCellRenderer( selectedIds );
        myPoliciesList.setCellRenderer(myRenderer);
        myPoliciesList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        myPoliciesList.addMouseListener( new MouseListener() {
            
            @Override
            public void mouseReleased( MouseEvent e ) {
            }
            
            @Override
            public void mousePressed( MouseEvent e ) {
            }
            
            @Override
            public void mouseExited( MouseEvent e ) {
            }
            
            @Override
            public void mouseEntered( MouseEvent e ) {
            }
            
            @Override
            public void mouseClicked( MouseEvent e ) {
                int index = myPoliciesList.getSelectedIndex();
                ListUI listUi = myPoliciesList.getUI();
                Rectangle bounds = listUi.getCellBounds( myPoliciesList, 
                        index, index+1);
                Point listLocatoin = myPoliciesList.getLocationOnScreen();
                Point location = bounds.getLocation();
                Point clickPoint = e.getLocationOnScreen();
                myRenderer.click( clickPoint.getX() -listLocatoin.getX()  -location.getX() , 
                        clickPoint.getY() - listLocatoin.getY() - location.getY() , index );
                myPoliciesList.repaint(bounds );
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        myPoliciesList = new javax.swing.JList();
        myLabel = new javax.swing.JLabel();

        jScrollPane1.setViewportView(myPoliciesList);

        myLabel.setLabelFor(myPoliciesList);
        org.openide.awt.Mnemonics.setLocalizedText(myLabel, org.openide.util.NbBundle.getMessage(PoliciesVisualPanel.class, "LBL_Policies")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(myLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(myLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel myLabel;
    private javax.swing.JList myPoliciesList;
    // End of variables declaration//GEN-END:variables
    
    private PoliciesListCellRenderer myRenderer ;
    private FileObject myWsJavaFile;
    private String wsFqn;
    
    private class PoliciesListCellRenderer extends JPanel implements ListCellRenderer {
        
        PoliciesListCellRenderer( Set<String> selected ){
            JList list = new JList(new Object[]{""});
            Component comp = new DefaultListCellRenderer().
                getListCellRendererComponent( list, "", 0, true, true);
            mySelectedBckgrnd = comp.getBackground();
            setOpaque( true );
            setLayout( new FlowLayout(FlowLayout.LEFT) );
            myCheckBox = new JCheckBox();
            myCheckBox.setBackground( Color.WHITE );
            add( myCheckBox );
            myLbl = new JLabel( );
            myLbl.setBackground( Color.WHITE );
            add( myLbl );
            setBackground( Color.WHITE );
            
            mySelected = new boolean[ myPoliciesList.getModel().getSize()];
            for( int i=0; i<mySelected.length ; i++ ){
                String id = myPoliciesList.getModel().getElementAt(i).toString();
                mySelected[ i] =selected.contains( id);
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        @Override
        public Component getListCellRendererComponent( JList list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus )
        {
            myLbl.setText( value.toString() );
            if ( isSelected ){
                setBackground( mySelectedBckgrnd );
                myLbl.setBackground( mySelectedBckgrnd );
                myCheckBox.setBackground( mySelectedBckgrnd );
            }
            else {
                setBackground(Color.WHITE);
                myLbl.setBackground( Color.WHITE );
                myCheckBox.setBackground( Color.WHITE );
            }
            myCheckBox.setSelected(mySelected[index]);
            return this;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.JComponent#paint(java.awt.Graphics)
         */
        @Override
        public void paint( Graphics g ) {
            super.paint(g);
            
            if ( myLocation == null ){
                myLocation = myCheckBox.getBounds();
            }
        }
        
        List<String> getPolicyIds(){
            List<String> result = new ArrayList<String>( 
                    myPoliciesList.getModel().getSize());
            for (int i = 0; i < mySelected.length; i++) {
                if ( mySelected[i]){
                    result.add( myPoliciesList.getModel().getElementAt(i).toString());
                }
            }
            return result;
        }

        void click( double x, double y, int index) {
            if ( myLocation.contains( new Point((int)x, (int)y))){
                mySelected[index] =!mySelected[index];
            }
        }
        
        private JCheckBox myCheckBox;
        private JLabel myLbl;
        private Color mySelectedBckgrnd; 
        private Rectangle myLocation;
        private boolean[] mySelected; 
    }
}
