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
package org.netbeans.modules.websvc.saas.codegen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter;
import org.netbeans.modules.websvc.saas.codegen.Constants.DropFileType;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamFilter;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider;
import org.netbeans.modules.websvc.saas.codegen.util.UniqueVariableNameFinder;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public abstract class SaasClientCodeGenerator implements SaasClientCodeGenerationProvider {
    
    public static final String REST_CONNECTION = "RestConnection"; //NOI18N
    public static final String REST_RESPONSE = "RestResponse"; //NOI18N
    public static final String REST_CONNECTION_PACKAGE = "org.netbeans.saas";
    public static final String SAAS_SERVICES = "SaaSServices"; //NOI18N
    public static final String TEMPLATES_SAAS = "Templates/"+SAAS_SERVICES+"/"; //NOI18N
    public static final String REST_CONNECTION_TEMPLATE = TEMPLATES_SAAS+REST_CONNECTION+"."+Constants.JAVA_EXT; //NOI18N
    public static final String REST_RESPONSE_TEMPLATE = TEMPLATES_SAAS+REST_RESPONSE+"."+Constants.JAVA_EXT; //NOI18N
    public static final String COMMENT_END_OF_HTTP_MEHTOD_GET = "TODO return proper representation object";      //NOI18N
    public static final String GENERIC_REF_CONVERTER_TEMPLATE = TEMPLATES_SAAS+"RefConverter.java"; //NOI18N
    public static final String GENERIC_REF_CONVERTER = "GenericRefConverter"; //NOI18N
    public static final String CONVERTER_SUFFIX = "Converter";      //NOI18N
    public static final String CONVERTER_FOLDER = "converter";      //NOI18N
    public static final String RESOURCE_SUFFIX = "Resource";      //NOI18N
    public static final String VAR_NAMES_RESULT_DECL = REST_RESPONSE + " " + Util.VAR_NAMES_RESULT;
    public static final String INDENT = "        ";
    public static final String INDENT_2 = "             ";
    
    private FileObject targetFile; // resource file target of the drop
    private FileObject destDir;
    private Project project;
    private SaasBean bean;
    private Document targetDocument;
    private int start = 0;
    private int end = 0;
    private ProgressHandle pHandle;
    private int totalWorkUnits;
    private int workUnits;
    private DropFileType dropFileType;
    private int precedence;
    private UniqueVariableNameFinder nFinder = new UniqueVariableNameFinder();
    
    public SaasClientCodeGenerator() {
    }

    protected Document getTargetDocument() {
        return this.targetDocument;
    }

    protected FileObject getTargetFile() {
        return this.targetFile;
    }

    protected FileObject getTargetFolder() {
        return this.destDir;
    }
   
    protected Project getProject() {
        return this.project;
    }
    
    public int getStartPosition() {
        return start;
    }
    
    public void setStartPosition(int start) {
        this.start = start;
    }
    
    public int getEndPosition() {
        return end;
    }
    
    public void setEndPosition(int end) {
        this.end = end;
    }

    public DropFileType getDropFileType() {
        return dropFileType;
    }

    public void setDropFileType(DropFileType dropFileType) {
        this.dropFileType = dropFileType;
    }
    
    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }
    
    public int getPrecedence() {
        return precedence;
    }
    
    public void initProgressReporting(ProgressHandle pHandle) {
        initProgressReporting(pHandle, true);
    }
    
    public void initProgressReporting(ProgressHandle pHandle, boolean start) {
        this.pHandle = pHandle;
        this.totalWorkUnits = getTotalWorkUnits();
        this.workUnits = 0;
        
        if (pHandle != null && start) {
            if (totalWorkUnits > 0) {
                pHandle.start(totalWorkUnits);
            } else {
                pHandle.start();
            }
        }
    }
    
    public void reportProgress(String message) {     
        if (pHandle != null) {
            if (totalWorkUnits > 0) {
                pHandle.progress(message, ++workUnits);
            } else {
                pHandle.progress(message);
            }
        }
    }
    
    public void finishProgressReporting() {
        if (pHandle != null) {
            pHandle.finish();
        }
    }
    
    public int getTotalWorkUnits() {
        return 0;
    }
    
    protected ProgressHandle getProgressHandle() {
        return pHandle;
    }
    
    public abstract boolean canAccept(SaasMethod method, Document doc);

    public Set<FileObject> generate() throws IOException {
        preGenerate();
        FileObject[] result = new FileObject[]{getTargetFile()};
        //JavaSourceHelper.saveSource(result);

        finishProgressReporting();

        return new HashSet<FileObject>(Arrays.asList(result));
    }
    
    public void init(SaasMethod method, Document doc) throws IOException {
        if(doc == null)
            throw new IOException("Cannot generate, target document is null.");
        this.targetDocument = doc;
        this.targetFile = NbEditorUtilities.getFileObject(targetDocument);
        
        this.destDir = targetFile.getParent();
        project = FileOwnerQuery.getOwner(targetFile);

        if (project == null) {
            throw new IllegalArgumentException(targetFile.getPath() + " is not part of a project.");
        }
    }
    
    protected void preGenerate() throws IOException {
    }
    
    /*
     * Copy File only
     */    
    public void copyFile(String resourceName, File destFile) throws IOException {
        String path = resourceName;
        if(!destFile.exists()) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = this.getClass().getResourceAsStream(path);
                os = new FileOutputStream(destFile);
                int c;
                while ((c = is.read()) != -1) {
                    os.write(c);
                }
            } finally {
                if(os != null) {
                    os.flush();
                    os.close();
                }
                if(is != null)
                    is.close();            
            }
        }
    }

    protected abstract String getCustomMethodBody() throws IOException;
    
    public SaasBean getBean() {
        return bean;
    }
    
    public void setBean(SaasBean bean) {
        this.bean = bean;
    }
    
    private String getParamList() {
        List<ParameterInfo> inputParams = bean.filterParametersByAuth
                (bean.filterParameters(new ParamFilter[]{ParamFilter.FIXED}));
        String text = ""; //NOI18N
        for (int i = 0; i < inputParams.size(); i++) {
            ParameterInfo param = inputParams.get(i);

            if (i == 0) {
                text += getParameterName(param, true, true, true);
            } else {
                text += ", " + getParameterName(param, true, true, true); //NOI18N
            }
        }

        return text;
    }

  
    protected void insert(String s, boolean reformat)
            throws BadLocationException {
        Document doc = getTargetDocument();
        if (doc == null)
            return;
        
        if (s == null)
            return;
        
        insert(s, getStartPosition(), getEndPosition(), doc, reformat);
    }
    
    protected int insert(String s, int start, int end, Document doc, boolean reformat)
            throws BadLocationException {
        try {
            doc.remove(start, end - start);
            doc.insertString(start, s, null);
        } catch (BadLocationException ble) {}
        
        if(reformat)
            reformat(doc, 0, doc.getLength());
        
        return start;
    }
    
    protected boolean isInBlock(Document doc) {
        //TODO - FIX return true if the caret position where code is
        //going to be inserted is within some block other Class block.
        return true;
    }
    
    protected void reformat(Document doc, final int start, final int end) 
            throws BadLocationException {
        final Reformat reformat = Reformat.get(doc);
        final BadLocationException[] ble = new BadLocationException[1];
        reformat.lock();
        try {
            NbDocument.runAtomic((StyledDocument)doc, new Runnable() {
                public void run() {
                    try {
                        reformat.reformat(start, end);
                    } catch (BadLocationException e) {
                        ble[0] = e;
                    }
                }
            });
        } finally {
            reformat.unlock();
        }
        if (ble[0] != null)
            throw ble[0];
    }
  
    protected String[] getGetParamNames(List<ParameterInfo> queryParams) {
        ArrayList<String> params = new ArrayList<String>();
        params.addAll(Arrays.asList(getParamNames(queryParams)));
        return params.toArray(new String[0]);
    }
    
    protected String[] getGetParamTypes(List<ParameterInfo> queryParams) {
        ArrayList<String> types = new ArrayList<String>();
        types.addAll(Arrays.asList(getParamTypeNames(queryParams)));
        return types.toArray(new String[0]);
    }
    
    
    protected String[] getParamNames(List<ParameterInfo> params) {
        List<String> results = new ArrayList<String>();
        
        for (ParameterInfo param : params) {
            results.add(getParameterName(param, true, true, true));
        }
        
        return results.toArray(new String[0]);
    }
    
    protected String[] getParamTypeNames(List<ParameterInfo> params) {
        List<String> results = new ArrayList<String>();
        
        for (ParameterInfo param : params) {
            results.add(param.getTypeName());
        }
        
        return results.toArray(new String[0]);
    }
    
    protected String getParameterName(ParameterInfo param) {
        return Util.getParameterName(param);
    }
    
    protected String getParameterName(ParameterInfo param, 
            boolean camelize, boolean normalize) {
        return Util.getParameterName(param, camelize, normalize, false);
    }
    
    protected String getParameterName(ParameterInfo param, 
            boolean camelize, boolean normalize, boolean trimBraces) {
        return Util.getParameterName(param, camelize, normalize, trimBraces);
    }
    
    protected String getVariableName(String name) {
        return Util.getVariableName(name, true, true, true);
    }
    
    protected String getVariableName(final String name, 
            boolean camelize, boolean normalize, boolean trimBraces) {
        return Util.getVariableName(name, camelize, normalize, trimBraces);
    }
    
    protected Object[] getParamValues(List<ParameterInfo> params) {
        List<Object> results = new ArrayList<Object>();
        
        for (ParameterInfo param : params) {
            Object defaultValue = null;
            
            if (param.getStyle() != ParamStyle.QUERY) {
                defaultValue = param.getDefaultValue();
            }
            
            results.add(defaultValue);
        }
        
        return results.toArray(new Object[0]);
    }
    
    public void setDropLocation(JTextComponent targetComponent) {
        Caret caret = targetComponent.getCaret();
        setStartPosition(Math.min(caret.getDot(), caret.getMark()));
        setEndPosition(Math.max(caret.getDot(), caret.getMark()));
    }
    
    protected String findNewName(String pattern, String oldName) {
        updateVariableDecl(pattern);
        return nFinder.findNewName(pattern, oldName);
    }
    
    protected String getResultPattern() {
        return Util.VAR_NAMES_RESULT+
                nFinder.getVariableCount(VAR_NAMES_RESULT_DECL);
    }
    
    protected void addVariablePattern(String pattern, int count) {
        nFinder.addPattern(pattern, new Integer(count));
    }
    
    protected void updateVariableNames(List<ParameterInfo> params) {
        nFinder.addPattern(VAR_NAMES_RESULT_DECL, new Integer(0));
        try {
            String text = getTargetDocument().getText(0, getTargetDocument().getLength());
            nFinder.updateVariableDecl(text, params);
            nFinder.updateVariableDecl(text, VAR_NAMES_RESULT_DECL);
        } catch (BadLocationException ex) {}
    }
    
    protected void updateVariableNamesForWS(List<? extends WSParameter> params) {
        nFinder.addPattern(VAR_NAMES_RESULT_DECL, new Integer(0));
        try {
            String text = getTargetDocument().getText(0, getTargetDocument().getLength());
            nFinder.updateVariableDeclForWS(text, params);
            nFinder.updateVariableDecl(text, VAR_NAMES_RESULT_DECL);
        } catch (BadLocationException ex) {}
    }
    
    private void updateVariableDecl(String pattern) {
        try {
            nFinder.updateVariableDecl(getTargetDocument().getText(0, getTargetDocument().getLength()), pattern);
        } catch (BadLocationException ex) {}
    }
    
    protected List<ParameterInfo> renameParameterNames(List<ParameterInfo> params) {
        return nFinder.renameParameterNames(params);
    }
    
    public String getVariableDecl(ParameterInfo p) {
        return nFinder.getVariableDecl(p);
    }

    public String getVariableDecl(WSParameter p) {
        return nFinder.getVariableDecl(p);
    }
    
    protected void clearVariablePatterns() {
        nFinder.clearPatterns();
    }
}
