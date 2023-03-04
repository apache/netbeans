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

package org.netbeans.modules.schema2beans;

public class Version implements java.io.Serializable {
    public static final int MAJVER = 5;
    public static final int MINVER = 0;
    public static final int PTCVER = 0;

	private int major;
	private int minor;
	private int patch;
	
	public Version(int major, int minor, int patch) {
	    this.major = major;
	    this.minor = minor;
	    this.patch = patch;
	}
	
	public int getMajor() {
	    return this.major;
	}
	
	public int getMinor() {
	    return this.minor;
	}
	
	public int getPatch() {
	    return this.patch;
	}

    /**
     * Returns the current version of the runtime system.
     */
	public static String getVersion() {
	    return "version " + MAJVER + "." + MINVER + "." + PTCVER;
	}
}
