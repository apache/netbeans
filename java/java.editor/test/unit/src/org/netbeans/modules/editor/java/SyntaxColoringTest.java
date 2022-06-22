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
package org.netbeans.modules.editor.java;

import javax.swing.JEditorPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.openide.text.CloneableEditorSupport;

public class SyntaxColoringTest extends NbTestCase {

	private int tokenCount;

	public SyntaxColoringTest(String name) {
		super(name);
	}

	public void testTokenHierarchy() {
		JEditorPane pane = new JEditorPane();
		pane.setEditorKit(CloneableEditorSupport.getEditorKit(JavaKit.JAVA_MIME_TYPE));
		pane.setText("public class Hello {}");
		AbstractDocument document = (AbstractDocument) pane.getDocument();
		document.readLock();
		TokenHierarchy<Document> tokenHierarchy = TokenHierarchy.get(document);
		if (tokenHierarchy != null) {
			TokenSequence<?> tokenSequence = tokenHierarchy.tokenSequence();
			if (tokenSequence != null) {
				tokenCount = tokenSequence.tokenCount();
			}
		}
		assertEquals(9, tokenCount);
	}

	@Override
	protected boolean runInEQ() {
		return true;
	}
}
