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

package org.netbeans.modules.dbschema.migration.archiver.deserializer;

import java.util.ArrayList;
import java.util.EmptyStackException;

//@olsen+MBO: local class providing an unsynchronized Stack
class ArrayListStack extends ArrayList {
    public ArrayListStack() {
    }

    public Object push(Object item) {
	add(item);
	return item;
    }

    public Object pop() {
	Object	obj;
	int	len = size();

	obj = remove(len - 1);
	return obj;
    }

    public Object peek() {
	int	len = size();

	if (len == 0)
	    throw new EmptyStackException();
	return get(len - 1);
    }

    public boolean empty() {
	return size() == 0;
    }

    public int search(Object o) {
	int i = lastIndexOf(o);

	if (i >= 0) {
	    return size() - i;
	}
	return -1;
    }
}
