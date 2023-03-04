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


package org.netbeans.modules.search;

import java.awt.EventQueue;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.openide.ErrorManager;

import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;


/**
 * Presents search results in output window.
 *
 * @author  Petr Kuzel
 * @author  Marian Petras
 */
public final class SearchDisplayer {

    /** name of attribute &quot;text to display in the Output Window&quot; */
    public static final String ATTR_OUTPUT_LINE = "output line";        //NOI18N
    /** writer to that tab */
    private OutputWriter ow = null;
    /** */
    private Reference<OutputWriter> owRef = null;

    /** Creates new SearchDisplayer */
    SearchDisplayer() {
    }

    /**
     */
    void prepareOutput() {
        String tabName = NbBundle.getMessage(ResultView.class,
                                             "TITLE_SEARCH_RESULTS");   //NOI18N
        InputOutput searchIO = IOProvider.getDefault().getIO(tabName, false);
        ow = searchIO.getOut();
        owRef = new WeakReference<>(ow);
        
        searchIO.select();
    }
    
    /**
     */
    static void clearOldOutput(final Reference<OutputWriter> outputWriterRef) {
        if (outputWriterRef != null) {
            OutputWriter oldWriter = outputWriterRef.get();
            if (oldWriter != null) {
                try {
                    oldWriter.reset();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
    }
    
    /**
     * Displays the given nodes.
     *
     * @param  nodes  nodes to display
     */
    void displayNodes(final Node[] nodes) {

        /* Prepare the output lines: */
        final String[] outputLines = new String[nodes.length];
        final OutputListener[] listeners = new OutputListener[nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            final Node node = nodes[i];
            final Object o = node.getValue(ATTR_OUTPUT_LINE);
            outputLines[i] = o instanceof String ? (String) o
                                                 : node.getShortDescription();
            listeners[i] = node instanceof OutputListener ? (OutputListener)node
                                                          : null;
        }

        /* Print the output lines: */
        try {
            EventQueue.invokeAndWait(() -> {
                try {
                    for (int i = 0; i < outputLines.length; i++) {
                        OutputListener listener = listeners[i];
                        if (listener != null) {
                            ow.println(outputLines[i], listener);
                        } else {
                            ow.println(outputLines[i]);
                        }
                    }
                } catch (Exception ex) {
                    ErrorManager.getDefault()
                            .notify(ErrorManager.EXCEPTION, ex);
                }
            });
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    /**
     */
    void finishDisplaying() {
        ow.flush();
        ow.close();
        ow = null;
    }
    
    /**
     */
    Reference<OutputWriter> getOutputWriterRef() {
        return owRef;
    }
    
}
