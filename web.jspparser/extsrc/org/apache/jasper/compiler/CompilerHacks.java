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
