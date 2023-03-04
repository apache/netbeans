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
package org.netbeans.modules.websvc.editor.hints.fixes;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit
 */
public class SetAnnotationArgument extends AddAnnotationArgument {

    /** Creates a new instance of SetAnnotationArgument */
    public SetAnnotationArgument(FileObject fileObject, Element element,
            AnnotationMirror annMirror, String argumentName, Object argumentValue) {
        super(fileObject, element, annMirror, argumentName, argumentValue);
    }

    @Override
    public String getText(){
        return NbBundle.getMessage(RemoveAnnotation.class, 
                "LBL_AddAnnotationAttribute_SetToValue",argumentName,argumentValue);
    }

}
