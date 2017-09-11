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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.refactoring;

import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;

/**
 * A refactoring element for refactoring the mapped the class name
 * 
 * @author Dongmei Cao
 */
public class HibernateRefactoringElement extends SimpleRefactoringElementImplementation {

    private FileObject mappingFileObject;
    private PositionBounds position;
    private String text;
    String origName;
    String matching;

    public HibernateRefactoringElement(FileObject fo, String oldName, PositionBounds position, String text) {
        this(fo, oldName, oldName, position, text);
    }
    
    /**
     * 
     * @param fo
     * @param oldName
     * @param matching - in some cases refactoring text do not match class name as it may be splitted for package part and for class part (in mapping.xml)
     * @param position
     * @param text 
     */
    public HibernateRefactoringElement(FileObject fo, String oldName, String matching, PositionBounds position, String text) {
        this.mappingFileObject = fo;
        this.origName = oldName;
        this.position = position;
        this.text = text;
        this.matching = matching !=null && matching.length()>0 ? matching : oldName;
    }
    
    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public String getDisplayText() {
        return fixDisplayText(getText());
    }

    @Override
    public void performChange() {
        // Do nothing here.
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public FileObject getParentFile() {
        return mappingFileObject;
    }

    @Override
    public PositionBounds getPosition() {
        return position;
    }

    private String fixDisplayText(String displayText) {
        String finalText = displayText.replaceAll("<", "&lt;");
        finalText = finalText.replaceAll(">", "&gt;");
        // TODO: will not split properly for cases, such as,
        // <property column="name" name="name"/>. Will fix it later
        String[] subStrings = finalText.split(matching);
        return subStrings[0] + "<b>" + matching + "</b>" + subStrings[1];
    }
}
