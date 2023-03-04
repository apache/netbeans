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

package org.netbeans.modules.maven.osgi.util;

/**
 *
 * @author johnsonlau@netbeans.org
 */
public class PackageDefinitionUtil {

	/**
	 * Omits all directives in a package definition.
	 *
	 * @param definition Bnd package definition to operate on.
	 * @return the Bnd package definition without any directive.
	 */
	public static String omitDirectives(String definition) {
		final StringBuilder sb = new StringBuilder();
		visitPackageDefinition(definition, new PackageDefinitionVisitor() {

			private boolean quoted = false;

			private boolean directive = false;

			@Override
			public void startPattern() {
			}

			@Override
			public void endPattern() {
				sb.append(",");
			}

			@Override
			public void startDirective() {
				directive = true;
			}

			@Override
			public void endDirective() {
				directive = false;
			}

			@Override
			public void startQuotedString() {
				quoted = true;
			}

			@Override
			public void endQuotedString() {
				quoted = false;
			}

			@Override
			public void onCharacter(char c) {
				if (!quoted && !directive) {
					sb.append(c);
				}
			}

		});
		int length = sb.length();
		return sb.substring(0, length > 0 ? length - 1 : length);
	}

	public static void visitPackageDefinition(String definition, PackageDefinitionVisitor visitor) {
		boolean pattern = false;
		boolean quotedString = false;
		boolean directive = false;
		for (char c : definition.toCharArray()) {
			if (c == '\"') {
				quotedString = !quotedString;
				if (quotedString) {
					visitor.startQuotedString();
				} else {
					visitor.endQuotedString();
				}
				continue;
			}

			if (!quotedString) {
				if (c == ';') {
					if (directive) {
						visitor.endDirective();
					}
					visitor.startDirective();
					directive = true;
					continue;

				} else if (c == ',') {
					if (directive) {
						visitor.endDirective();
					}
					if (pattern) {
						visitor.endPattern();
					}
					directive = false;
					visitor.startPattern();
					pattern = true;
					continue;
				}
			}

			if (!pattern) {
				visitor.startPattern();
				pattern = true;
			}

			visitor.onCharacter(c);
		}
		// If there is still unclosed quotation, close it
		if (quotedString) {
			visitor.endQuotedString();
		}
		if (directive) {
			visitor.endDirective();
		}
		if (pattern) {
			visitor.endPattern();
		}
	}

}
