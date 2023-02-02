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
package org.netbeans.modules.maven.hints.pom.props;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.hints.pom.PomModelUtils;
import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.netbeans.modules.maven.hints.pom.spi.SelectionPOMFixProvider;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import static org.netbeans.modules.maven.hints.pom.props.Bundle.*;
/**
 *
 * @author mkleint
 */
public class TurnToPropertyHint implements SelectionPOMFixProvider {
    private static final Logger LOG = Logger.getLogger(TurnToPropertyHint.class.getName());

    private Configuration configuration;
    @NbBundle.Messages({
        "TIT_TurnToPropertyHint=Turn selected text to property", 
        "DESC_TurnToPropertyHint=Selected text is turned into an expression and new property is defined. Existing properties containing the selected value are also offered."})
    public TurnToPropertyHint() {
        configuration = new Configuration("TurnToPropertyHint", //NOI18N
                TIT_TurnToPropertyHint(),
                DESC_TurnToPropertyHint(),
                true, Configuration.HintSeverity.WARNING);
    }

    @Override
    public List<ErrorDescription> getErrorsForDocument(POMModel model, Project prj,
            int selectionStart, int selectionEnd, int caretPosition) {
        List<ErrorDescription> err = new ArrayList<ErrorDescription>();
        if (prj == null) {
            return err;
        }
        DocumentComponent comp1 = model.findComponent(selectionStart);
        DocumentComponent comp2 = model.findComponent(selectionEnd);
        if (comp1 == null || comp2 == null) { //#157213
            return err;
        }
        if (comp1 == comp2 && comp1 instanceof POMExtensibilityElement) {
            POMExtensibilityElement el = (POMExtensibilityElement) comp1;
            int startPos = el.findPosition();
            startPos = startPos + el.getQName().getLocalPart().length() + 2; //2 is brackets
            String text = el.getElementText();
            int endPos = startPos + text.length();
            if (selectionStart >= startPos && selectionEnd <= endPos) {
                //we are in actual text now..
                //TODO also skip when inside expression as well..
                int offset = selectionStart - startPos;
                int endOffset = selectionEnd - startPos;
                String s = text.substring(offset, endOffset);
                if (s.length() > 0) {
                    List<Fix> fixes = new ArrayList<Fix>();
                    String elementName = el.getQName().getLocalPart();
                    Map<String, String> props = loadAllProperties(prj, model, el, selectionStart);
                    for (Map.Entry<String, String> ent : props.entrySet()) {
                        if (s.equals(ent.getValue()) && !elementName.equals(ent.getKey())) { //do not want to complete the cycle
                            fixes.add(new PropFix(text, offset, endOffset, el, model, ent.getKey()));
                        }
                    }
                    fixes.add(new PropFix(text, offset, endOffset, el, model));
                    try {
                        Line line = NbEditorUtilities.getLine(model.getBaseDocument(), selectionEnd, false);
                        err.add(ErrorDescriptionFactory.createErrorDescription(
                                Severity.HINT,
                                TIT_TurnToPropertyHint(),
                                fixes,
                                model.getBaseDocument(), line.getLineNumber() + 1));
                    } catch (IndexOutOfBoundsException iiob) {
                        //#214527
                        LOG.log(Level.FINE, "document changed", iiob);
                    }
                    
                }
            }
        }
        return err;
    }

    @Override
    public JComponent getCustomizer(Preferences preferences) {
        return null;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private Map<String, String> loadAllProperties(Project prj, POMModel mdl, POMExtensibilityElement el, int position) {
        Map<String, String> props = new HashMap<>();
        NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
        if (nbprj != null) {
            MavenProject mp = nbprj.getMavenProject();
            Properties p = mp.getProperties();
            if (p != null) {
                for (Map.Entry ent : p.entrySet()) {
                    if (ent.getKey() != null && ent.getValue() != null) {
                        props.put(ent.getKey().toString(), ent.getValue().toString());
                    }
                }
            }
            props.put("project.groupId", mp.getGroupId());
            props.put("project.artifactId", mp.getArtifactId());
            props.put("project.version", mp.getVersion());
            
        }
        //all from current profile as well..
        Profile prof = mdl.findComponent(position, Profile.class, true);
        if (prof != null) {
            org.netbeans.modules.maven.model.pom.Properties p = prof.getProperties();
            if (p != null) {
                Map<String, String> mdlprops = p.getProperties();
                if (mdlprops != null) {
                    props.putAll(mdlprops);
                }
            }
        }
        org.netbeans.modules.maven.model.pom.Properties pp = mdl.getProject().getProperties();
        if (pp != null) {
            Map<String, String> mdlprops = pp.getProperties();
            if (mdlprops != null) {
                props.putAll(mdlprops);
            }
        }
        return props;
    }

    private static class PropFix implements Fix {
        private POMModel mdl;
        private final int start;
        private final int end;
        private String key;
        private final String old;
        private final POMExtensibilityElement element;

        PropFix(String oldValue, int offset, int endOffset, POMExtensibilityElement el, POMModel model) {
            this(oldValue, offset, endOffset, el, model, null);
        }

        private PropFix(String oldValue, int offset, int endOffset, POMExtensibilityElement el, POMModel model, String key) {
            mdl = model;
            this.old = oldValue;
            start = offset;
            end = endOffset;
            this.key = key;
            element = el;
        }

        @Override
        @NbBundle.Messages({
            "# {0} - property name",
            "TXT_Replace=Replace by expression {0}", 
            "TXT_Create=Create new property"})
        public String getText() {
            return key != null ? TXT_Replace("${" + key + "}") : TXT_Create();
        }

        @Override
        @NbBundle.Messages({
            "TXT_Enter=Enter new property name:", 
            "TIT_Enter=Create new property"})
        public ChangeInfo implement() throws Exception {

            ChangeInfo info = new ChangeInfo();
            if (!mdl.getState().equals(Model.State.VALID)) {
                return info;
            }
            final Boolean[] create = new Boolean[1];
            create[0] = Boolean.FALSE;
            if (key == null) {
                NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(TXT_Enter(), TIT_Enter());
                if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
                    key = nd.getInputText();
                    create[0] = Boolean.TRUE;
                } else {
                    return info;
                }
            }
            
            PomModelUtils.implementInTransaction(mdl, new Runnable() {
                @Override
                public void run() {
                    String newVal = old.substring(0, start) + "${"  + key + "}" + old.substring(end);
                    element.setElementText(newVal);
                    if (create[0].equals(Boolean.TRUE)) {
                        Profile prof = mdl.findComponent(element.findPosition(), Profile.class, true);
                        if (prof != null) {
                            org.netbeans.modules.maven.model.pom.Properties props = prof.getProperties();
                            if (props == null) {
                                props = mdl.getFactory().createProperties();
                                prof.setProperties(props);
                            }
                            props.setProperty(key, old.substring(start, end));
                        } else {
                            org.netbeans.modules.maven.model.pom.Properties props = mdl.getProject().getProperties();
                            if (props == null) {
                                props = mdl.getFactory().createProperties();
                                mdl.getProject().setProperties(props);
                            }
                            props.setProperty(key, old.substring(start, end));
                        }
                    }
                }
            });
            return info;
        }
    }

}
