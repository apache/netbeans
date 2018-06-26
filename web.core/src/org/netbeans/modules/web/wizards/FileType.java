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
package org.netbeans.modules.web.wizards;

public class FileType {

    private String name,  suffix;

    private FileType(String name, String suffix) {
        this.name = name;
        this.suffix = suffix;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getSuffix() {
        return suffix;
    }
    public static final FileType SERVLET =
            new FileType("servlet", "java");
    public static final FileType FILTER =
            new FileType("filter", "java");
    public static final FileType LISTENER =
            new FileType("listener", "java");
    public static final FileType JSP =
            new FileType("jsp", "jsp");
    public static final FileType JSF =
            new FileType("jsf", "jsp");
    public static final FileType JSPDOC =
            new FileType("jspdoc", "jspx");
    public static final FileType JSPF =
            new FileType("jspf", "jspf");
    public static final FileType TAG =
            new FileType("tag_file", "tag");
    public static final FileType TAGLIBRARY =
            new FileType("tag_library", "tld");
    public static final FileType TAG_HANDLER =
            new FileType("tag_handler", "java");
    public static final FileType HTML =
            new FileType("html", "html");
    public static final FileType XHTML =
            new FileType("xhtml", "xhtml");
    public static final FileType CSS =
            new FileType("css", "css");
    public static final FileType JS =
            new FileType("javascript","js");
    
    public static String IS_XML = "isXml";          // NOI18N
    public static String IS_SEGMENT = "isSegment";  // NOI18N
    public static String IS_FACELETS= "isFacelerts";// NOI18N
} 

