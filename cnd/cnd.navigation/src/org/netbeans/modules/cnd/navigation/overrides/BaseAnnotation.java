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
package org.netbeans.modules.cnd.navigation.overrides;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.OverridesPopup;
import org.netbeans.modules.cnd.utils.ui.PopupUtil;
import org.openide.text.Annotation;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 */
public abstract class BaseAnnotation extends Annotation {

    public enum AnnotationType {
        IS_OVERRIDDEN,
        IS_OVERRIDDEN_PSEUDO,
        OVERRIDES,
        OVERRIDES_PSEUDO,
        OVERRIDEN_COMBINED,
        OVERRIDEN_COMBINED_PSEUDO,
        IS_SPECIALIZED,
        SPECIALIZES,
        EXTENDED_IS_SPECIALIZED,
        EXTENDED_SPECIALIZES
    }

    /*package*/ static final Logger LOGGER = Logger.getLogger("cnd.overrides.annotations.logger"); // NOI18N

    protected final StyledDocument document;
    protected final Position pos;
    protected final AnnotationType type;
    protected final Collection<CsmUID<? extends CsmOffsetableDeclaration>> baseUIDs;
    protected final Collection<CsmUID<? extends CsmOffsetableDeclaration>> descUIDs;
    protected final Collection<CsmUID<? extends CsmOffsetableDeclaration>> pseudoBaseUIDs;
    protected final Collection<CsmUID<? extends CsmOffsetableDeclaration>> pseudoDescUIDs;
    protected final Collection<CsmUID<? extends CsmOffsetableDeclaration>> baseTemplateUIDs;
    protected final Collection<CsmUID<? extends CsmOffsetableDeclaration>> specializationUIDs;
    
    protected BaseAnnotation(StyledDocument document, CsmOffsetableDeclaration decl,
            Collection<? extends CsmOffsetableDeclaration> baseDecls,
            Collection<? extends CsmOffsetableDeclaration> descDecls,
            Collection<? extends CsmOffsetableDeclaration> baseTemplates,
            Collection<? extends CsmOffsetableDeclaration> templateSpecializations) {
        assert decl != null;
        this.document = document;
        this.pos = getPosition(document, decl.getStartOffset());
        if (baseTemplates.isEmpty() && templateSpecializations.isEmpty()) {
            // overrides only 
            if (baseDecls.isEmpty()) {
                type = AnnotationType.IS_OVERRIDDEN;
            } else if (descDecls.isEmpty()) {
                type =  AnnotationType.OVERRIDES;
            } else {
                type = AnnotationType.OVERRIDEN_COMBINED;
            }
        } else if (baseDecls.isEmpty() && descDecls.isEmpty()) {
            // templates only
            if (baseTemplates.isEmpty()) {
                type = AnnotationType.IS_SPECIALIZED;
            } else if (templateSpecializations.isEmpty()) {
                type = AnnotationType.SPECIALIZES;
            } else {
                type = AnnotationType.SPECIALIZES;
            }
        } else {
            assert !baseTemplates.isEmpty() || !templateSpecializations.isEmpty() || !descDecls.isEmpty() || !baseDecls.isEmpty() : "all are empty?";
            if (baseTemplates.isEmpty()) {
                type = AnnotationType.EXTENDED_IS_SPECIALIZED;
            } else if (templateSpecializations.isEmpty()) {
                type = AnnotationType.EXTENDED_SPECIALIZES;
            } else {
                type = AnnotationType.EXTENDED_SPECIALIZES;
            }
        }
        baseUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(baseDecls.size());
        for (CsmOffsetableDeclaration d : baseDecls) {
            baseUIDs.add(UIDs.get(d));
        }
        pseudoBaseUIDs= Collections.emptyList();
        descUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(descDecls.size());
        for (CsmOffsetableDeclaration d : descDecls) {
            descUIDs.add(UIDs.get(d));
        }
        pseudoDescUIDs= Collections.emptyList();
        baseTemplateUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(baseTemplates.size());
        for (CsmOffsetableDeclaration d : baseTemplates) {
            baseTemplateUIDs.add(UIDs.get(d));
        }
        specializationUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(templateSpecializations.size());
        for (CsmOffsetableDeclaration d : templateSpecializations) {
            specializationUIDs.add(UIDs.get(d));
        }
    }
    
    protected BaseAnnotation(StyledDocument document, CsmFunction decl, CsmVirtualInfoQuery.CsmOverrideInfo thisMethod, 
            Collection<CsmVirtualInfoQuery.CsmOverrideInfo> baseDecls,
            Collection<CsmVirtualInfoQuery.CsmOverrideInfo> descDecls,
            Collection<? extends CsmOffsetableDeclaration> baseTemplates,
            Collection<? extends CsmOffsetableDeclaration> templateSpecializations) {
        assert decl != null;
        this.document = document;
        this.pos = getPosition(document, decl.getStartOffset());
        if (baseTemplates.isEmpty() && templateSpecializations.isEmpty()) {
            // overrides only 
            if (baseDecls.isEmpty()) {
                boolean pseudo = false;
                if (thisMethod != null) {
                    pseudo = !thisMethod.isVirtual();
                }
                for(CsmVirtualInfoQuery.CsmOverrideInfo info :descDecls) {
                    if (!info.isVirtual()) {
                        pseudo = true;
                        break;
                    }
                }
                if (pseudo) {
                    type = AnnotationType.IS_OVERRIDDEN_PSEUDO;
                } else {
                    type = AnnotationType.IS_OVERRIDDEN;
                }
            } else if (descDecls.isEmpty()) {
                boolean pseudo = false;
                for(CsmVirtualInfoQuery.CsmOverrideInfo info :baseDecls) {
                    if (!info.isVirtual()) {
                        pseudo = true;
                        break;
                    }
                }
                if (pseudo) {
                    type =  AnnotationType.OVERRIDES_PSEUDO;
                } else {
                    type =  AnnotationType.OVERRIDES;
                }
            } else {
                boolean pseudo = false;
                for(CsmVirtualInfoQuery.CsmOverrideInfo info :descDecls) {
                    if (!info.isVirtual()) {
                        pseudo = true;
                        break;
                    }
                }
                if (!pseudo) {
                    for(CsmVirtualInfoQuery.CsmOverrideInfo info :baseDecls) {
                        if (!info.isVirtual()) {
                            pseudo = true;
                            break;
                        }
                    }
                }
                if (pseudo) {
                    type = AnnotationType.OVERRIDEN_COMBINED_PSEUDO;
                } else {
                    type = AnnotationType.OVERRIDEN_COMBINED;
                }
            }
        } else if (baseDecls.isEmpty() && descDecls.isEmpty()) {
            // templates only
            if (baseTemplates.isEmpty()) {
                type = AnnotationType.IS_SPECIALIZED;
            } else if (templateSpecializations.isEmpty()) {
                type = AnnotationType.SPECIALIZES;
            } else {
                type = AnnotationType.SPECIALIZES;
            }
        } else {
            assert !baseTemplates.isEmpty() || !templateSpecializations.isEmpty() || !descDecls.isEmpty() || !baseDecls.isEmpty() : "all are empty?";
            if (baseTemplates.isEmpty()) {
                type = AnnotationType.EXTENDED_IS_SPECIALIZED;
            } else if (templateSpecializations.isEmpty()) {
                type = AnnotationType.EXTENDED_SPECIALIZES;
            } else {
                type = AnnotationType.EXTENDED_SPECIALIZES;
            }
        }
        baseUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(baseDecls.size());
        pseudoBaseUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(baseDecls.size());
        for (CsmVirtualInfoQuery.CsmOverrideInfo info : baseDecls) {
            if (info.isVirtual()) {
                baseUIDs.add(UIDs.get(info.getMethod()));
            } else {
                pseudoBaseUIDs.add(UIDs.get(info.getMethod()));
            }
        }
        descUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(descDecls.size());
        pseudoDescUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(descDecls.size());
        for (CsmVirtualInfoQuery.CsmOverrideInfo info : descDecls) {
            if (info.isVirtual()) {
                descUIDs.add(UIDs.get(info.getMethod()));
            } else {
                pseudoDescUIDs.add(UIDs.get(info.getMethod()));
            }
        }
        baseTemplateUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(baseTemplates.size());
        for (CsmOffsetableDeclaration d : baseTemplates) {
            baseTemplateUIDs.add(UIDs.get(d));
        }
        specializationUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(templateSpecializations.size());
        for (CsmOffsetableDeclaration d : templateSpecializations) {
            specializationUIDs.add(UIDs.get(d));
        }
    }
    
    public AnnotationType getType() {
        return type;
    }

    @Override
    public String getAnnotationType() {
        switch(getType()) {
            case IS_OVERRIDDEN:
                return "org-netbeans-modules-cnd-navigation-is_overridden"; //NOI18N
            case IS_OVERRIDDEN_PSEUDO:
                return "org-netbeans-modules-cnd-navigation-is_overridden_pseudo"; //NOI18N
            case OVERRIDES:
                return "org-netbeans-modules-cnd-navigation-overrides"; //NOI18N
            case OVERRIDES_PSEUDO:
                return "org-netbeans-modules-cnd-navigation-overrides_pseudo"; //NOI18N
            case OVERRIDEN_COMBINED:
                return "org-netbeans-modules-cnd-navigation-is_overridden_combined"; //NOI18N
            case OVERRIDEN_COMBINED_PSEUDO:
                return "org-netbeans-modules-cnd-navigation-is_overridden_combined_pseudo"; //NOI18N
            case SPECIALIZES:
                return "org-netbeans-modules-cnd-navigation-specializes"; // NOI18N
            case IS_SPECIALIZED:
                return "org-netbeans-modules-cnd-navigation-is_specialized"; // NOI18N
            case EXTENDED_SPECIALIZES:
                return "org-netbeans-modules-cnd-navigation-extended_specializes"; // NOI18N
            case EXTENDED_IS_SPECIALIZED:
                return "org-netbeans-modules-cnd-navigation-extended_is_specialized"; // NOI18N
            default:
                throw new IllegalStateException("Currently not implemented: " + type); //NOI18N
        }
    }
    
    protected final String addTemplateAnnotation(String baseDescr) throws MissingResourceException {
        if (baseTemplateUIDs.isEmpty() && !specializationUIDs.isEmpty()) {
            CharSequence text = "..."; //NOI18N
            if (specializationUIDs.size() == 1) {
                CsmOffsetableDeclaration obj = specializationUIDs.iterator().next().getObject();
                if (obj != null) {
                    text = obj.getQualifiedName();
                }
            }
            if (baseDescr.isEmpty()) {
                baseDescr = NbBundle.getMessage(getClass(), "LAB_Specialization", text);
            } else {
                baseDescr = NbBundle.getMessage(getClass(), "LAB_Specialization2", baseDescr, text);
            }
        } else if (!baseTemplateUIDs.isEmpty() && specializationUIDs.isEmpty()) {
            CharSequence text = "..."; //NOI18N
            if (baseTemplateUIDs.size() == 1) {
                CsmOffsetableDeclaration obj = baseTemplateUIDs.iterator().next().getObject();
                if (obj != null) {
                    text = obj.getQualifiedName();
                }
            }
            if (baseDescr.isEmpty()) {
                baseDescr = NbBundle.getMessage(getClass(), "LAB_BaseTemplate", text);
            } else {
                baseDescr = NbBundle.getMessage(getClass(), "LAB_BaseTemplate2", baseDescr, text);
            }
        } else if (!baseTemplateUIDs.isEmpty() && !specializationUIDs.isEmpty()) {
            if (baseDescr.isEmpty()) {
                baseDescr = NbBundle.getMessage(getClass(), "LAB_BaseTemplateAndSpecialization");
            } else {
                baseDescr = NbBundle.getMessage(getClass(), "LAB_BaseTemplateAndSpecialization2", baseDescr);
            }
        }
        return baseDescr;
    }
    
    public boolean attach() {
        int offset = pos.getOffset();
        if (offset == -1) {
            return false;
        }
        Position endPos = document.getEndPosition();
        if (endPos == null || offset >= endPos.getOffset()) {
            return false;
        }
        if (!(document instanceof NbDocument.Annotatable)) {
            return false;
        }
        if (getAnnotationType() == null) {
            return false;
        }
        try {
            NbDocument.addAnnotation(document, pos, -1, this);
            return true;
        } catch (Throwable e) {
            Exceptions.printStackTrace(e);
            return false;
        }
    }
    
    public void detachImpl() {
        NbDocument.removeAnnotation(document, this);
    }
    
    @Override
    public String toString() {
        return "[IsOverriddenAnnotation: " + getShortDescription() + "]"; //NOI18N
    }

    public Position getPosition() {
        return pos;
    }

    private static Position getPosition(final StyledDocument doc, final int offset) {
        class Impl implements Runnable {

            private Position pos;

            @Override
            public void run() {
                if (offset < 0 || offset >= doc.getLength()) {
                    return;
                }

                try {
                    pos = doc.createPosition(offset - NbDocument.findLineColumn(doc, offset));
                } catch (BadLocationException ex) {
                    //should not happen?
                    Logger.getLogger(BaseAnnotation.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }

        Impl i = new Impl();

        doc.render(i);
        
        if (i.pos == null) {
            i.pos = new Position() {
                @Override
                public int getOffset() {
                    return -1;
                }
            };
        }

        return i.pos;
    }
    
    protected abstract CharSequence debugTypeString();

    /** for test/debugging purposes */
    public CharSequence debugDump() {
        StringBuilder sb = new StringBuilder();
        int line = NbDocument.findLineNumber(document, getPosition().getOffset()) + 1; // convert to 1-based
        sb.append(line);
        sb.append(':');
        sb.append(debugTypeString());
        sb.append(' ');
        boolean first = true;

        Comparator<CsmOffsetableDeclaration> comparator = new Comparator<CsmOffsetableDeclaration>() {
            @Override
            public int compare(CsmOffsetableDeclaration o1, CsmOffsetableDeclaration o2) {
                return o1.getQualifiedName().toString().compareTo(o2.getQualifiedName().toString());
            }

        };
        List<CsmOffsetableDeclaration> baseDecls = toDeclarations(baseUIDs);
        baseDecls.addAll(toDeclarations(pseudoBaseUIDs));
        Collections.sort(baseDecls, comparator);

        List<CsmOffsetableDeclaration> descDecls = toDeclarations(descUIDs);
        descDecls.addAll(toDeclarations(pseudoDescUIDs));
        Collections.sort(descDecls, comparator);

        List<CsmOffsetableDeclaration> baseTemplateDecls = toDeclarations(baseTemplateUIDs);
        Collections.sort(baseTemplateDecls, comparator);

        List<CsmOffsetableDeclaration> specializationDecls = toDeclarations(specializationUIDs);
        Collections.sort(specializationDecls, comparator);
        
        List<CsmOffsetableDeclaration> allDecls = new ArrayList<CsmOffsetableDeclaration>();
        allDecls.addAll(baseDecls);
        allDecls.addAll(descDecls);
        allDecls.addAll(baseTemplateDecls);
        allDecls.addAll(specializationDecls);

        for (CsmOffsetableDeclaration decl : allDecls) {
            int gotoLine = decl.getStartPosition().getLine();
            String gotoFile = decl.getContainingFile().getName().toString();
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(decl.getQualifiedName());
            sb.append(' ');
            sb.append(gotoFile);
            sb.append(':');
            sb.append(gotoLine);
        }
        return sb;
    }

    void mouseClicked(JTextComponent c, Point p) {
        Point position = new Point(p);
        position.y += c.getFontMetrics(c.getFont()).getHeight();
        SwingUtilities.convertPointToScreen(position, c);        
        performGoToAction(position);
    }
    
    private void performGoToAction(Point position) {
        if (baseUIDs.size() + pseudoBaseUIDs.size() + descUIDs.size() + pseudoDescUIDs.size() +baseTemplateUIDs.size() + specializationUIDs.size() == 1) {
            Collection<CsmUID<? extends CsmOffsetableDeclaration>> all = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>> (1);
            all.addAll(baseUIDs);
            all.addAll(pseudoBaseUIDs);
            all.addAll(descUIDs);
            all.addAll(pseudoDescUIDs);
            all.addAll(baseTemplateUIDs);
            all.addAll(specializationUIDs);
            CsmUID<? extends CsmOffsetableDeclaration> uid = all.iterator().next();
            final CsmOffsetableDeclaration decl = uid.getObject();
            if (decl != null) { // although openSource seems to process nulls ok, it's better to check here
                final String taskName = "Open override function"; //NOI18N
                Runnable run = new Runnable() {

                    @Override
                    public void run() {
                        CsmUtilities.openSource(decl);
                    }
                };
                CsmModelAccessor.getModel().enqueue(run, taskName);
            }
        } else if (baseUIDs.size() + pseudoBaseUIDs.size() + descUIDs.size() + pseudoDescUIDs.size() +baseTemplateUIDs.size() + specializationUIDs.size() > 1) { 
            String caption = getShortDescription();
            OverridesPopup popup = new OverridesPopup(caption, toDeclarations(baseUIDs), toDeclarations(pseudoBaseUIDs),
                    toDeclarations(descUIDs), toDeclarations(pseudoDescUIDs),
                    toDeclarations(baseTemplateUIDs), toDeclarations(specializationUIDs));
            PopupUtil.showPopup(popup, caption, position.x, position.y, true, 0);
        } else {
            throw new IllegalStateException("method list should not be empty"); // NOI18N
        }
    }

    private static List<CsmOffsetableDeclaration> toDeclarations(Collection<CsmUID<? extends CsmOffsetableDeclaration>> uids) {
        List<CsmOffsetableDeclaration> decls = new ArrayList<CsmOffsetableDeclaration>(uids.size());
        for (CsmUID<? extends CsmOffsetableDeclaration> uid : uids) {
            CsmOffsetableDeclaration decl = uid.getObject();
            if (decl != null) {
                decls.add(decl);
            }
        }
        return decls;
    }
}
