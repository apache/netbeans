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
package org.netbeans.api.print;

import java.io.InputStream;
import javax.swing.Action;
import javax.swing.JComponent;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.netbeans.spi.print.PrintProvider;

/**
 * <p class="nonnormative">
 * The Print Manager is powerful functionality to preview and
 * send data out to printer. Print action from <code>File</code>
 * menu (<code>Ctrl+Alt+Shift+P</code> shortcut) invokes the Print Preview
 * dialog. The Print Preview dialog provides page layout, the set of options
 * including font, color, header, footer, printer settings such as paper size
 * and orientation, number of copies, margins, collation and system properties.</p>
 *
 * There are several ways to enable printing for a custom data:<p>
 *
 * If the data is a Swing component which extends {@linkplain JComponent}
 * and shown in a {@link TopComponent}, the key {@linkplain #PRINT_PRINTABLE}
 * with value <code>"Boolean.TRUE"</code> in the component must be set as a
 * client property. See example:
 *
 * <blockquote><pre>
 * public class MyComponent extends javax.swing.JComponent {
 *   public MyComponent() {
 *     ...
 *     putClientProperty("print.printable", Boolean.TRUE); // NOI18N
 *   }
 *   ...
 * }</pre></blockquote>
 *
 * The key {@linkplain #PRINT_NAME} is used to specify the name of the component
 * which will be printed in the header/footer:
 *
 * <blockquote><pre>
 * putClientProperty("print.name", &lt;name&gt;); // NOI18N</pre></blockquote>
 *
 * If the key is not set at all, the display name of the top
 * component is used by default. The content of the header/footer
 * can be adjusted in the Print Options dialog.<p>
 *
 * If the size of the custom component for printing differs from visual
 * dimension, specify this with the key {@linkplain #PRINT_SIZE}:
 *
 * <blockquote><pre>
 * putClientProperty("print.size", new Dimension(printWidth, printHeight)); // NOI18N</pre></blockquote>
 *
 * If the custom data is presented by several components, all of them can
 * be enabled for print preview. The key {@linkplain #PRINT_ORDER} is used
 * for this purpose, all visible and printable components are ordered
 * and shown in the Print Preview dialog from the left to right:
 *
 * <blockquote><pre>
 * putClientProperty("print.order", &lt;order&gt;); // NOI18N</pre></blockquote>
 *
 * If the custom data is presented by another classes, a {@link PrintProvider}
 * should be implemented and put in the {@linkplain TopComponent#getLookup lookup}
 * of the {@linkplain TopComponent top component} where the custom data lives.
 *
 * How to put the Print action on custom Swing tool bar:
 *
 * <blockquote><pre>
 * public class MyComponent extends javax.swing.JComponent {
 *   ...
 *   JToolBar toolbar = new JToolBar();
 *   // print
 *   toolbar.addSeparator();
 *   toolbar.add(PrintManager.printAction(this));
 *   ...
 * }</pre></blockquote>
 *
 * How does <code>Print</code> action from the main menu decide what to print?<p>
 *
 * At first, the manager searches for {@link PrintProvider} in the
 * {@linkplain TopComponent#getLookup lookup} of the active top component.
 * If a print provider is found, it is used by the print manager for print preview.<p>
 *
 * Otherwise, it tries to obtain {@linkplain #PRINT_PRINTABLE printable} components
 * among the {@linkplain java.awt.Container#getComponents descendants} of the active
 * top component. All found printable components are passed into the Print Preview
 * dialog. Note that {@linkplain JComponent#print print} method is invoked by
 * the manager for preview and printing the component.<p>
 *
 * If there are no printable components, printable data are retrieved from the 
 * {@linkplain TopComponent#getActivatedNodes selected nodes} of the active top component.
 * The Print manager gets {@link EditorCookie} or {@link InputStream} from the selected
 * {@linkplain org.openide.nodes.Node nodes}. The {@link javax.swing.text.StyledDocument},
 * returned by the editor cookie, or string, returned by the stream, contain printing
 * information (text, font, color). This information is shown in the print preview.
 * So, any textual documents (Java/C++/Php/... sources, html, xml, plain text, etc.)
 * are printable by default.
 *
 * @see org.netbeans.spi.print.PrintProvider
 *
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.12
 */
public final class PrintManager {

    /**
     * This key indicates the name of the component being printed.
     * By default, the name is shown in the left part of the header.
     */
    public static final String PRINT_NAME = "print.name"; // NOI18N
    /**
     * This key indicates the order of the component being printed.
     * The value of the key must be {@link Integer}. All visible and
     * printable components are ordered and shown in the Print Preview
     * dialog from the left to right.
     */
    public static final String PRINT_ORDER = "print.order"; // NOI18N
    /**
     * This key indicates the size of the component being printed.
     * The value of the key must be {@link java.awt.Dimension}.
     */
    public static final String PRINT_SIZE = "print.size"; // NOI18N
    /**
     * This key indicates whether the component is printable. To be printable the
     * value {@link Boolean#TRUE} must be set as a client property of the component.
     */
    public static final String PRINT_PRINTABLE = "print.printable"; // NOI18N

    /**
     * Creates a new instance of <code>PrintManager</code>.
     */
    private PrintManager() {}

    /**
     * Returns the Print action for a component.
     * All {@linkplain #PRINT_PRINTABLE printable} components are obtained among the
     * {@linkplain java.awt.Container#getComponents descendants} of the given component.
     * All found printable components are passed into the Print Preview dialog.
     *
     * @param component is the component being printed
     * @return the Print action
     * @see PrintProvider
     */
    public static Action printAction(JComponent component) {
        return new org.netbeans.modules.print.action.PrintAction(component);
    }

    /**
     * Returns the Print action for the given {@linkplain PrintProvider print providers}.
     * All {@link org.netbeans.spi.print.PrintPage}s returned by the providers are
     * shown in the Print Preview dialog.
     *
     * @param providers is the array of print providers
     * @return the Print action
     * @see PrintProvider
     */
    public static Action printAction(PrintProvider[] providers) {
        return new org.netbeans.modules.print.action.PrintAction(providers);
    }
}
