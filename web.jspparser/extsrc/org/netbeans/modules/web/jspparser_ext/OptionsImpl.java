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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
