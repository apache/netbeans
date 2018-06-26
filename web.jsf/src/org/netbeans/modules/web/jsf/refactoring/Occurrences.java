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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.web.jsf.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.Converter;
import org.netbeans.modules.web.jsf.api.editor.JSFEditorUtilities;
import org.netbeans.modules.web.jsf.api.facesmodel.*;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * These classes represents an occurence in a faces configuration file.
 * @author Petr Pisl, Po-Ting Wu
 */
public class Occurrences {
    
    private static final Logger LOGGER = Logger.getLogger(Occurrences.class.getName());
    
    public static abstract class OccurrenceItem {
        // the faces configuration file
        protected FileObject config;
        
        protected JSFConfigComponent component;
        // needed for undo
        protected JSFConfigComponent copy;
        
        protected String oldValue;
        protected String newValue;
        protected String name;
        
        public OccurrenceItem(FileObject config, JSFConfigComponent component, String newValue, String oldValue, String name) {
            this.config = config;
            this.component = component;
            this.copy = (JSFConfigComponent) component.copy(component.getParent());
            this.newValue = newValue;
            this.oldValue = oldValue;
            this.name = name;
        }
        
        public OccurrenceItem(JSFConfigComponent component, String newValue, 
                String oldValue, String name) 
        {
            this(null, component , newValue, oldValue , name );
            this.config = component.getModel().getModelSource().getLookup().
                lookup(FileObject.class);
        }
        
        public String getNewValue(){
            return newValue;
        }
        
        public String getOldValue(){
            return oldValue;
        }
        
        public FileObject getFacesConfig() {
            return config;
        }
        
        public String getElementText(){
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("<font color=\"#0000FF\">");    //NOI18N
            stringBuffer.append("&lt;").append(getXMLElementName()).append("&gt;</font><b>"); //NOI18N
            stringBuffer.append(oldValue).append("</b><font color=\"#0000FF\">&lt;/").append(getXMLElementName()); //NOI18N
            stringBuffer.append("&gt;</font>");     //NOI18N
            return stringBuffer.toString();
        }
        
        protected abstract String getXMLElementName();
        
        public String getRenamePackageMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_Package_Rename",  //NOI18N
                    new Object[] {getElementText()});
        }
        
        // usages 
        public abstract String getWhereUsedMessage();
        
        //for changes like rename, move, change package...
        public abstract String getChangeMessage();
        public abstract void performChange();
        public abstract void undoChange();
        
        // save delete
        public abstract String getSafeDeleteMessage();
        public abstract void performSafeDelete();
        public abstract void undoSafeDelete();
        
        public PositionBounds getChangePosition() {
            try{
                DataObject dataObject = DataObject.find(config);
                if (!(dataObject instanceof JSFConfigDataObject)) {
                    return null;
                }
                
                BaseDocument document = JSFEditorUtilities.getBaseDocument(dataObject);
                int start = component.findPosition();
                String text = document.getText(start, document.getLength()-start);
                int startOffset = text.indexOf(getXMLElementName());
                startOffset = start + text.indexOf(oldValue, startOffset);
                int endOffset = startOffset + oldValue.length();
                
                CloneableEditorSupport editor
                        = JSFEditorUtilities.findCloneableEditorSupport((JSFConfigDataObject)dataObject);
                if (editor != null) {
                    PositionRef bgn, end;
                    if (startOffset == -1) {
                        // issue #187924: start offset is -1 when the occurence doens't exist any more
                        bgn = editor.createPositionRef(0, Bias.Forward);
                        end = editor.createPositionRef(0, Bias.Backward);
                    } else {
                        bgn = editor.createPositionRef(startOffset, Bias.Forward);
                        end = editor.createPositionRef(endOffset, Bias.Backward);
                    }
                    return new PositionBounds(bgn, end);
                }
            } catch (BadLocationException exception) {
                LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            } catch (DataObjectNotFoundException exception) {
                LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            }
            
            return null;
        }
    }
    
    /**
     * Implementation for ManagedBean
     */
    public static class ManagedBeanClassItem extends OccurrenceItem{
        public ManagedBeanClassItem(FileObject config, ManagedBean bean, String newValue) {
            super(config, bean, newValue, bean.getManagedBeanClass(), bean.getManagedBeanName());
        }
        
        public ManagedBeanClassItem(ManagedBean bean, String newValue) {
            super(bean, newValue, bean.getManagedBeanClass(), bean.getManagedBeanName());
        }
        
        protected String getXMLElementName(){
            return "managed-bean-class"; //NOI18N
        }
        
        public String getWhereUsedMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ManagedBeanClass_WhereUsed", //NOI18N
                    new Object[] { name, getElementText()});
        }
        
        public String getChangeMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ManagedBeanClass_Rename",  //NOI18N
                    new Object[] { name, getElementText()});
        }
        
        public void performChange(){
            changeBeanClass(oldValue, newValue);
        }
        
        public void undoChange(){
            changeBeanClass(newValue, oldValue);
        }

        private void changeBeanClass(String oldClass, String newClass){
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            for (ManagedBean managedBean : facesConfig.getManagedBeans()) {
                if (oldClass.equals(managedBean.getManagedBeanClass())){
                    facesConfig.getModel().startTransaction();
                    managedBean.setManagedBeanClass(newClass);
                    endModelTransaction(facesConfig.getModel());
                    break;
                }
            }
        }
        
        public String getSafeDeleteMessage() {
            return NbBundle.getMessage(Occurrences.class, "MSG_ManagedBeanClass_SafeDelete",  //NOI18N
                    new Object[] { name, getElementText()});
        }
        
        public void performSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            for (ManagedBean managedBean : facesConfig.getManagedBeans()) {
                if (oldValue.equals(managedBean.getManagedBeanClass())){
                    facesConfig.getModel().startTransaction();
                    facesConfig.removeManagedBean(managedBean);
                    endModelTransaction(facesConfig.getModel());
                    break;
                }
            }
        }
        
        public void undoSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            facesConfig.getModel().startTransaction();
            facesConfig.addManagedBean((ManagedBean) copy);
            endModelTransaction(facesConfig.getModel());
        }
    }
    
    public static class ConverterClassItem extends OccurrenceItem {
        public ConverterClassItem(FileObject config, Converter converter, String newValue){
            super(config, converter, newValue, converter.getConverterClass(), converter.getConverterId());
        }
        
        public ConverterClassItem(Converter converter, String newValue){
            super(converter, newValue, converter.getConverterClass(), converter.getConverterId());
        }
        
        protected String getXMLElementName(){
            return "converter-class"; //NOI18N
        }
        
        public String getWhereUsedMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterClass_WhereUsed", //NOI18N
                    new Object[] { name, getElementText()});
        }
        
        public String getChangeMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterClass_Rename", //NOI18N
                    new Object[] { name, getElementText()});
        }
        
        public void performChange(){
            changeConverterClass(oldValue, newValue);
        }
        
        public void undoChange(){
            changeConverterClass(newValue, oldValue);
        }
        
        private void changeConverterClass(String oldClass, String newClass){
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            for (Converter converter : facesConfig.getConverters()) {
                if (oldClass.equals(converter.getConverterClass())){
                    converter.getModel().startTransaction();
                    converter.setConverterClass(newClass);
                    endModelTransaction(converter.getModel());
                    break;
                }
            }
        }
        
        public String getSafeDeleteMessage() {
            return NbBundle.getMessage(Occurrences.class, "MSG_Converter_SafeDelete", //NOI18N
                    new Object[] { name, getElementText()});
        }
        
        public void performSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            for (Converter converter : facesConfig.getConverters()) {
                if (oldValue.equals(converter.getConverterClass())){
                    facesConfig.getModel().startTransaction();
                    facesConfig.removeConverter(converter);
                    endModelTransaction(facesConfig.getModel());
                    break;
                }
            }
        }
        
        public void undoSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            facesConfig.getModel().startTransaction();
            facesConfig.addConverter((Converter) copy);
            endModelTransaction(facesConfig.getModel());
        }
    }
    
    public static class ConverterForClassItem extends OccurrenceItem {
        public ConverterForClassItem(FileObject config, Converter converter, String newValue){
            super(config, converter, newValue, converter.getConverterForClass(), converter.getConverterId());
        }
        
        public ConverterForClassItem(Converter converter, String newValue){
            super(converter, newValue, converter.getConverterForClass(), converter.getConverterId());
        }
        
        protected String getXMLElementName(){
            return "converter-for-class"; //NOI18N
        }
        
        public String getWhereUsedMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterForClass_WhereUsed", //NOI18N
                    new Object[] { name, getElementText()});
        }
        
        public String getChangeMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterForClass_Rename", //NOI18N
                    new Object[] { name, getElementText()});
        }
        
        public void performChange(){
            changeConverterForClass(oldValue, newValue);
        }
        
        public void undoChange(){
            changeConverterForClass(newValue, oldValue);
        }
        
        private void changeConverterForClass(String oldClass, String newClass){
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            for (Converter converter : facesConfig.getConverters()) {
                if (oldClass.equals(converter.getConverterForClass())){
                    converter.getModel().startTransaction();
                    converter.setConverterForClass(newClass);
                    endModelTransaction(converter.getModel());
                    break;
                }
            }
        }
        
        public String getSafeDeleteMessage() {
            return NbBundle.getMessage(Occurrences.class, "MSG_Converter_SafeDelete", //NOI18N
                    new Object[] { name, getElementText()});
        }
        
        public void performSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            for (Converter converter : facesConfig.getConverters()) {
                if (oldValue.equals(converter.getConverterForClass())){
                    facesConfig.getModel().startTransaction();
                    facesConfig.removeConverter(converter);
                    endModelTransaction(facesConfig.getModel());
                    break;
                }
            }
        }
        
        public void undoSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            facesConfig.getModel().startTransaction();
            facesConfig.addConverter((Converter) copy);
            endModelTransaction(facesConfig.getModel());
        }
    }
    
    public static List<OccurrenceItem> getAllOccurrences(Project project,
            final String oldName, final String newName)
    {
        final List<OccurrenceItem> result = new ArrayList<OccurrenceItem>();
        assert project != null;
        assert oldName != null;
        
        LOGGER.fine("getAllOccurences("+ project.getProjectDirectory() + ", " + oldName + ", " + newName + ")"); //NOI18N
        if (project != null){
            // find all jsf configuration files in the web module
            /*
             * Old access code to JSF model.  
             * 
             * FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
            
            if (configs != null){
                for (FileObject config : configs) {
                    FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
                    for (Converter converter : facesConfig.getConverters()) {
                        if (oldName.equals(converter.getConverterClass()))
                            result.add(new ConverterClassItem(config, converter, newName));
                        else if (oldName.equals(converter.getConverterForClass()))
                            result.add(new ConverterForClassItem(config, converter, newName));
                    }
                    for (ManagedBean managedBean : facesConfig.getManagedBeans()) {
                        if (oldName.equals(managedBean.getManagedBeanClass()))
                            result.add(new ManagedBeanClassItem(config, managedBean, newName));
                    }
                }
            }*/
            /**
             * @author ads
             * Migrated code .
             */
            MetadataModel<JsfModel> model = JSFUtils.getModel(project);
            if ( model == null ){
                return result;
            }
            try {
                model.runReadAction(new MetadataModelAction<JsfModel, Void>() {

                    public Void run( JsfModel metadata ) throws Exception {
                        /*
                         *  Collect ONLY XML occurrences . Annotated elements
                         *  are Java class elements. So they will be refactored
                         *  as part of Java functionality.
                         */
                        List<Converter> converters = metadata.getElements(Converter.class);
                        for ( Converter converter : converters) {
                            if (oldName.equals(converter.getConverterClass())){
                                result.add(new ConverterClassItem(converter, newName));
                            }
                            else if (oldName.equals(converter.getConverterForClass())){
                                result.add(new ConverterForClassItem(converter, newName));
                            }
                        }
                        List<ManagedBean> beans = metadata.getElements( ManagedBean.class);
                        for (ManagedBean bean : beans) {
                            if (oldName.equals(bean.getManagedBeanClass())){
                                result.add(new ManagedBeanClassItem(bean, newName));
                            }
                        }
                        return null;
                    }
                });
            }
            catch( MetadataModelException e ){
                LOGGER.log( Level.WARNING , e.getMessage() , e );
            }
            catch( IOException e ){
                LOGGER.log( Level.WARNING , e.getMessage() , e );
            }
        }
        return result;
    }

    private static void endModelTransaction(JSFConfigModel model) {
        try {
            model.endTransaction();
        } catch (IllegalStateException ise) {
            Exceptions.printStackTrace(ise);
        }
    }
    
    public static List <OccurrenceItem> getPackageOccurrences(Project project,
            final String oldPackageName,
            final String newPackageName, final boolean renameSubpackages)
    {
        final List<OccurrenceItem> result = new ArrayList<OccurrenceItem>();
        assert project != null;
        assert oldPackageName != null;
        
        if (project != null){
            // find all jsf configuration files in the web module
            /*
             * Old access code to JSF model. 
             * 
             * FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
            
            if (configs != null){
                for (FileObject config : configs) {
                    FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
                    for (Converter converter : facesConfig.getConverters()) {
                        if (JSFRefactoringUtils.containsRenamingPackage(converter.getConverterClass(), oldPackageName, renameSubpackages))
                            result.add(new ConverterClassItem(config, converter, 
                                    getNewFQCN(newPackageName, oldPackageName, converter.getConverterClass())));
                        if (JSFRefactoringUtils.containsRenamingPackage(converter.getConverterForClass(), oldPackageName, renameSubpackages))
                            result.add(new ConverterForClassItem(config, converter,
                                    getNewFQCN(newPackageName, oldPackageName, converter.getConverterForClass())));
                    }
                    for (ManagedBean managedBean : facesConfig.getManagedBeans()) {
                        if (JSFRefactoringUtils.containsRenamingPackage(managedBean.getManagedBeanClass(), oldPackageName, renameSubpackages))
                            result.add(new ManagedBeanClassItem(config, managedBean,
                                    getNewFQCN(newPackageName, oldPackageName, managedBean.getManagedBeanClass())));
                    }
                }
            }*/
            /**
             * @author ads
             */
            MetadataModel<JsfModel> model = JSFUtils.getModel(project);
            if ( model == null ){
                return result;
            }
            try {
                model.runReadAction(new MetadataModelAction<JsfModel, Void>() {

                    public Void run( JsfModel metadata ) throws Exception {
                        /*
                         *  Collect ONLY XML occurrences . Annotated elements
                         *  are Java class elements. So they will be refactored
                         *  as part of Java functionality.
                         */
                        List<Converter> converters = metadata.getElements(Converter.class);
                        for ( Converter converter : converters) {
                            if (JSFRefactoringUtils.containsRenamingPackage(
                                    converter.getConverterClass(), oldPackageName, 
                                    renameSubpackages))
                            {
                                result.add(new ConverterClassItem(converter, 
                                        getNewFQCN(newPackageName, oldPackageName, 
                                                converter.getConverterClass())));
                            }
                            if (JSFRefactoringUtils.containsRenamingPackage(
                                    converter.getConverterForClass(), oldPackageName, 
                                    renameSubpackages))
                            {
                                result.add(new ConverterForClassItem(converter,
                                        getNewFQCN(newPackageName, oldPackageName, 
                                                converter.getConverterForClass())));
                            }
                        }
                        List<ManagedBean> beans = metadata.getElements( ManagedBean.class);
                        for (ManagedBean bean : beans) {
                            if (JSFRefactoringUtils.containsRenamingPackage(
                                    bean.getManagedBeanClass(), oldPackageName, 
                                    renameSubpackages))
                                result.add(new ManagedBeanClassItem(bean,
                                        getNewFQCN(newPackageName, oldPackageName, 
                                                bean.getManagedBeanClass())));
                        }
                        return null;
                    }
                });
            }
            catch( MetadataModelException e ){
                LOGGER.log( Level.WARNING , e.getMessage() , e );
            }
            catch( IOException e ){
                LOGGER.log( Level.WARNING , e.getMessage() , e );
            }
        }
        return result;
    }
    
    /**
     * A helper method, which is used for obtaining new FQCN, when a package is renamed. 
     * @param newPackageName the new package name. It must to be always full qualified package name.
     * @param oldPackageName the old package name. It must to be always full qualified package name.
     * @param oldFQCN the full qualified class name
     * @param folderRename Indicates whether the changing package is based on the 
     * renaming package or renaming folder.
     * @returns new FQCN for the class. 
     */
    public static String getNewFQCN(String newPackageName, String oldPackageName, String oldFQCN){
        String value = oldFQCN;
        
        if (oldPackageName.length() == 0){
            value = newPackageName + '.' + oldFQCN;
        }
        else {
            if (oldFQCN.startsWith(oldPackageName)){
                value = value.substring(oldPackageName.length());
                if (newPackageName.length() > 0){
                    value = newPackageName + value;
                }
                if (value.charAt(0) == '.'){
                    value = value.substring(1);
                }
            }
         }
         return value;
    }
}
