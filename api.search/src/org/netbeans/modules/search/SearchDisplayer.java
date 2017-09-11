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
        owRef = new WeakReference<OutputWriter>(ow);
        
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
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
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
