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

package org.netbeans.modules.refactoring.java.callhierarchy;

import com.sun.source.util.TreePath;
import java.awt.EventQueue;
import java.awt.Image;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.text.PositionBounds;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Pokorsky
 */
final class Call implements CallDescriptor {

    private static final String TYPE_COLOR = "#707070"; // NOI18N

    private List<Call> references;
    private List<CallOccurrence> occurrences;
    CallHierarchyModel model;

    private String displayName;
    private String htmlDisplayName;
    private Icon icon;
    TreePathHandle selection;
    TreePathHandle declaration;
    private TreePathHandle overridden;
    private ElementHandle identity;
    private Call parent;
    private boolean leaf;
    /** collection of references might not be complete */
    private boolean canceled = false;
    private enum State { CANCELED, BROKEN, INCOMPLETE }
    private State state;

    private Call() {
    }

    public List<Call> getReferences() {
        return references != null ? references : Collections.<Call>emptyList();
    }

    void setReferences(List<Call> references) {
        this.references = references;
    }

    public List<CallOccurrence> getOccurrences() {
        return occurrences;
    }

    CallHierarchyModel getModel() {
        return model;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public boolean isLeaf() {
        return leaf;
    }

    public boolean isCanceled() {
        return this.state == State.CANCELED;
    }

    void setCanceled(boolean canceled) {
        if (canceled) {
            this.state = State.CANCELED;
        }
    }

    void setIncomplete(boolean state) {
        if (state) {
            this.state = State.INCOMPLETE;
        }
    }

    public boolean isIncomplete() {
        return this.state == State.INCOMPLETE;
    }

    void setBroken() {
        this.state = State.BROKEN;
    }

    public boolean isBroken() {
        return this.state == State.BROKEN;
    }

    TreePathHandle getSourceToQuery() {
        if(parent != null && overridden != null) {
            return overridden;
        } else if(declaration != null) {
            return declaration;
        } else {
            return selection;
        }
    }

    @Override
    public void open() {
        if (occurrences != null && !occurrences.isEmpty()) {
            occurrences.get(0).open();
        }
    }

    /**
     * Creates an empty Call, which will be eventually replaced by the real item
     *
     * @return empty Call node, which must be replaced by the real one.
     */
    public static Call createEmpty() {
        Call c = new Call();
        c.setIncomplete(true);
        return c;
    }

    public static Call createRoot(CompilationInfo javac, TreePath selection, Element selectionElm, boolean isCallerGraph) {
        // supports go to source on the root of hierarchy tree  
        final List<TreePath> occurrences = Arrays.asList(selection);
        return createReference(javac, selection, selectionElm, null, isCallerGraph, occurrences);
    }

    public static Call createUsage(CompilationInfo javac, TreePath selection, Element selectionElm, Call parent, List<TreePath> occurrences) {
        return createReference(javac, selection, selectionElm, parent, parent.model.getType() == CallHierarchyModel.HierarchyType.CALLER, occurrences);
    }

    private static Call createReference(CompilationInfo javac, TreePath selection, Element selectionElm, Call parent, boolean isCallerGraph, List<TreePath> occurrences) {
        Call c = new Call();
        if (selectionElm.getKind() == ElementKind.INSTANCE_INIT || selectionElm.getKind() == ElementKind.STATIC_INIT) {
            c.displayName = "<init>"; // NOI18N
            c.identity = null;
        } else {
            c.displayName = ElementHeaders.getHeader(selectionElm, javac, ElementHeaders.NAME);
            c.identity = ElementHandle.create(selectionElm);
        }
        c.htmlDisplayName = createHtmlHeader(selectionElm, occurrences.size(), javac);

        Icon i = ElementIcons.getElementIcon(selectionElm.getKind(), selectionElm.getModifiers());

        if (isCallerGraph) {
            if (parent != null) {
                c.icon = ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.icon2Image(i), ImageUtilities.loadImage("org/netbeans/modules/refactoring/java/resources/up.png"), 0, 0));
            } else {
                c.icon = i;
            }
        } else {
            c.icon = ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.icon2Image(i), ImageUtilities.loadImage("org/netbeans/modules/refactoring/java/resources/down.png"), 0, 0));
        }

        c.selection = TreePathHandle.create(selection, javac);
        c.parent = parent;
        if (parent != null) {
            c.model = parent.model;
        }
        
        if (isCallerGraph && selectionElm.getKind() == ElementKind.METHOD) {
            Collection<ExecutableElement> overridenMethods = JavaRefactoringUtils.getOverriddenMethods((ExecutableElement) selectionElm, javac);
            if (!overridenMethods.isEmpty()) {
                ExecutableElement next = overridenMethods.iterator().next();
                c.overridden = TreePathHandle.create(next, javac);
                c.identity = ElementHandle.create(next);
            }
        }

        if(selectionElm.getKind() != ElementKind.INSTANCE_INIT && selectionElm.getKind() != ElementKind.STATIC_INIT) {
            c.declaration = TreePathHandle.create(selectionElm, javac);
        }

        if (c.identity != null) {
            boolean[] recursion = {false};
            c.leaf = isLeaf(selectionElm, c.identity, parent, isCallerGraph, recursion);
            if (recursion[0]) {
                Image badge = ImageUtilities.loadImage("org/netbeans/modules/refactoring/java/resources/recursion_badge.png");
                Image icon2Image = ImageUtilities.icon2Image(c.icon);
                Image badgedImage = ImageUtilities.mergeImages(icon2Image, badge, 8, 8);
                c.icon = ImageUtilities.image2Icon(badgedImage);
            }
        } else {
            c.leaf = true;
        }

        c.occurrences = new ArrayList<CallOccurrence>(occurrences.size());
        for (TreePath occurrence : occurrences) {
            c.occurrences.add(CallOccurrence.createOccurrence(javac, occurrence, parent));
        }
        return c;
    }

    @Override
    public String toString() {
        return String.format("name='%s', handle='%s', refs='%s'", displayName, selection, references); // NOI18N
    }

    private static boolean isLeaf(Element elm, ElementHandle handle, Call parent, boolean isCallerGraph, boolean[] recursion) {
        recursion[0] = false;
        if (!RefactoringUtils.isExecutableElement(elm)) {
            return true;
        }
        if (!isCallerGraph && elm.getModifiers().contains(Modifier.ABSTRACT)) {
            return true;
        }
        while (parent != null) {
            if (handle.equals(parent.identity)) {
                recursion[0] = true;
                return true;
            }
            parent = parent.parent;
        }
        return false;
    }

    private static String createHtmlHeader(Element e, int occurrences, CompilationInfo javac) {
        String member;
        switch (e.getKind()) {
            case METHOD:
            case CONSTRUCTOR:
                member = createHtmlHeader((ExecutableElement) e,javac);
                break;
            case INSTANCE_INIT:
            case STATIC_INIT:
                member = NbBundle.getMessage(Call.class, "Call.staticInitializerHtmlHeader");
                break;
            default:
                member = ElementHeaders.getHeader(e, javac, ElementHeaders.NAME);
        }

        String encloser = String.format("<font color=%s>%s</font>", TYPE_COLOR, // NOI18N
                javac.getElements().getBinaryName((TypeElement) e.getEnclosingElement()).toString());

        return NbBundle.getMessage(Call.class, "Call.htmlHeader", member, encloser, occurrences);
    }
    /**
     * Creates HTML display name of the Executable element
     * @see org.netbeans.modules.java.navigation.ElementScanningTask
     */
    private static String createHtmlHeader(ExecutableElement e, CompilationInfo javac) {

        boolean isDeprecated = javac.getElements().isDeprecated(e);

        StringBuilder sb = new StringBuilder();
        if (isDeprecated) {
            sb.append("<s>"); // NOI18N
        }
        if (e.getKind() == ElementKind.CONSTRUCTOR) {
            sb.append(e.getEnclosingElement().getSimpleName());
        } else {
            sb.append(e.getSimpleName());
        }
        if (isDeprecated) {
            sb.append("</s>"); // NOI18N
        }

        sb.append("("); // NOI18N

        for (VariableElement param : e.getParameters()) {
            sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N
            sb.append(print(param.asType()));
            sb.append("</font>"); // NOI18N
            sb.append(" "); // NOI18N
            sb.append(param.getSimpleName());
//            if (it.hasNext()) {
                sb.append(", "); // NOI18N
//            }
        }

        if (sb.charAt(sb.length() - 1) == '(') {
            sb.append(")"); // NOI18N
        } else {
            sb.replace(sb.length() - 2, sb.length(), ")"); // NOI18N
        }

//        if (e.getKind() != ElementKind.CONSTRUCTOR) {
//            TypeMirror rt = e.getReturnType();
//            if (rt.getKind() != TypeKind.VOID) {
//                sb.append(" : "); // NOI18N
//                sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N
//                sb.append(print(e.getReturnType()));
//                sb.append("</font>"); // NOI18N
//            }
//        }

        return sb.toString();
    }

    private static String print(TypeMirror tm) {
        StringBuilder sb;

        switch (tm.getKind()) {
            case DECLARED:
                DeclaredType dt = (DeclaredType) tm;
                sb = new StringBuilder(dt.asElement().getSimpleName().toString());
                List<? extends TypeMirror> typeArgs = dt.getTypeArguments();
                if (!typeArgs.isEmpty()) {
                    sb.append("&lt;"); // NOI18N

                    for (Iterator<? extends TypeMirror> it = typeArgs.iterator(); it.hasNext();) {
                        TypeMirror ta = it.next();
                        sb.append(print(ta));
                        if (it.hasNext()) {
                            sb.append(", "); // NOI18N
                        }
                    }
                    sb.append("&gt;"); // NOI18N
                }

                return sb.toString();
            case TYPEVAR:
                TypeVariable tv = (TypeVariable) tm;
                sb = new StringBuilder(tv.asElement().getSimpleName().toString());
                return sb.toString();
            case ARRAY:
                ArrayType at = (ArrayType) tm;
                sb = new StringBuilder(print(at.getComponentType()));
                sb.append("[]"); // NOI18N
                return sb.toString();
            case WILDCARD:
                WildcardType wt = (WildcardType) tm;
                sb = new StringBuilder("?");
                if (wt.getExtendsBound() != null) {
                    sb.append(" extends "); // NOI18N
                    sb.append(print(wt.getExtendsBound()));
                }
                if (wt.getSuperBound() != null) {
                    sb.append(" super "); // NOI18N
                    sb.append(print(wt.getSuperBound()));
                }
                return sb.toString();
            default:
                return tm.toString();
        }
    }

    static boolean doOpen(FileObject fo, PositionBounds bounds) {
        try {
            final int begin = bounds.getBegin().getOffset();
            final int end = bounds.getEnd().getOffset();
            DataObject od = DataObject.find(fo);
            final EditorCookie ec = od.getLookup().lookup(org.openide.cookies.EditorCookie.class);
            boolean opened = NbDocument.openDocument(od, begin, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
            if (opened) {
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        JEditorPane textC = NbDocument.findRecentEditorPane(ec);
                        if(textC != null) {
                            textC.setSelectionStart(begin);
                            textC.setSelectionEnd(end);
                        }
                    }
                });
                return true;
            }
            return opened;
        } catch (DataObjectNotFoundException e) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    Call.class, "Call.open.warning", FileUtil.getFileDisplayName(fo))); // NOI18N
        }

        return false;
    }

}
