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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.spi.webmodule;

import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 * List of templates which should be in the initial "privileged" list
 * when creating a new file. An instance should be placed in the
 * <code>j2ee/webtier/templates</code> folder in a module layer.
 *
 * @author Petr Pisl
 */
public interface WebPrivilegedTemplates {

    /**
     * Returns the list of templates which should be added in the initial "privileged" list
     * when created a new file.
     *
     * @param  webModule the web module to return the templates for.
     *         For example, it can be used to find out whether the web module is extended
     *         by a framework and then appropriate templates for the framework can be offered
     *         in the list of privileged list of templates.
     *
     * @return full paths to privileged templates, e.g. <samp>Templates/Other/XmlFile.xml</samp>; never null.
     */
    public String[] getPrivilegedTemplates(WebModule webModule);
}
