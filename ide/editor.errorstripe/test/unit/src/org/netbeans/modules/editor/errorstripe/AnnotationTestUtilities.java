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

package org.netbeans.modules.editor.errorstripe;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import junit.framework.*;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationType.Severity;
import org.netbeans.editor.AnnotationTypes;
import org.netbeans.editor.AnnotationTypes.Loader;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;


/**
 *
 * @author Jan Lahoda
 */
public class AnnotationTestUtilities extends TestCase {
    
    /*package private*/ static final String NAME_TEST_ANNOTATION_DESC1 = "test-annotation-1";
    /*package private*/ static final String NAME_TEST_ANNOTATION_DESC2 = "test-annotation-2";
    
    /*package private*/ static final String SD_TEST_ANNOTATION_DESC1 = "Test1";
    /*package private*/ static final String SD_TEST_ANNOTATION_DESC2 = "Test2";
    
    
    /*package private*/ static class TestAnnotationDesc1 extends AnnotationDesc {
        
        private BaseDocument doc;
        private Position position;
        
        public TestAnnotationDesc1(BaseDocument bd, Position position) {
            super(position.getOffset(), -1);
            this.doc      = bd;
            this.position = position;
        }
        
        public String getShortDescription() {
            return SD_TEST_ANNOTATION_DESC1;
        }

        public String getAnnotationType() {
            return NAME_TEST_ANNOTATION_DESC1;
        }

        public int getOffset() {
            return position.getOffset();
        }

        public int getLine() {
            try {
                return Utilities.getLineOffset(doc, getOffset());
            } catch (BadLocationException e) {
                IllegalStateException exc = new IllegalStateException();
                
                exc.initCause(e);
                
                throw exc;
            }
        }
        
    }
    
    /*package private*/ static class TestAnnotationDesc2 extends AnnotationDesc {
        
        private BaseDocument doc;
        private Position position;
        
        public TestAnnotationDesc2(BaseDocument bd, Position position) {
            super(position.getOffset(), -1);
            this.doc      = bd;
            this.position = position;
        }
        
        public String getShortDescription() {
            return SD_TEST_ANNOTATION_DESC2;
        }

        public String getAnnotationType() {
            return NAME_TEST_ANNOTATION_DESC2;
        }

        public int getOffset() {
            return position.getOffset();
        }

        public int getLine() {
            try {
                return Utilities.getLineOffset(doc, getOffset());
            } catch (BadLocationException e) {
                IllegalStateException exc = new IllegalStateException();
                
                exc.initCause(e);
                
                throw exc;
            }
        }
        
    }
    
}
