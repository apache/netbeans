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
package org.netbeans.core.spi.multiview;

/**
 *
 * A marker interface for <code>MultiViewDescription</code> instances that allows to identify them
 * as containing source code. The associated <code>MultiViewElement</code>'s visual representation
 * is assumed to implement <code>CloneableEditorSupport.Pane</code> interface.
 * Fixes issue <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=68912">#68912</a>.
 * @author Milos Kleint
 * @since org.netbeans.core.multiview 1.10
 */
public interface SourceViewMarker {

}
