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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;

/** Reflection stuff for org.apache.jasper.compiler.Compiler.
 *
 * @author Petr Jiricka
 */
public class CompilerHacks {
    
    private static final Logger LOGGER = Logger.getLogger(CompilerHacks.class.getName());

    // @GuardedBy(this)
    private Compiler comp;
    private final JspCompilationContext ctxt;

    private static Field pageInfoF;
    private static Field errDispatcherF;

    static {
        initMethodsAndFields();
    }
    
    /** Creates a new instance of CompilerHacks */
    public CompilerHacks(JspCompilationContext ctxt) {
        this.ctxt = ctxt;
    }

    static void initMethodsAndFields() {
        try {
            // pageInfo field
            pageInfoF = Compiler.class.getDeclaredField("pageInfo"); // NOI18N
            pageInfoF.setAccessible(true);
            // errDispatcher field
            errDispatcherF = Compiler.class.getDeclaredField("errDispatcher"); // NOI18N
            errDispatcherF.setAccessible(true);
        } catch (NoSuchFieldException e) {
            LOGGER.log(Level.INFO, null, e);
        }
    }
    
    private synchronized void setupCompiler() throws JasperException {
        if (comp == null) {
            comp = ctxt.createParser();
            setErrDispatcherInCompiler(comp, new ErrorDispatcher(false));
            comp.setPageInfo(new HackPageInfo(new BeanRepository(
                ctxt.getClassLoader(), comp.getErrorDispatcher()), ctxt.getJspFile()));
        }
    }
    
    Compiler getCompiler() throws JasperException {
        setupCompiler();
        return comp;
    }
    
    private static void setErrDispatcherInCompiler(Compiler c, ErrorDispatcher errDispatcher) throws JasperException {
        try {
            errDispatcherF.set(c, errDispatcher);
        } catch (IllegalAccessException e) {
            throw new JasperException(e);
        }
    }
    
    /** Hacked PageInfo to get better XML directive data
     */
    static final class HackPageInfo extends PageInfo {

        /** Map of prefix -> uri. */
        private final Map<String, String> approxXmlPrefixMapper = new HashMap<String, String>();
        
        HackPageInfo(BeanRepository beanRepository, String jspFile) {
            super(beanRepository, jspFile);
        }
        
        @Override
        public void pushPrefixMapping(String prefix, String uri) {
            super.pushPrefixMapping(prefix, uri);
            if (uri != null) {
                synchronized (approxXmlPrefixMapper) {
                    approxXmlPrefixMapper.put(prefix, uri);
                }
            }
        }
        
        Map<String, String> getApproxXmlPrefixMapper() {
            synchronized (approxXmlPrefixMapper) {
                return approxXmlPrefixMapper;
            }
        }
    }
}
