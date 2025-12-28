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
package org.netbeans.modules.print.action;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import javax.swing.JComponent;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.PrintCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

import org.netbeans.api.print.PrintManager;
import org.netbeans.spi.print.PrintProvider;
import org.netbeans.modules.print.provider.ComponentProvider;
import org.netbeans.modules.print.provider.EditorProvider;
import org.netbeans.modules.print.provider.TextProvider;
import org.netbeans.modules.print.ui.Preview;
import org.netbeans.modules.print.util.Config;
import static org.netbeans.modules.print.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.24
 */
public final class PrintAction extends IconAction {

    public PrintAction() {
        this("MNU_Print_Action", "TLT_Print_Action", null, false, null); // NOI18N
    }

    public PrintAction(PrintProvider[] providers) {
        this(null, "ACT_Print_Action", "print", true, providers); // NOI18N
    }

    public PrintAction(JComponent component) {
        this((PrintProvider[]) null);
        myProviders = getComponentProviders(component);
    }

    private PrintAction(String name, String toolTip, String icon, boolean enabled, PrintProvider[] providers) {
        super(i18n(PrintAction.class, name), i18n(PrintAction.class, toolTip), icon(Config.class, icon));
        setEnabled(enabled);
        myEnabled = enabled;
        myProviders = providers;
    }

    public void actionPerformed(ActionEvent event) {
        if (myProviders == null) {
            myProviders = getPrintProviders();
        }
        if (myProviders != null) {
            Preview.getDefault().print(myProviders, true);

            if ( !myEnabled) {
                myProviders = null;
            }
        }
        else {
            PrintCookie cookie = getPrintCookie();

            if (cookie != null) {
                cookie.print();
            }
        }
    }

    private PrintProvider[] getPrintProviders() {
//out();
//out("GET PRINT PROVIDERS");
        TopComponent top = getActiveTopComponent();
//out("Top: " + top);

        PrintProvider[] providers = getComponentProviders(top);
//out("Component providers: " + providers);

        if (providers != null) {
            return providers;
        }
        providers = getLookupProviders(top);
//out("Lookup providers: " + providers);

        if (providers != null) {
            return providers;
        }
        return getCookieProviders(getSelectedNodes());
    }

    private PrintProvider[] getLookupProviders(TopComponent top) {
        if (top == null) {
            return null;
        }
        return getProviders((PrintProvider) top.getLookup().lookup(PrintProvider.class));
    }

    private PrintProvider[] getComponentProviders(JComponent top) {
        if (top == null) {
            return null;
        }
        List<JComponent> printable = new ArrayList<JComponent>();
        findPrintable(top, printable);

        if (printable.size() == 0) {
            return null;
        }
        return getProviders(new ComponentProvider(printable, getName(printable, top), getDate(top)));
    }

    private PrintProvider[] getCookieProviders(Node[] nodes) {
//out();
//out("get cookie provider");
        if (nodes == null) {
//out("NODES NULL");
            return null;
        }
        List<PrintProvider> providers = new ArrayList<PrintProvider>();

        for (Node node : nodes) {
//out("  see: " + node);
            PrintProvider provider = getCookieProvider(node);

            if (provider != null) {
                providers.add(provider);
            }
        }
        if (providers.size() == 0) {
//out("result null");
            return null;
        }
//out("result: " + providers);
        return providers.toArray(new PrintProvider[0]);
    }

    private PrintProvider getCookieProvider(Node node) {
//out();
//out("GET input stream provider: " + node);
        String text = getText(node.getLookup().lookup(InputStream.class));

        if (text != null) {
//out("text: " + text);
            return new TextProvider(text);
        }
//out("get editor provider");
        EditorCookie editor = node.getLookup().lookup(EditorCookie.class);

        if (editor == null) {
//out("get editor provider.2");
            return null;
        }
        if (editor.getDocument() == null) {
//out("get editor provider.3");
            return null;
        }
        return new EditorProvider(editor, getDate(getDataObject(node)));
    }

    private void findPrintable(Container container, List<JComponent> printable) {
        if (container.isShowing() && isPrintable(container)) {
            printable.add((JComponent) container);
        }
        Component[] children = container.getComponents();

        for (Component child : children) {
            if (child instanceof Container) {
                findPrintable((Container) child, printable);
            }
        }
    }

    private PrintProvider[] getProviders(PrintProvider provider) {
        if (provider == null) {
            return null;
        }
        return new PrintProvider[] { provider };
    }

    private boolean isPrintable(Container container) {
        return container instanceof JComponent && ((JComponent) container).getClientProperty(PrintManager.PRINT_PRINTABLE) == Boolean.TRUE;
    }

    private String getName(List<JComponent> printable, JComponent top) {
        for (JComponent component : printable) {
            Object object = component.getClientProperty(PrintManager.PRINT_NAME);

            if (object instanceof String) {
                return (String) object;
            }
        }
        return getName(getData(top));
    }

    private String getName(DataObject data) {
        if (data == null) {
            return null;
        }
        return data.getName();
    }

    private Date getDate(JComponent top) {
        return getDate(getData(top));
    }

    private Date getDate(DataObject data) {
        if (data == null) {
            return null;
        }
        return data.getPrimaryFile().lastModified();
    }

    private DataObject getData(JComponent top) {
        if ( !(top instanceof TopComponent)) {
            return null;
        }
        return (DataObject) ((TopComponent) top).getLookup().lookup(DataObject.class);
    }

    private String getText(InputStream input) {
        if (input == null) {
            return null;
        }
        try {
            input.reset();
            int length = input.available();
            byte[] bytes = new byte[length];
            input.read(bytes);
            input.close();
            return new String(bytes);
        }
        catch (IOException e) {
            return null;
        }
    }

    private PrintCookie getPrintCookie() {
        Node node = getSelectedNode();

        if (node == null) {
            return null;
        }
        return (PrintCookie) node.getCookie(PrintCookie.class);
    }

    @Override
    public boolean isEnabled() {
        if (myEnabled) {
            return true;
        }
//out("IS ENABLED: " + (getPrintProviders() != null || getPrintCookie() != null));
//out("          : " + getPrintProviders());
        return getPrintProviders() != null || getPrintCookie() != null;
    }

    private boolean myEnabled;
    private PrintProvider[] myProviders;
}
