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

package org.netbeans.modules.editor;


import org.netbeans.editor.DialogSupport;
import org.netbeans.modules.editor.impl.NbDialogFactory;

/** The NetBeans way of handling Dialogs is through TopManager,
 * prividing it with DialogDescriptor.
 *
 * @author  Petr Nejedly
 * @version 1.0
 * @deprecated Without any replacement.
 */
@Deprecated
public class NbDialogSupport extends NbDialogFactory implements DialogSupport.DialogFactory {
    public NbDialogSupport() {
        
    }
}
