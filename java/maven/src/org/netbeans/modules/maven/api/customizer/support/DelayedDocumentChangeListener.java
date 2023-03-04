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

package org.netbeans.modules.maven.api.customizer.support;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 *
 * @author Dafe Simonek
 */
public final class DelayedDocumentChangeListener implements DocumentListener {

    public static DocumentListener create (Document doc, ChangeListener l, int delay) {
        return new DelayedDocumentChangeListener(doc, l, delay);
    }

    private Document doc;
    private Timer changeTimer;
    private ChangeEvent chEvt;

    @SuppressWarnings("LeakingThisInConstructor")
    private DelayedDocumentChangeListener (Document doc, final ChangeListener l, int delay) {
        this.doc = doc;
        this.doc.addDocumentListener(this);
        this.chEvt = new ChangeEvent(doc);
        changeTimer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l.stateChanged(chEvt);
            }
        });
        changeTimer.setRepeats(false);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        maybeChange(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        maybeChange(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        maybeChange(e);
    }

    private void maybeChange(DocumentEvent e) {
        changeTimer.restart();
    }

}
