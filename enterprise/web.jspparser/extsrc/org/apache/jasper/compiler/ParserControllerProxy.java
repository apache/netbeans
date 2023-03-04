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

package org.apache.jasper.compiler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;

/**
 *
 * @author Petr Jiricka
 */
public class ParserControllerProxy {
    
    private static final Logger LOGGER  = Logger.getLogger(ParserControllerProxy.class.getName());

    private final JspCompilationContext ctxt;
    private final Compiler compiler;
    private final ParserController pc;
    
    boolean isXml;
    String sourceEnc;
    
    private static Method getJarFileM;
    private static Method resolveFileNameM;
    private static Method getJspConfigPageEncodingM;
    private static Method determineSyntaxAndEncodingM;
    private static Field isXmlF;
    private static Field sourceEncF;
    
    static {
        initMethodsAndFields();
    }
    
    /** Creates a new instance of ParserControllerProxy */
    public ParserControllerProxy(JspCompilationContext ctxt, Compiler compiler) {
        this.ctxt = ctxt; 
	this.compiler = compiler;
        pc = new ParserController(ctxt, compiler);
    }
    
    public static void initMethodsAndFields() {
        try {
            // getJarFile method
            getJarFileM = ParserController.class.getDeclaredMethod("getJarFile", new Class[] {URL.class}); // NOI18N
            getJarFileM.setAccessible(true);
            // resolveFileName method
            resolveFileNameM = ParserController.class.getDeclaredMethod("resolveFileName", new Class[] {String.class}); // NOI18N
            resolveFileNameM.setAccessible(true);
            // getJspConfigPageEncoding method
            getJspConfigPageEncodingM = ParserController.class.getDeclaredMethod("getJspConfigPageEncoding", new Class[] {String.class}); // NOI18N
            getJspConfigPageEncodingM.setAccessible(true);
            // determineSyntaxAndEncoding method
            determineSyntaxAndEncodingM = ParserController.class.getDeclaredMethod("determineSyntaxAndEncoding", new Class[]  // NOI18N
                {String.class, JarFile.class, String.class});
            determineSyntaxAndEncodingM.setAccessible(true);
            // isXML field
            isXmlF = ParserController.class.getDeclaredField("isXml"); // NOI18N
            isXmlF.setAccessible(true);
            // sourceEnc field
            sourceEncF = ParserController.class.getDeclaredField("sourceEnc"); // NOI18N
            sourceEncF.setAccessible(true);
        } catch (NoSuchMethodException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (NoSuchFieldException e) {
            LOGGER.log(Level.INFO, null, e);
        }
    }
    
    public void extractSyntaxAndEncoding(String inFileName)
	throws FileNotFoundException, JasperException, IOException {
	// If we're parsing a packaged tag file or a resource included by it
	// (using an include directive), ctxt.getTagFileJar() returns the 
	// JAR file from which to read the tag file or included resource,
	// respectively.
	extractSyntaxAndEncoding(inFileName, ctxt.getTagFileJarUrl());
    }
    
    private void extractSyntaxAndEncoding(String inFileName, URL jarFileUrl) 
        throws FileNotFoundException, JasperException, IOException {
        try {
            JarFile jarFile = (JarFile)getJarFileM.invoke(pc, new Object[] {jarFileUrl});
            String absFileName = (String)resolveFileNameM.invoke(pc, new Object[] {inFileName});
            String jspConfigPageEnc = (String)getJspConfigPageEncodingM.invoke(pc, new Object[] {absFileName});

            // Figure out what type of JSP document and encoding type we are
            // dealing with
            determineSyntaxAndEncodingM.invoke(pc, new Object[] {absFileName, jarFile, jspConfigPageEnc});
            
            // now the isXml and sourceEnc variables of ParserController have values
            isXml = ((Boolean)isXmlF.get(pc));
            sourceEnc = (String)sourceEncF.get(pc);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.INFO, null, e);
            throw new JasperException(e);
        } catch (InvocationTargetException e) {
            Throwable r = e.getTargetException();
            if (r instanceof RuntimeException) {
                throw (RuntimeException)r;
            }
            if (r instanceof FileNotFoundException) {
                throw (FileNotFoundException)r;
            }
            if (r instanceof JasperException) {
                throw (JasperException)r;
            }
            if (r instanceof IOException) {
                throw (IOException)r;
            }
            LOGGER.log(Level.INFO, null, e);
            throw new JasperException(e);
        }
    }
}
