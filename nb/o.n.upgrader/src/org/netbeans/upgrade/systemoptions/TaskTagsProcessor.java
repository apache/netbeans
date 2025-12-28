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

package org.netbeans.upgrade.systemoptions;

/**
 * For: org.netbeans.modules.tasklist.docscan.TaskTags
 * @author Radek Matous
 */
class TaskTagsProcessor extends PropertyProcessor {
    
    /** Creates a new instance of TaskTagsProcessor */
    TaskTagsProcessor() {
        super("org.netbeans.modules.tasklist.docscan.TaskTags");//NOI18N
    }
    
    @Override
    void processPropertyImpl(String propertyName, Object value) {
        if ("taskTags".equals(propertyName)) {//NOI18N
            for (Object elem : ((SerParser.ObjectWrapper)value).data) {
                if (elem instanceof SerParser.ObjectWrapper) {
                    String clsname = Utils.prettify(((SerParser.ObjectWrapper)elem).classdesc.name);
                    if ("org.netbeans.modules.tasklist.docscan.TaskTag".equals(clsname)) {//NOI18N
                        processTag(elem);//NOI18N
                    }
                }
            }
        }  else {
            throw new IllegalStateException();
        }
    }
    
    private void processTag(final Object value) {
        String tagName = null;
        for (Object elem : ((SerParser.ObjectWrapper)value).data) {
            if (elem instanceof SerParser.ObjectWrapper) {
                String val = ((SerParser.NameValue)(((SerParser.ObjectWrapper)elem).data.get(0))).value.toString();
                assert tagName != null;
                addProperty(tagName, val);
            } else if (elem instanceof String) {
                tagName = "Tag"+(String)elem;//NOI18N
            }
        }
    }
    
}
