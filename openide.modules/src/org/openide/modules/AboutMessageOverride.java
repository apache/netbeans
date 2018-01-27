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
package org.openide.modules;

import org.openide.util.lookup.ServiceProvider;

/**
 * An interface to allow you to change what is displayed in the about window.
 * Register it with {@link ServiceProvider}
 * @author astephens
 */
public interface AboutMessageOverride {
	public String formatAboutText(String bundleString, String productVersion,
			String javaVersion, String vmVersion, String os, String encoding,
			String locale, String userDir, String cacheDir, String updates,
			int fontSize, String javaRuntime);
}
