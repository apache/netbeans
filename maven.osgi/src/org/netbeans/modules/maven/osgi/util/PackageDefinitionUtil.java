/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
