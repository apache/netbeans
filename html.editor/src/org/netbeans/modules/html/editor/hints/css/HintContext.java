/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.hints.css;

import java.util.Collection;
import java.util.Map;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marek
 */
public class HintContext {

    private final String pureElementName;
    private final String elementName;
    private final Collection<FileObject> referredFiles;
    private final Collection<FileObject> allStylesheets;
    private final Map<FileObject, Collection<String>> elements;
    private final Map<String, Collection<FileObject>> element2files;

    public HintContext(String pureElementName, String elementName, Collection<FileObject> referredFiles, Collection<FileObject> allStylesheets, Map<FileObject, Collection<String>> elements, Map<String, Collection<FileObject>> element2files) {
        this.pureElementName = pureElementName;
        this.elementName = elementName;
        this.referredFiles = referredFiles;
        this.allStylesheets = allStylesheets;
        this.elements = elements;
        this.element2files = element2files;
    }
    
    /**
     * Gets the name of the element without the type prefix (dot or hash).
     */
    public String getPureElementName() {
        return pureElementName;
    }

    public String getElementName() {
        return elementName;
    }

    public Collection<FileObject> getAllStylesheets() {
        return allStylesheets;
    }

    public Collection<FileObject> getReferredFiles() {
        return referredFiles;
    }

    public Map<FileObject, Collection<String>> getElements() {
        return elements;
    }

    public Map<String, Collection<FileObject>> getElement2files() {
        return element2files;
    }
}
