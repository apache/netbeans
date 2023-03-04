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

package org.netbeans.modules.web.jspparser_ext;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.apache.jasper.JspC;
import org.apache.jasper.Options;
import org.apache.jasper.compiler.JspConfig;
import org.apache.jasper.compiler.TagPluginManager;
import org.apache.jasper.runtime.TldScanner;
import org.apache.jasper.xmlparser.ParserUtils;

/**
 *
 * @author Petr Jiricka
 */
public class OptionsImpl implements Options {

    private static final Logger LOGGER = Logger.getLogger(OptionsImpl.class.getName());
    TldScanner scanner = null;

    /**
     * Jsp config information
     */
    private final JspConfig jspConfig;

    /**
     * TagPluginManager
     */
    private final TagPluginManager tagPluginManager;

    /** Creates a new instance of OptionsImpl */
    public OptionsImpl(ServletContext context) {
        ParserUtils.setSchemaResourcePrefix("/resources/schemas/");
        ParserUtils.setDtdResourcePrefix("/resources/dtds/");
        scanner = new TldScanner(context, false);
        // #188703 - JSTL 1.1 handling
        clearStaticHashSet(TldScanner.class, "systemUris");
        clearStaticHashSet(TldScanner.class, "systemUrisJsf");
        jspConfig = new JspConfig(context);
        tagPluginManager = new TagPluginManager(context);
    }
    
    public int getCheckInterval() {
        return 300;
    }
    
    public boolean getClassDebugInfo() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public String getClassPath() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public String getCompiler() {
        // should not be needed
        //throw new UnsupportedOperationException();
        return null;
    }
    
    public boolean getDevelopment() {
        return true;
    }
    
    public boolean getFork() {
        // should not be needed
        return false;
    }
    
    public String getIeClassId() {
        return JspC.DEFAULT_IE_CLASS_ID;
    }
    
    public String getJavaEncoding() {
        // should not be needed
        throw new UnsupportedOperationException();
        //return "UTF-8";
    }
    
    public JspConfig getJspConfig() {
	return jspConfig;
    }
    
    public boolean getKeepGenerated() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public boolean getTrimSpaces() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public boolean genStringAsCharArray() {
        // should not be needed
        throw new UnsupportedOperationException();
    }

    public boolean getMappedFile() {
        return false;
    }
    
    public boolean getReloading() {
        return true;
    }
    
    public java.io.File getScratchDir() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public boolean getSendErrorToClient() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public TagPluginManager getTagPluginManager() {
        return tagPluginManager;
    }
    
    public boolean isPoolingEnabled() {
        // should not be needed
        throw new UnsupportedOperationException();
        //return true;
    }
    
    public boolean isSmapDumped() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public boolean isSmapSuppressed() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public boolean isXpoweredBy() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public boolean getErrorOnUseBeanInvalidClassAttribute() {
        // should not be needed
        throw new UnsupportedOperationException();
    }

    public int getModificationTestInterval(){
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public String getCompilerSourceVM(){
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public String getCompilerTargetVM(){
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public String getCompilerClassName() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public int getInitialCapacity(){
        throw new UnsupportedOperationException();
    }
    
    public boolean getUsePrecompiled(){
        return false;
    }
    
    public String getSystemClassPath(){
        throw new UnsupportedOperationException();
    }

    public boolean isTldValidationEnabled() {
        return false;
    }
    
    // ----------------- Glassfish V2 --------------
    
    public boolean genStringAsByteArray() {
        throw new UnsupportedOperationException("Not supported yet. genStringAsByteArray");
    }
    
    public boolean isDefaultBufferNone() {
        throw new UnsupportedOperationException("Not supported yet. isDefaultBufferNone");
    }
    
    public boolean isValidationEnabled() {
        LOGGER.fine("isValidationEnabled");
        return false;
    }
    
    public boolean getSaveBytecode() {
        throw new UnsupportedOperationException("Not supported yet. getSaveBytecode");
    }

    @Override
    public TldScanner getTldScanner() {
        return scanner;
    }

    private void clearStaticHashSet(Class clazz, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            HashSet s = (HashSet)f.get(null);
            s.clear();
        } catch (Exception e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }
}
