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
package org.netbeans.modules.maven.grammar.effpom;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;

/**
 *
 * @author mkleint
 */
@SuppressWarnings("unchecked") // Get name clashes with Mark and AnnotationMark
 final class AnnotationMarkProvider extends MarkProvider {

    private List<AnnotationMark> marks = Collections.emptyList();

    public void setMarks (List<AnnotationMark> marks) {
        List<AnnotationMark> old = this.marks;
        this.marks = marks;
        firePropertyChange(PROP_MARKS, old, marks);
    }

    @Override
    public synchronized List getMarks () {
        return marks;
    }    
}
