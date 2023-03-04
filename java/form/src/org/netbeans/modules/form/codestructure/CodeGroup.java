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

package org.netbeans.modules.form.codestructure;

import java.util.Iterator;

/**
 * @author Tomas Pavek
 */

public interface CodeGroup {

    public void addStatement(CodeStatement statement);
    public void addStatement(int index, CodeStatement statement);

    public void addGroup(CodeGroup group);
    public void addGroup(int index, CodeGroup group);

    public CodeStatement getStatement(int index);

    public int indexOf(Object object);

    public void remove(Object object);
    public void remove(int index);
    public void removeAll();

    public Iterator getStatementsIterator();
}
