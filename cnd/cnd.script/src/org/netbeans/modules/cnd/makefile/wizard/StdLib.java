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

package  org.netbeans.modules.cnd.makefile.wizard;

public final class StdLib{
    // Each flag is a boolean for using/not using a library
    private boolean used;
    private String cmd;
    private String name;
    private char mnemonic;

    /**
     * Constructor
     */
    StdLib(String name, char mnemonic, String cmd) {
	this.name = name;
	this.mnemonic = mnemonic;
	this.cmd = cmd;
	used = false;
    }

    StdLib(StdLib old) {
	this.name = old.getName();
	this.mnemonic = old.getMnemonic();
	this.cmd = old.getCmd();
	this.used = old.isUsed();
    }

    /** Getter and setter for the used flag */
    public boolean isUsed() {
	return used;
    }
    public void setUsed(boolean used) {
	this.used = used;
    }

    /** Getter and setter for name */
    public String getName() {
	return name;
    }
    public void setName(String name) {
	this.name = name;
    }

    /** Getter and setter for cmd */
    public String getCmd() {
	return cmd;
    }
    public void setCmd(String cmd) {
	this.cmd = cmd;
    }

    /** Getter and setter for mnemonic */
    public char getMnemonic() {
	return mnemonic;
    }
    public void setMnemonic(char mnemonic) {
	this.mnemonic = mnemonic;
    }
}

