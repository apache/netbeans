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
package org.netbeans.modules.html.angular.model;

import org.netbeans.modules.javascript2.model.api.JsArray;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsObject;

/**
 *
 * @author Petr Pisl
 */
public class ModelUtils {
    // TODO this class shouldnot be there. The model utils should be a part
    // of the editor API. This is copy paste from ModelUtils
    
    public static JsObject findJsObject(JsObject object, int offset) {
        JsObject jsObject = object;
        JsObject result = null;
        JsObject tmpObject = null;
        if (jsObject.getOffsetRange().containsInclusive(offset)) {
            result = jsObject;
            for (JsObject property : jsObject.getProperties().values()) {
                JsElement.Kind kind = property.getJSKind();
                if (kind == JsElement.Kind.OBJECT
                        || kind == JsElement.Kind.ANONYMOUS_OBJECT
                        || kind == JsElement.Kind.OBJECT_LITERAL
                        || kind == JsElement.Kind.FUNCTION
                        || kind == JsElement.Kind.METHOD
                        || kind == JsElement.Kind.CONSTRUCTOR
                        || kind == JsElement.Kind.WITH_OBJECT
                        || kind == JsElement.Kind.ARROW_FUNCTION) {
                    tmpObject = findJsObject(property, offset);
                }
                if (tmpObject != null) {
                    if (tmpObject instanceof JsArray) {
                        tmpObject = null;
                        result = tmpObject;
                    } else {
                        result = tmpObject;
                        break;
                    }
                }
            }
        }
        return result;
    }
}
