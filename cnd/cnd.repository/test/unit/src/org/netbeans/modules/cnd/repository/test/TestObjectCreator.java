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

package org.netbeans.modules.cnd.repository.test;

import java.io.*;
import java.util.*;
import org.netbeans.modules.cnd.repository.spi.Key;

/**
 * Creates a set of TestObject objects
 * that correspond to file by the given path
 */
public class TestObjectCreator {

    private static int currUnitId = 0;

    private final int unitId;
    private String unit;
    private Key.Behavior behavior;
    
    public TestObjectCreator() {
        this("Test", Key.Behavior.Default);
    }
    
    public TestObjectCreator(String unit, Key.Behavior behavior) {
        this.unitId = currUnitId++;
        this.unit = unit;
        this.behavior = behavior;
    }
    
    public Collection<TestObject> createTestObjects(String... args) throws FileNotFoundException {
	Collection<TestObject> objects = new ArrayList<TestObject>();
	for (int i = 0; i < args.length; i++) {
            final File file = new File(args[i]);
            if (!file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
	    createTestObjects(file, objects);
	}
	return objects;
    }
    
    public Collection<TestObject> createTestObjects(List<String> args) {
	Collection<TestObject> objects = new ArrayList<TestObject>();
	for( String path : args ) {
	    createTestObjects(new File(path), objects);
	}
	return objects;
    }
    
    private void createTestObjects(File file, Collection<TestObject> objects) {
	TestObject  obj = new TestObject(file.getAbsolutePath(), unitId, unit, behavior);
	if( file.exists() ) {
	    obj.lData = file.length();
	    objects.add(obj);
	    if( file.isDirectory() ) {
		obj.sData = file.list();
		File[] children = file.listFiles();
		if( children != null ) {
		    for (int i = 0; i < children.length; i++) {
                        createTestObjects(children[i], objects);
                    }
		}
	    }
        }
    }
}
