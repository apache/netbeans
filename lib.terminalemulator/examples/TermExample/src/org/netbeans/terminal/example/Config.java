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

package org.netbeans.terminal.example;

/**
 *
 * @author ivan
 */
public final class Config {
    public static enum Provider {
	DEFAULT,
	TERM,
    }

    public static enum DispatchThread {
	EDT,
	RP,
    }

    public static enum Execution {
	RICH,
	NATIVE,
    }

    public static enum IOShuttling {
	INTERNAL,
	EXTERNAL,
    }

    public static enum ContainerStyle {
	TABBED,
	MUXED,
    }

    public enum AllowClose {
	 /**
	  * Tab is unclosable.
	  * This will control IOVisibility.setClosable()
	  */
	NEVER,

	/**
	 * Tab is closable. a vetoableChange() will always be called on
	 * IOVisibility.VISIBILITY.
	 */
	ALWAYS,
	/**
	 * Tab is closable. a vetoableChange() will always be called on
	 * IOVisibility.VISIBILITY and it's supposed to allow closing
	 * w/o confirmation if IOConnect.isConnected() is false.
	 */
	DISCONNECTED
    }

    private final String command;
    private final Provider containerProvider;
    private final Provider ioProvider;
    private final AllowClose allowClose;
    private final DispatchThread dispatchThread;
    private final Execution execution;
    private final IOShuttling ioShuttling;
    private final ContainerStyle containerStyle;
    private final boolean restartable;
    private final boolean hupOnClose;
    private final boolean keep;
    private final boolean debug;

    public Config(
		String command,
		Provider containerProvider,
		Provider ioProvider,
		AllowClose allowClose,
		DispatchThread dispatchThread,
		Execution execution,
		IOShuttling ioShuttling,
		ContainerStyle containerStyle,
		boolean restartable,
		boolean hupOnClose,
		boolean keep,
                boolean debug
	    ) {
	this.command  = command;
	this.containerProvider  = containerProvider;
	this.ioProvider  = ioProvider;
	this.allowClose  = allowClose;
	this.dispatchThread  = dispatchThread;
	this.execution  = execution;
	this.ioShuttling  = ioShuttling;
	this.containerStyle = containerStyle;
	this.restartable  = restartable;
	this.hupOnClose  = hupOnClose;
	this.keep = keep;
	this.debug = debug;
    }

    public static Config getShellConfig() {
	return new Config("/bin/bash",
	                  null,
	                  null,
	                  AllowClose.ALWAYS,
	                  null,
	                  null,
	                  IOShuttling.INTERNAL,
			  ContainerStyle.TABBED,
	                  false,	// restartable
	                  true,		// hupOnClose
			  false,	// keep
                          false         // debug
			  );
    }

    public static Config getCmdConfig(String command) {
	return new Config(command,
	                  null,
	                  null,
	                  AllowClose.ALWAYS,
	                  null,
	                  null,
	                  IOShuttling.INTERNAL,
			  ContainerStyle.TABBED,
	                  true,		// restartable
	                  true,		// hupOnClose
			  false,	// keep
                          false         // debug
			  );
    }

    public String getCommand() {
	return command;
    }

    public Provider getContainerProvider() {
	return containerProvider;
    }

    public Provider getIOProvider() {
	return ioProvider;
    }

    public AllowClose getAllowClose() {
	return allowClose;
    }

    public DispatchThread getThread() {
	return dispatchThread;
    }

    public Execution getExecution() {
	return execution;
    }

    public boolean isRestartable() {
	return restartable;
    }

    public boolean isHUPOnClose() {
	return hupOnClose;
    }

    public boolean isKeep() {
	return keep;
    }

    public boolean isDebug() {
        return debug;
    }

    public IOShuttling getIOShuttling() {
	return ioShuttling;
    }

    public ContainerStyle getContainerStyle() {
	return containerStyle;
    }
}
