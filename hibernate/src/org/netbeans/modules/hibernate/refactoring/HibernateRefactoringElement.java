/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
