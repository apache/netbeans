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
package org.netbeans.modules.jumpto.common;

import java.util.Comparator;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Tomas Zezula
 */
public interface StateFullComparator<T> extends Comparator<T> {
    void addChangeListener(@NonNull ChangeListener l);
    void removeChangeListener(@NonNull ChangeListener l);
}
