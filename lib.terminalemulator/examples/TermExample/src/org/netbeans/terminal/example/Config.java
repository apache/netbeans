/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
