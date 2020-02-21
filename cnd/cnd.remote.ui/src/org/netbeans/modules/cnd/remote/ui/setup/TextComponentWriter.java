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
package org.netbeans.modules.cnd.remote.ui.setup;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.openide.util.Exceptions;

/**
 * An utility class that prints into text component;
 * all calls to this component are performed in the UI thread.
 *
 */
public final class TextComponentWriter extends PrintWriter {

    public TextComponentWriter(final JTextComponent textPane) {
        super(new Writer() {

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                final String value = new String(cbuf, off, len);
                addOuputTextInUiThread(value, textPane);
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() throws IOException {
            }
        });

    }

    private static void addOuputTextInUiThread(final String value, final JTextComponent textPane) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                textPane.setText(textPane.getText() + value);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InterruptedException ex) {
                // it's normal! for example user could cancel dialog...
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
