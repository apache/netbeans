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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import java.io.IOException;
import java.util.Map;
import javax.enterprise.deploy.spi.TargetModuleID;

/** This interface should be implemented by plugins that want to support JSP
 * source level debugging, but do not support JSR 45.
 * The prerequisite for the JSP debugging support (without JSR 45) is the support
 * for finding a generated servlet, represented by the FindJSPServlet interface.
 *
 * @author Petr Jiricka
 */
public interface OldJSPDebug extends FindJSPServlet {

    /** Creates a servlet <-> JSP mapping for a given JSP. May be null if the server is not running
     *  or the page has not been compiled. Also may be null if the server plugin does not support
     *  creation of the line mapping information.
     * @param module web module in which the JSP is located.
     * @param jspResourcePath the path of the JSP for which the mapping is requested, e.g.
     *  "pages/login.jsp". Never starts with a '/'.
     * @return JspSourceMapper for this JSP.
     */
    public JspSourceMapper getSourceMapper(TargetModuleID module, String jspResourcePath);
    
    /** Provides common interface for JSP <----> Java mappings */
    public interface JspSourceMapper {
        
        /** Converts the JSP file name (from the string int the servlet comments into
         * the Forte4J resource name)
         */
        public interface NameConverter {
            /** Converts the JSP name (from the string int the servlet comments into
             * the Forte4J resource name)
             *@param name - JSP name to convert
             */
            String convert(String name) throws IOException;
        }
        
        /**
         * Get the value of primaryJspFileName.
         * @return Value of primaryJspFileName.
         */
        String getPrimaryJspFileName();
        
        /**
         * Set the value of primaryJspFileName.
         * @param v  Value to assign to primaryJspFileName.
         */
        public void setPrimaryJspFileName(String v);
        
        /**
         * Returns the number of entries in this source mapper. This number is 0, if no entries were made.
         * @return The number of entries in this source mapper. This number is 0, if no entries were made.
         */
        int size();
        
        /**
         * Returns whether the JSP page is empty
         * @return  Whether the JSP page is empty
         */
        boolean isEmpty();
        
        
        /* *
         * Converts a position in the JAVA_CODE to a JSP_CODE range.
         * @param jspFileName JSP file name to get forward mapping for
         * @param range Range to convert

        // Range javaToJsp(String jspFile, Position position );

        /* *
         * Convert a range in the JSP_CODE to a JAVA_CODE range.
         * @param jspFileName JSP file name to get forward mapping for
         * @param range Range to convert
         */
        
        // Range jspToJava(String jspFile, Range range);
        
        /**
         * Returns Java Servlet line number for the given line number of the primary (not "included") JSP file
         * @param line unmangled (JSP) line number
         * @return The mangled (Servlet) line number
         */
        int mangle(int line);
        
        /**
         * Returns Java Servlet line number for the given line/column number of the primary (not "included") JSP file
         * @param line unmangled (JSP) line number
         * @param col  unmangled (JSP) column number
         * @return The mangled (Servlet) line number
         */
        int mangle(int line, int col);
        
        /**
         * Returns Java Servlet line number for the given line number of the given JSP
         * @param jspFile
         */
        int mangle(String jspFile, int line);
        
        /**
         * Returns Java Servlet line/column number for the given line number of the given JSP
         * @param jspFileName Name of the JSP file to map
         * @param line unmangled (JSP) line number
         * @param col  unmangled (JSP) column number
         * @return The mangled (Servlet) line number
         */
        int mangle(String jspFileName, int line, int col);
        
        /**
         * Returns primary (not "included") JSP file line number for the given line number in the generated Java Servlet
         * @param line mangled (Servlet) line number
         * @return The unmangled (JSP) line number
         */
        int unmangle(int line);
        
        /**
         * Returns primary (not "included") JSP file line/column number for the given line number
         * in the generated Java Servlet
         * @param line mangled (Servlet) line number
         * @param col  mangled (Servlet) column number
         * @return The unmangled (JSP) line number
         */
        int unmangle(int line, int col);
        
        /**
         * Returns JSP file line/column number for the given line number in the generated Java Servlet
         * @param jspFileName JSP file name
         * @param line mangled (Servlet) line number
         * @param col  mangled (Servlet) column number
         * @return The unmangled (JSP) line number
         */
        int unmangle(String jspFileName, int line, int col);
        
        /**
         * Returns the type of the generated Servlet line/column
         * @param line mangled (Servlet) line number
         * @param col  mangled (Servlet) column number
         * @return The type of the generated Servlet line/column
         */
        String getJavaLineType(int line, int col);
        
        /**
         * Returns the JSP file name for the given line/column in the generated Servlet
         * @param line mangled (Servlet) line number
         * @param col  mangled (Servlet) column number
         * @return The JSP file name for the given line/column in the generated Servlet
         */
        public String getJspFileName(int line, int col) throws IOException;
        
        
        /**
         * Returns whether the giver Servlet line/column is a part of dynamic (Java) code in the JSP page
         * @param line mangled (Servlet) line number
         * @param col  mangled (Servlet) column number
         * @return Whether the giver Servlet line/column is a part of dynamic (Java) code in the JSP page
         */
        boolean isJavaCodeInJspPage(int line, int col);
        
        /** @return a Map of all JSP file names comprising the generated Java servlet */
        Map getFileNames();
        
        /**
         * Returns whether the primary JSP page has included files (HTML, JSP etc)
         *@return Whether the primary JSP page has included files (HTML, JSP etc)
         */
        public boolean hasIncludeFiles();
        
        /**
         * Returns whether the given name is a proper JSP file name
         * @param name given name
         *@return  whether the given name is a proper JSP file name
         */
        public boolean isProperJspFileName(String name);
    }    
    
}
