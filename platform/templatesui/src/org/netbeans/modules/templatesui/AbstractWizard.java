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
package org.netbeans.modules.templatesui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.java.html.BrwsrCtx;
import net.java.html.js.JavaScriptBody;
import net.java.html.json.Model;
import net.java.html.json.Models;
import netscape.javascript.JSObject;
import org.netbeans.api.htmlui.HTMLDialog.Builder;
import org.netbeans.api.templates.FileBuilder;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 */
@Model(className = "InitWizard", targetId = "", properties = {
})
abstract class AbstractWizard 
implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {
    private static final Logger LOG = Logger.getLogger(AbstractWizard.class.getName());
    
    private int index;
    private List<String> steps = Collections.emptyList();
    private List<String> stepNames = Collections.emptyList();
    private String current;
    private Object data;
    private Object ref;
    private JComponent p;
    private BrwsrCtx ctx;
    private ChangeListener listener;
    private int errorCode = 0;
    private WizardDescriptor wizard;
    
    protected abstract Object initSequence(ClassLoader l) throws Exception;
    protected abstract URL initPage(ClassLoader l);
    protected abstract void initializationDone(Throwable error);
    protected abstract String[] getTechIds();

    @Override
    public Set<? extends Object> instantiate() throws IOException {
        try {
            final TemplateWizard tw = (TemplateWizard) wizard;
            FutureTask<Map<String,Object>> t = new FutureTask<>(new Callable<Map<String,Object>>() {
                @Override
                public Map<String,Object> call() throws Exception {
                    Object[] namesAndValues = rawProps(data);
                    Map<String,Object> map = new TreeMap<>();
                    for (int i = 0; i < namesAndValues.length; i += 2) {
                        String name = (String) namesAndValues[i];
                        Object value = namesAndValues[i + 1];
                        map.put(name, value);
                    }
                    return map;
                }
            });
            if (ctx != null) {
                ctx.execute(t);
            }
            Map<String, Object> params = new HashMap<>();
            
            for (Map.Entry<String, Object> entry : tw.getProperties().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                params.put(key, value);
            }
            if (ctx != null) {
                params.put("wizard", t.get()); // NOI18N
            }
            
            List<FileObject> result = new FileBuilder(
                tw.getTemplate().getPrimaryFile(),
                tw.getTargetFolder().getPrimaryFile()
            ).
            name(tw.getTargetName()).
            withParameters(params).build();
                    
            Set<DataObject> objs = new LinkedHashSet<>(result.size() * 2);
            for (FileObject fileObject : result) {
                objs.add(DataObject.find(fileObject));
            }
            return objs;
        } catch (Exception ex) {
            throw (IOException)new InterruptedIOException().initCause(ex);
        }
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        this.wizard = null;
    }

    
    private List<? extends WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        fillPanels((TemplateWizard)wizard, this, panels, steps);
        return Collections.unmodifiableList(panels);
    }
    
    static void fillPanels(
        TemplateWizard wizard, AbstractWizard aw,
        List<WizardDescriptor.Panel<WizardDescriptor>> panels, List<String> steps
    ) {
        int cnt = steps.size();
        if (cnt == 0) {
            cnt = 1;
        }
        for (int i = 0; i < cnt; i++) {
            if (steps.size() > i) {
                final String panelName = steps.get(i);
                if ("targetChooser".equals(panelName)) { // NOI18N
                    panels.add(wizard.targetChooser());
                    continue;
                }
                final String tcPrefix = "targetChooser:"; // NOI18N
                if (panelName != null && panelName.startsWith(tcPrefix)) {
                    WizardDescriptor.Panel<WizardDescriptor> panel = aw.getChooser(wizard, panelName.substring(tcPrefix.length()));
                    panels.add(panel);
                    continue;
                }
            }
            final HTMLPanel p = new HTMLPanel(i, aw);
            panels.add(p);
        }
    }
    
    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        WizardDescriptor.Panel<WizardDescriptor> ret = getPanels().get(index);
        if (ret.getComponent() != p) {
            if (ret.getComponent() instanceof JComponent) {
                JComponent update = (JComponent) ret.getComponent();
                update.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA,
                    p.getClientProperty(WizardDescriptor.PROP_CONTENT_DATA)
                );
                update.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,
                    p.getClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX)
                );
            }
        }
        return ret;
    }

    @NbBundle.Messages({
        "# {0} - current index",
        "# {1} - number of panels",
        "MSG_HTMLWizardName={0} of {1}"
    })
    @Override
    public String name() {
        return Bundle.MSG_HTMLWizardName(index + 1, getPanels().size());
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
        onStepsChange(null);
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
        onStepsChange(null);
    }

    @Override
    public synchronized void addChangeListener(ChangeListener l) {
        assert this.listener == null;
        this.listener = l;
    }

    @Override
    public synchronized void removeChangeListener(ChangeListener l) {
        if (this.listener == l) {
            this.listener = null;
        }
    }
    
    final void fireChange() {
        final ChangeListener l;
        synchronized (this) {
            l = this.listener;
            notifyAll();
        }
        if (l != null) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    l.stateChanged(new ChangeEvent(this));
                }
            });
        }
    }

    final JComponent component(final int index) {
        if (p == null) {
            ClassLoader tmpL = Lookup.getDefault().lookup(ClassLoader.class);
            if (tmpL == null) {
                tmpL = Thread.currentThread().getContextClassLoader();
            }
            if (tmpL == null) {
                tmpL = HTMLPanel.class.getClassLoader();
            }

            final ClassLoader l = tmpL;

            URL u = initPage(l);
            p = Builder.newDialog(u.toExternalForm()).
                addTechIds(getTechIds()).
                loadFinished(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ctx = BrwsrCtx.findDefault(HTMLPanel.class);
                            Models.toRaw(new InitWizard());
                            Object ret = initSequence(l);

                            if (ret instanceof String) {
                                data = eval((String) ret);
                                if (data == null || "undefined".equals(data)) {
                                    throw new IllegalArgumentException("Executing " + ret + " returned null, that is wrong, should get JSON object with ko bindings");
                                }
                            } else {
                                if (ret != null && Models.isModel(ret.getClass())) {
                                    data = Models.toRaw(ret);
                                    ref = ret;
                                } else {
                                    throw new IllegalStateException("Returned value should be string or class generated by @Model annotation: " + ret);
                                }
                            }
                            registerStepHandler(data);

                            boolean stepsOK = listenOnProp(data, AbstractWizard.this, "steps");
                            boolean errorCodeOK = listenOnProp(data, AbstractWizard.this, "errorCode");

                            applyBindings(data);
                            initializationDone(null);
                        } catch (Exception ex) {
                            initializationDone(ex);
                        } catch (Error ex) {
                            initializationDone(ex);
                        }
                    }
                }).component(javax.swing.JComponent.class);
            p.setPreferredSize(new Dimension(500, 340));
            p.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
            p.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
            p.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
        }
        if (index < stepNames.size()) {
            p.setName(stepNames.get(index));
        }
        return p;
    }
    
    
    final void onChange(String prop, Object data) {
        if ("steps".equals(prop)) {
            onStepsChange((Object[])data);
        }
        if ("errorCode".equals(prop)) {
            errorCode = data instanceof Number ? ((Number)data).intValue() : -1;
            fireChange();
        }
    }

    boolean isValid() {
        return errorCode == 0;
    }
    
    final Object executeScript(final String code) throws InterruptedException, ExecutionException {
        FutureTask<?> t = new FutureTask<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return eval(code);
            }
        });
        ctx.execute(t);
        Object ret = t.get();
        return ret;
    }
    final Object evaluateCall(final Object fn, final Object p) throws InterruptedException, ExecutionException {
        FutureTask<?> t = new FutureTask<Object>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                JSObject jsRegFn = (JSObject) fn;
                return jsRegFn.call("call", null, p);
            }
        });
        ctx.execute(t);
        return t.get();
    }
    final Object evaluateProp(final String prop) throws InterruptedException, ExecutionException {
        FutureTask<?> t = new FutureTask<Object>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return getPropertyValue(data, prop);
            }
        });
        ctx.execute(t);
        return t.get();
    }
    
    final void setProp(final String prop, final Object value) throws InterruptedException, ExecutionException {
        FutureTask<?> t = new FutureTask<Object>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return changeProperty(data, prop, value);
            }
        });
        ctx.execute(t);
        t.get();
    }
    
    final Object data() {
        return data;
    }

    final Reference<?> ref() {
        return new WeakReference<Object>(ref);
    }
    
    final String[] steps(boolean localized) {
        return (localized ? stepNames : steps).toArray(new String[0]);
    }
    
    final String currentStep() {
        return current;
    }
    
   @NbBundle.Messages({
        "LBL_TemplatesPanel_Name=Choose File Type",
        "LBL_TargetPanel_Name=Name and Location"
    })
   private void onStepsChange(Object[] obj) {
        if (obj != null) {
            List<String> arr = new ArrayList<>();
            for (Object s : obj) {
                arr.add(stringOrId(s, "id", null)); // NOI18N
            }
            if (!arr.equals(steps)) {
                steps = arr;
                fireChange();
            }
            final List<String> names = new ArrayList<>();
            for (Object s : obj) {
                String id = stringOrId(s, "text", "id"); // NOI18N
                if (id != null && id.equals("targetChooser") || id.startsWith("targetChooser:")) { // NOI18N
                    id = Bundle.LBL_TargetPanel_Name();
                }
                names.add(id);
            }
            stepNames = new ArrayList<>(names);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    p.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, names.toArray(new String[0]));
                }
            });
            fireChange();
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                p.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index);
            }
        });
        if (steps != null && steps.size() > index) {
            current = steps.get(index);
            ctx.execute(new Runnable() {
                @Override
                public void run() {
                    changeProperty(data, "current", current); // NOI18N
                }
            });
        }
    }

    boolean validationRequested;
    boolean prepareValidation() {
        FutureTask<Boolean> t = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return validationRequested = callValidate(data);
            }
        });
        ctx.execute(t);
        try {
            return t.get();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    synchronized void waitForValidation() throws WizardValidationException {
        if (!validationRequested) {
            return;
        }
        while (errorCode == -1) {
            try {
                wait();
            } catch (InterruptedException ex) {
                LOG.log(Level.INFO, null, ex);
            }
        }
        if (errorCode != 0) {
            throw new WizardValidationException(p, null, null);
        }
    }

    @JavaScriptBody(args = "code", body = "return 0 || eval(code);")
    static native Object eval(String code);
    
    @JavaScriptBody(args = { "arr" }, body = 
        "for (var i = 0; i < arr.length; i++) {\n" +
        "  arr[i]();\n" +
        "}\n" +
        ""
    )
    native void invokeFn(Object[] arr);
    
    @JavaScriptBody(args = { "raw" }, body = 
        "if (raw.errorCode() !== -1) return false;" +
        "if (raw.validate) {" +
        "  raw.validate();" +
        "  return true;" +
        "}" +
        "return false;"
    )
    static native boolean callValidate(Object raw);
   
    @JavaScriptBody(args = {"data", "onChange", "p" }, 
        javacall = true, body = ""
        + "if (typeof data[p] !== 'function') {\n"
        + "  throw 'Type of property ' + p + ' should be a function!';\n"
        + "}\n"
        + "data[p].subscribe(function(value) {\n"
        + "  onChange.@org.netbeans.modules.templatesui.AbstractWizard::onChange(Ljava/lang/String;Ljava/lang/Object;)(p, value);\n"
        + "});\n"
        + "onChange.@org.netbeans.modules.templatesui.AbstractWizard::onChange(Ljava/lang/String;Ljava/lang/Object;)(p, data[p]());\n"
        + "return true;\n"
    )
    static native boolean listenOnProp(
        Object raw, AbstractWizard onChange, String propName
    );
    
    @JavaScriptBody(args = { "raw", "propName", "value" }, body = ""
        + "var fn = raw[propName];\n"
        + "if (typeof fn !== 'function') return false;\n"
        + "fn(value);\n"
        + "return true;\n"
    )
    private static native boolean changeProperty(Object raw, String propName, Object value);

    @JavaScriptBody(args = { "fn", "arr" }, body = ""
        + "return fn.apply(null, arr);"
    )
    private static native Object callFn(Object fn, Object[] arr);
    
    @JavaScriptBody(args = { "raw", "propName" }, body = ""
        + "var fn = raw[propName];\n"
        + "if (typeof fn !== 'function') return null;\n"
        + "return fn();\n"
    )
    static native Object getPropertyValue(Object raw, String propName);
    
    @JavaScriptBody(args = { "raw" }, body = ""
        + "var ret = [];\n"
        + "for (var n in raw) {\n"
        + "  if (n === 'current') continue;\n"
        + "  if (n === 'errorCode') continue;\n"
        + "  if (n === 'steps') continue;\n"
        + "  var fn = raw[n];\n"
        + "  ret.push(n);\n"
        + "  if (typeof fn === 'function') ret.push(fn()); else ret.push(fn);\n"
        + "}\n"
        + "return ret;\n"
    )
    static native Object[] rawProps(Object raw);
    
    @JavaScriptBody(args = { "obj", "id", "fallback" }, body = 
        "if (typeof obj === 'string') return obj;\n"
      + "if (obj[id]) return obj[id].toString();\n"
      + "if (fallback && obj[fallback]) return obj[fallback].toString();\n"
      + "return null;\n"
    )
    static native String stringOrId(Object obj, String id, String fallback);
    
    @JavaScriptBody(args = { "raw" }, body = ""
        + "var current = raw.current || (raw.current = ko.observable());\n"
        + "var steps = raw.steps || (raw.steps = ko.observableArray());\n"
        + "if (!raw.errorCode) raw.errorCode = ko.computed(function() {\n"
        + "  return 1;\n"
        + "});\n"
        + "ko.bindingHandlers.step = {\n"
        + "  init : function(element, valueAccessor, allBindings, viewModel, bindingContext) {\n"
        + "    steps.push(valueAccessor());\n"
        + "  },\n"
        + "  update : function(element, valueAccessor, allBindings, viewModel, bindingContext) {\n"
        + "    var v = valueAccessor();\n"
        + "    if (typeof v !== 'string') v = v.id;\n"
        + "    if (current() === v) {\n"
        + "      element.style.display = '';\n"
        + "    } else {\n"
        + "      element.style.display = 'none';\n"
        + "    }\n;\n"
        + "  }\n"
        + "};\n"
    )
    static native void registerStepHandler(Object raw);
    
    @JavaScriptBody(args = { "raw" }, body = "ko.applyBindings(raw);")
    static native void applyBindings(Object raw);

    Map<String,WizardDescriptor.Panel<WizardDescriptor>> choosers;
    WizardDescriptor.Panel<WizardDescriptor> getChooser(TemplateWizard wizard, String type) {
        if (choosers == null) {
            choosers = new HashMap<>();
        }
        WizardDescriptor.Panel<WizardDescriptor> panel = choosers.get(type);
        
        if (panel == null) {
            panel = loadPanel(type, wizard);
            choosers.put(type, panel);
        }
        return panel;
    }

    private static WizardDescriptor.Panel<WizardDescriptor> loadPanel(String type, TemplateWizard tw) {
        WizardDescriptor.Panel<WizardDescriptor> panel;
        try {
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = Thread.currentThread().getContextClassLoader();
            }
            if (l == null) {
                l = AbstractWizard.class.getClassLoader();
            }
            Method create;
            if ("archetype".equals(type)) { // NOI18N
                Class<?> clazz = Class.forName("org.netbeans.modules.maven.api.archetype.ArchetypeWizards", true, l); // NOI18N
                create = clazz.getDeclaredMethod("basicWizardPanel", Object.class, String.class); // NOI18N
            } else {
                Class<?> clazz = Class.forName("org.netbeans.spi.java.project.support.ui.templates.JavaTemplates", true, l); // NOI18N
                create = clazz.getDeclaredMethod("createPackageChooser", Object.class, String.class); // NOI18N
            }
            create.setAccessible(true);
            panel = (WizardDescriptor.Panel<WizardDescriptor>) create.invoke(null, tw.getProperty("project"), type); // NOI18N      
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Cannot create targetChooser for type " + type + " using default. "
                    + "Don't forget to include org.netbeans.modules.java.project.ui module in your application.", t
            );
            panel = tw.targetChooser();
        }
        return panel;
    }
}
