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

package org.netbeans.editor;

/**
 * Short description of this extension of base action is localized by this
 * (org.netbeans.editor) package by using <code>BaseKit.class</code> bundle class.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

abstract class LocalBaseAction extends BaseAction {

    public LocalBaseAction() {
        super();
    }

    public LocalBaseAction(int updateMask) {
        super(updateMask);
    }

    public LocalBaseAction(String name) {
        super(name);
    }

    public LocalBaseAction(String name, int updateMask) {
        super(name, updateMask);
    }

    protected Class getShortDescriptionBundleClass() {
        return BaseKit.class;
    }

}
