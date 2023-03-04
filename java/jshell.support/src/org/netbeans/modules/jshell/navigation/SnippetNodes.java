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
package org.netbeans.modules.jshell.navigation;

import java.awt.Color;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import jdk.jshell.JShell;
import jdk.jshell.MethodSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.VarSnippet;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.env.ShellEvent;
import org.netbeans.modules.jshell.env.ShellListener;
import org.netbeans.modules.jshell.model.SnippetHandle;
import org.netbeans.modules.jshell.parsing.SnippetRegistry;
import org.netbeans.modules.jshell.support.ShellSession;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sdedic
 */
public class SnippetNodes extends Children.Keys implements ShellListener, Consumer<SnippetEvent> {
    // keep in sync with java navigator
    /**
     * UIMnager key for the color. Color is shared with Java navigator.
     */
    static final String TYPE_COLOR_KEY = "nb.navigator.type.color";    //NOI18N
    static final String HIDDEN_COLOR_KEY = "nb.navigator.inherited.color";   //NOI18N
    
    /**
     * Color for data types - method parameters or field type.
     */
    static final Color DEFAULT_TYPE_COLOR = new Color(0x70,0x70,0x70); //NOI18N
    static final Color DEFAULT_HIDDEN_COLOR = new Color(0x7D,0x69, 0x4A);    //NOI18N

    /**
     * Special group key for imports
     */
    public static final Object  KEY_IMPORTS = new String("imports"); // NOI18N
    
    /**
     * Special key for the input section, not really represented by a SnippetHandle.
     */
    public static final Object  KEY_INPUT = new String("input"); // NOI18N
    
    /**
     * The shell environment being displayed
     */
    private final JShellEnvironment   env;
    
    /**
     * The active session
     */
    private volatile ShellSession     session;
    
    /**
     * 
     */
    private Color                     typeColor;
    private Color                     hiddenColor;

    private final NF                  factory;
    
    private static final java.util.Map<JShellEnvironment, NF> env2Factories = new HashMap<>();
    
    @NonNull
    private static String getHtmlColor(@NullAllowed final Color _c, @NonNull final Color defaultColor) {
        Color c = _c == null ? defaultColor : _c;
        final int r = c.getRed();
        final int g = c.getGreen();
        final int b = c.getBlue();
        final StringBuilder result = new StringBuilder();
        result.append ("#");        //NOI18N
        final String rs = Integer.toHexString (r);
        final String gs = Integer.toHexString (g);
        final String bs = Integer.toHexString (b);
        if (r < 0x10)
            result.append('0');
        result.append(rs);
        if (g < 0x10)
            result.append ('0');
        result.append(gs);
        if (b < 0x10)
            result.append ('0');
        result.append(bs);
        return result.toString();
    }

    public SnippetNodes(JShellEnvironment env) {
        this.env = env;
        /*
        synchronized(SnippetNodes.class) {
            NF f = env2Factories.get(env);
            if (f == null) {
                f = new NF();
                env2Factories.put(env, f);
            }
            this.factory = f;
        }
        */
        this.factory = new NF();
        this.env.addShellListener(WeakListeners.create(ShellListener.class, this, env));
        update();
        try {
            attachTo(env.getShell());
        } catch (IllegalStateException ex) {
            // jshell may have been closed in the meantime
        }
        
        typeColor = UIManager.getColor(TYPE_COLOR_KEY);
        hiddenColor = UIManager.getColor(HIDDEN_COLOR_KEY);
    }
    
    private void update() {
        synchronized (this) {
            ShellSession session = env.getSession();
            if (session == null || session.getShell() == null) {
                return;
            }
            this.session = session;
        }
        SnippetRegistry reg = session.getSnippetRegistry();
        Collection<Snippet> snippets = session.getSnippetRegistry().getSnippets();
        Collection<SnippetHandle>   imports = new ArrayList<>();
        Collection<SnippetHandle>   keys = new ArrayList<>();
        for (Snippet s : snippets) {
            Snippet.Kind k = s.kind();
            SnippetHandle hdl = reg.getHandle(s);
            if (hdl == null) {
                continue;
            }
            switch (k) {
                case IMPORT:
                    imports.add(hdl);
                    continue;
                case TYPE_DECL:
                case METHOD:
                case VAR:
                    break;
                case STATEMENT:
                case EXPRESSION:
                case ERRONEOUS:
                    // ignore
                    continue;
                default:
                    throw new AssertionError(k.name());
                
            }
            keys.add(hdl);
        }
        setKeys(keys);
    }
    
    @Override
    protected Node[] createNodes(Object key) {
        if (key instanceof SnippetHandle) {
            return new Node[] { 
                createNode((SnippetHandle)key)
            };
        } else {
            return null;
        }
    }
    
    private N createNode(SnippetHandle k) {
        N n = new N(env, k);
        factory.register(k, n);
        return n;
    }
    
    @Override
    public void shellCreated(ShellEvent ev) {
        update();
    }
    
    private JShell state;
    private NR sub;
    
    private synchronized void attachTo(JShell shell) {
        if (shell == state) {
            return;
        }
        if (state != null) {
            sub.unsubscribe();
            state = null;
        }
        if (shell != null) {
            NR subscription = new NR(this, shell);
            subscription.subscribe();
            // may fail in JShell, record in member variables only after successful
            // registration
            state = shell;
            sub = subscription;
        }
    }

    @Override
    public void shellStarted(ShellEvent ev) {
        synchronized (this) {
            ShellSession s = ev.getSession();
            if (s == null) {
                return;
            }
        }
        update();
        try {
            attachTo(ev.getEngine());
        } catch (IllegalStateException ex) {
            // the shell may have terminated in between the event was generated and now. Ignore.
        }
        refreshNodeNames();
    }

    @Override
    public void accept(SnippetEvent t) {
        Snippet snip = t.snippet();
        Snippet.Status stat = t.status();
        if (session == null) {
            return;
        }
        if (stat == Snippet.Status.DROPPED ||
            stat == Snippet.Status.OVERWRITTEN) {
            SnippetHandle h = session.getSnippetRegistry().getHandle(snip);
            if (h != null) {
                N n = factory.findNode(h);
                if (n != null) {
                    n.fireDisplayNameChange();
                }
            }
        }
    }   
    
    public Node getNodeFor(SnippetHandle h) {
        Node[] keeAlive = getNodes(); // force initialization
        return factory.findNode(h);
    }
    
    private void refreshNodeNames() {
        Collection<N> nodes;
        
        for (N n : factory.getNodes()) {
            n.fireDisplayNameChange();
        }
    }

    @Override
    public void shellStatusChanged(ShellEvent ev) {
        update();
    }

    @Override
    public void shellShutdown(ShellEvent ev) {
        synchronized (SnippetNodes.class) {
            JShellEnvironment e = ev.getEnvironment();
            if (e != null) {
                env2Factories.remove(e);
            }
        }
    }
    
    @Override
    public void shellSettingsChanged(ShellEvent ev) {}
    
    private static final DropAction dropAction = new DropAction();
 
    @NbBundle.Messages({
        "# {0} - html node name/description",
        "NodeName_Obsoleted=<s>{0}</s>"
    })
    class N extends AbstractNode {
        private final JShellEnvironment env;
        private final ElementKind       nodeKind;
        private final SnippetHandle     snipHandle;
        private ImageIcon       icon;
        private String htmlDisplayName;

        public N(JShellEnvironment env, SnippetHandle handle) {
            super(Children.LEAF, 
                    Lookups.fixed(
                            env, 
                            handle, 
                            env, 
                            env.getConsoleFile()
                    )
            );
            this.snipHandle = handle;
            this.env = env;
            
            Snippet.Kind k = handle.getKind();
            switch (k) {
                case TYPE_DECL:
                    nodeKind = ElementKind.CLASS;
                    break;
                case METHOD:
                    nodeKind = ElementKind.METHOD;
                    break;
                case VAR:
                    nodeKind = ElementKind.FIELD;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            
            Snippet s = handle.getSnippet();
            String dispName = null;
            
            switch (s.kind()) {
                case TYPE_DECL:
                    dispName = handle.text();
                    break;
                case METHOD: {
                    MethodSnippet m = (MethodSnippet)s;
                    dispName = m.name() + "(" + m.parameterTypes() + ")"; // NOI18N
                    htmlDisplayName = m.name() + "(<font color=\"" +
                            getHtmlColor(typeColor, DEFAULT_TYPE_COLOR) + "\">"+ 
                            m.parameterTypes() + 
                            "</font>)";
                    break;
                }
                case VAR: {
                    VarSnippet v = (VarSnippet)s;
                    dispName = v.name() + " : " + v.typeName(); // NOI18N
                    htmlDisplayName = v.name() + " : <font color=\"" +
                            getHtmlColor(typeColor, DEFAULT_TYPE_COLOR) + "\">"+ 
                            v.typeName() +
                            "</font>";
                    dispName = handle.text();
                    break;
                }
                default:
                    throw new AssertionError(s.kind().name());
            }
            if (htmlDisplayName == null) {
                htmlDisplayName = dispName;
            }
            setDisplayName(dispName);
        }

        @Override
        public Action getPreferredAction() {
            return getOpenAction();
        }
        
        public Action[] getActions( boolean context ) {
            return new Action[] { 
                getOpenAction(),
                null,
                dropAction
            };
        }
        
        public void fireDisplayNameChange() {
            super.fireDisplayNameChange(null, null);
        }
        
        private boolean hasText() {
            return snipHandle.getSection() != null;
        }
        
        private OpenAction openAction;
        
        private OpenAction getOpenAction() {
            if (openAction == null) {
                openAction = new OpenAction(OpenAction.createOpener(env, snipHandle));
            }
            return openAction;
        }

        @Override
        public String getHtmlDisplayName() {
            String s = htmlDisplayName;
            boolean obsolete = snipHandle.getState() != env.getShell();
            
            switch (snipHandle.getStatus()) {
                case DROPPED:
                case OVERWRITTEN:
                case REJECTED:
                    obsolete = true;
                    break;
                default:
                    break;
            }
            if (!hasText()) {
                // change all the display name to inherited color:
                s = "<font color=\"" + getHtmlColor(hiddenColor, DEFAULT_HIDDEN_COLOR) + "\">" +
                        super.getDisplayName() + "</font>";
            }
            if (obsolete) {
                s = "<s>" + 
                    s
                        + "</s>";
            }
            return s;
        }

        @Override
        public Image getIcon(int type) {
            if (nodeKind == null) {
                return super.getIcon(type);
            }
            
            return ImageUtilities.icon2Image(
                    ElementIcons.getElementIcon(nodeKind, EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
            );
        }

        @Override
        public boolean canCut() {
            return false;
        }

        @Override
        public boolean canCopy() {
            return false;
        }

        @Override
        public Transferable clipboardCut() throws IOException {
            return null;
        }

        @Override
        public Transferable clipboardCopy() throws IOException {
            return null;
        }

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public boolean canRename() {
            return false;
        }
    }
    
    static class NF {
        private Set<N>                    nodes = new WeakSet<>();
        private java.util.Map<SnippetHandle, Reference<N>> handledNodes = new WeakHashMap<>();
        
        public void register(SnippetHandle h, N n) {
            synchronized (this) {
                nodes.add(n);
                handledNodes.put(h, new WeakReference<>(n));
            }
        }
        
        public N findNode(SnippetHandle h) {
            N n;
            synchronized (this) {
                Reference<N> rN = handledNodes.get(h);
                if (rN == null) {
                    return null;
                }
                n = rN.get();
                if (n == null) {
                    handledNodes.remove(h);
                    return null;
                }
                return n;
            }
        }
        
        public List<N> getNodes() {
            synchronized (this) {
                return new ArrayList<>(this.nodes);
            }
        }
    }
    
    static class NR extends WeakReference<SnippetNodes> implements Runnable, Consumer<SnippetEvent> {
        private JShell.Subscription sub;
        private final Reference<JShell> shellRef;

        public NR(SnippetNodes referent, JShell shell) {
            super(referent, Utilities.activeReferenceQueue());
            this.sub = sub;
            this.shellRef = new WeakReference<>(shell);
        }
        
        void subscribe() {
            shellRef.get().onSnippetEvent(this);
        }
        
        @Override
        public void run() {
            unsubscribe();
        }

        synchronized void unsubscribe() {
            JShell s = shellRef.get();
            if (sub == null || s == null) {
                return;
            }
            s.unsubscribe(sub);
        }

        @Override
        public void accept(SnippetEvent t) {
            SnippetNodes n = get();
            if (n == null) {
                unsubscribe();
            } else {
                n.accept(t);
            }
        }
    }
}
