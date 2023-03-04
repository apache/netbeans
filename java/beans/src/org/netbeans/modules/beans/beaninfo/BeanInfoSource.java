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

package org.netbeans.modules.beans.beaninfo;

import java.beans.PropertyVetoException;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.modules.beans.GenerateBeanException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 * Finds or creates BeanInfo source elemnet for the class.
 * It can regenerate the source if there are the guarded blocks.
 * @author  Petr Hrebejk
 */

public final class BeanInfoSource extends Object {

    private static final String BEANINFO_NAME_EXT = "BeanInfo"; // NOI18N
    
    private static final String DESCRIPTOR_SECTION = "BeanDescriptor"; // NOI18N
    private static final String PROPERTIES_SECTION = "Properties"; // NOI18N
    private static final String EVENTSETS_SECTION = "Events"; // NOI18N
    private static final String ICONS_SECTION = "Icons"; // NOI18N
    private static final String IDX_SECTION = "Idx"; // NOI18N
    private static final String METHODS_SECTION = "Methods"; // NOI18N
    private static final String SUPERCLASS_SECTION = "Superclass";  // NOI18N
    
    private DataObject javaDataObject;
    private DataObject   biDataObject = null;
    private BIEditorSupport   javaEditor =  null;

    /** Creates new BeanInfoSource */
    public BeanInfoSource (FileObject javafile) throws GenerateBeanException {

        findBeanInfo(javafile);
    }

    /** Returns wether the bean info exists or not */
    boolean exists() {
        return biDataObject != null;
    }

    /** Checks wether the bean info object has Guarded sections i.e.
     * was created from netbeans template.
     */
    boolean isNbBeanInfo() {

        GuardedSectionManager guards = null;
        if ( !exists() || javaEditor == null || null == (guards = javaEditor.getGuardedSectionManager())) {
            return false;
        }

        //JavaEditor.InteriorSection dis = javaEditor.findInteriorSection( DESCRIPTOR_SECTION );
        InteriorSection pis = guards.findInteriorSection( PROPERTIES_SECTION );
        InteriorSection eis = guards.findInteriorSection( EVENTSETS_SECTION );
//        JavaEditor.InteriorSection mis = javaEditor.findInteriorSection( METHODS_SECTION );
        //JavaEditor.SimpleSection iss = javaEditor.findSimpleSection( ICONS_SECTION );
        SimpleSection dss = guards.findSimpleSection( IDX_SECTION );

        //return ( pis != null && eis != null && iss != null && dss != null);
        return ( pis != null && eis != null && dss != null);
    }

    boolean hasIconInfo(){
        GuardedSectionManager guards = javaEditor.getGuardedSectionManager();
        if (guards == null) {
            return false;
        }
        SimpleSection iss = guards.findSimpleSection( ICONS_SECTION );
        return ( iss != null );
    }
    /** Checks wether the bean descriptor object has Guarded sections i.e.
     * was created from new netbeans template.
     */
    boolean isNbBeanInfoDescriptor() {

        GuardedSectionManager guards = null;
        if ( !exists() || javaEditor == null || null == (guards = javaEditor.getGuardedSectionManager())) {
            return false;
        }
        InteriorSection dis = guards.findInteriorSection( DESCRIPTOR_SECTION );
        return ( dis != null );
    }

    /** Checks wether the bean info object has Guarded sections for superclass i.e.
     * was created from new netbeans template.
     */
    boolean isNbSuperclass() {

        GuardedSectionManager guards = null;
        if ( !exists() || javaEditor == null || null == (guards = javaEditor.getGuardedSectionManager())) {
            return false;
        }
        InteriorSection dis = guards.findInteriorSection( SUPERCLASS_SECTION );
        return ( dis != null );
    }

    /** Finds the bean info for classElement asspciated with this
        object */
    void findBeanInfo(FileObject javafile) throws GenerateBeanException {

        javaEditor = null;
        try {
            this.javaDataObject = DataObject.find(javafile);
            FileObject parent = javafile.getParent();
            FileObject bifile = parent.getFileObject(javafile.getName() + BEANINFO_NAME_EXT, "java"); // NOI18N
            if (bifile != null) {
                biDataObject = DataObject.find(bifile);
                javaEditor = biDataObject.getLookup().lookup(BIEditorSupport.class);
            }
        } catch (DataObjectNotFoundException ex) {
            throw new GenerateBeanException();
            // Do nothing if no data object is found
        }
    }

    /** Deletes the BeanInfo */
    void delete() throws java.io.IOException {
        biDataObject.delete();
    }


    /** Creates beanInfo data object */
    void createFromTemplate( boolean iconBlock ) {        
        FileObject foTemplates = FileUtil.getConfigFile("Templates"); //NOI18N ;
        if ( foTemplates == null ) {
            return;
        }

        FileObject foClassTemplates = foTemplates.getFileObject( "Beans" ); // NOI18N
        if ( foClassTemplates == null ) {
            return;
        }    
            
        FileObject foBiTemplate = null;
        
        if( iconBlock ){
            foBiTemplate = foClassTemplates.getFileObject( "BeanInfo", "java" ); // NOI18N
        }
        else {
            foBiTemplate = foClassTemplates.getFileObject( "NoIconBeanInfo", "java" ); // NOI18N
        }

        if ( foBiTemplate == null ) {
            return;
        }

        try {
            DataObject doBiTemplate = DataObject.find ( foBiTemplate );
            DataFolder folder = this.javaDataObject.getFolder();
            biDataObject = doBiTemplate.createFromTemplate( folder, this.javaDataObject.getName() + BEANINFO_NAME_EXT );
            if (!(biDataObject instanceof BIDataObject)) {
                try {
                    biDataObject.setValid(false);
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
                biDataObject = DataObject.find(biDataObject.getPrimaryFile());
            }
            javaEditor = biDataObject.getLookup().lookup(BIEditorSupport.class);
        }
        catch ( org.openide.loaders.DataObjectNotFoundException e ) {
            //System.out.println ( e );
            // Do nothing if no data object is found
        }
        catch ( java.io.IOException e ) {
            //System.out.println ( e );
            // Do nothing if no data object is found
        }
    }

    /** If the bean info is available returns the bean info data object */
    DataObject getDataObject() {
        return biDataObject;
    }
    
    DataObject getSourceDataObject() {
        return javaDataObject;
    }

    /** opens the source */
    void open() {
        javaEditor.open();
    }

    /** Sets the header and bottom of properties section */
    void setDescriptorSection( String header, String bottom ) {
        setInteriorSection(DESCRIPTOR_SECTION, header, bottom);
    }

    /** Gets the header of properties setion */
    String getDescriptorSection() {
        GuardedSectionManager guards = javaEditor.getGuardedSectionManager();
        InteriorSection is = guards.findInteriorSection( DESCRIPTOR_SECTION );

        if ( is != null ) {
            return is.getText();
        }
        else
            return null;

    }

    /** Sets the header and bottom of properties section */
    void setPropertiesSection( String header, String bottom ) {
        setInteriorSection(PROPERTIES_SECTION, header, bottom);
    }

    /** Gets the header of properties setion */
    String getPropertiesSection() {
        return getInteriorSection(PROPERTIES_SECTION);
    }

    /** Sets the header and bottom of methods section */
    void setMethodsSection( String header, String bottom ) {
        setInteriorSection(METHODS_SECTION, header, bottom);
    }

    /** Gets the header of properties setion */
    String getMethodsSection() {
        return getInteriorSection(METHODS_SECTION);
    }

    /** Sets the header and bottom of event sets section */
    void setEventSetsSection( String header, String bottom ) {
        setInteriorSection(EVENTSETS_SECTION, header, bottom);
    }

    /** Gets the header of properties setion */
    String getEventSetsSection() {
        return getInteriorSection(EVENTSETS_SECTION);
    }

    /** Gets the header of properties setion */
    String getIconsSection() {
        return getSimpleSection(ICONS_SECTION);
    }

    /** Sets the header of properties setion */
    void setIconsSection( String text ) {
        setSimpleSection(ICONS_SECTION, text);
    }

    /** Gets the header of properties setion */
    String getDefaultIdxSection() {
        return getSimpleSection(IDX_SECTION);
    }

    /** Sets the header of properties setion */
    void setDefaultIdxSection( String text ) {
        setSimpleSection(IDX_SECTION, text);
    }

    /** Sets the header and bottom of properties section */
    void setSuperclassSection( String header, String bottom ) {
        setInteriorSection(SUPERCLASS_SECTION, header, bottom);
    }

    /** Gets the header of properties setion */
    String getSuperclassSection() {
        return getInteriorSection(SUPERCLASS_SECTION);
    }
    
    private void setInteriorSection(String section, String header, String bottom) {
        GuardedSectionManager guards = javaEditor.getGuardedSectionManager();
        if (guards == null) {
            return;
        }
        InteriorSection is = guards.findInteriorSection(section);

        if ( is != null ) {
            is.setHeader( header );
            is.setFooter( bottom );
        }
    }
    
    private String getInteriorSection(String section) {
        GuardedSectionManager guards = javaEditor.getGuardedSectionManager();
        if (guards == null) {
            return null;
        }
        InteriorSection is = guards.findInteriorSection(section);
        return is == null? null: is.getText();
    }
    
    private void setSimpleSection(String section, String text) {
        GuardedSectionManager guards = javaEditor.getGuardedSectionManager();
        if (guards == null) {
            return;
        }
        SimpleSection ss = guards.findSimpleSection(section);

        if (ss != null) {
            ss.setText(text);
        }
    }
    
    private String getSimpleSection(String section) {
        GuardedSectionManager guards = javaEditor.getGuardedSectionManager();
        if (guards == null) {
            return null;
        }
        SimpleSection ss = guards.findSimpleSection(section);
        return ss == null? null: ss.getText();
    }

    /*
    void regenerateMethods() {
      JavaEditor.InteriorSection is = javaEditor.findInteriorSection( "Events" );
      
      if ( is != null ) {
        is.setHeader( BeanInfoGenerator.generateMethods( classElement.getName().getName(), methods ) );
        is.setBottom( BeanInfoGenerator.generateMethodsBottom( methods ) );
      }
}
    */

}
