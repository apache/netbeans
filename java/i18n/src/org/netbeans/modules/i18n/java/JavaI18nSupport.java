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


package org.netbeans.modules.i18n.java;


import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.InfoPanel;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.PropertyPanel;
import org.netbeans.modules.i18n.ResourceHolder;
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.MapFormat;
import org.openide.util.Lookup;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/** 
 * Support for internationalizing strings in java sources.
 *
 * @author Peter Zavadsky
 * @see I18nSupport
 */
public class JavaI18nSupport extends I18nSupport {

    /** Modifiers of field which are going to be internbationalized (default is private static final). */
    protected Set<Modifier> modifiers = EnumSet.of(Modifier.PRIVATE,
                                                   Modifier.STATIC,
                                                   Modifier.FINAL);

    /** Identifier of field element pointing to field which defines resource bundle in the source. */
    protected String identifier;

    /** Generate field? */
    protected boolean generateField;
    
    /** Init string format. */
    protected String initFormat;

    private JavaReplacePanel additionalCustomizer;
    
    /** Constructor. 
     * @see I18nSupport */
    public JavaI18nSupport(DataObject sourceDataObject) {
        super(sourceDataObject);

        initFormat = I18nUtil.getOptions().getInitJavaCode();
    }
    
    
    /** Creates <code>I18nFinder</code>. Implements superclass abstract method. */
    protected I18nFinder createFinder() {
        return new JavaI18nFinder(document);
    }
    
    /** Creates <code>I18nReplacer</code>. Implemens superclass abstract method. */
    protected I18nReplacer createReplacer() {        
        return new JavaI18nReplacer();
    }
    
    /** Creates <code>ResourceHolder</code>. Implemens superclass abstract method. */
    protected ResourceHolder createResourceHolder() {
        return new JavaResourceHolder();
    }
    
    /** Implements superclass abstract method. */
    public I18nString getDefaultI18nString(HardCodedString hcString) {
        I18nString i18nString = new JavaI18nString(this);
        
        final ResourceHolder resourceHolder
                = i18nString.getSupport().getResourceHolder();
        if (resourceHolder.getResource() == null) {
            DataObject lastResource = I18nUtil.getOptions().getLastResource2(sourceDataObject);
            if (lastResource != null) {
                FileObject sourceFile = sourceDataObject.getPrimaryFile();
                FileObject bundleFile = lastResource.getPrimaryFile();
                ClassPath execClassPath = ClassPath.getClassPath(sourceFile,
                                                                 ClassPath.EXECUTE);
                if (execClassPath != null) {
                    if (execClassPath.getResourceName(bundleFile) != null) {
                        resourceHolder.setResource(lastResource);
                    }
                }
            }
        }

        if (hcString == null) {
            return i18nString;
        }
        
        String text = decodeUnicodeSeq(hcString.getText());
        String key = text.toUpperCase(); // don't replace ' ' with '_' ! #168798
        
        String hcStr = hcString.getText();
        String strAndVar = ((JavaI18nFinder)getFinder()).strAndVarFound;
        int strAndVarLength = strAndVar.length();
        if(hcStr.contains(strAndVar)) { // handle Bug 33759 (http://netbeans.org/bugzilla/show_bug.cgi?id=33759)
            ArrayList<String> variables = new ArrayList<String>();
            int startVar = hcStr.indexOf(strAndVar);
            int endVar = -1;
            int counterVar = 0;
            text = hcStr.substring(0, startVar);
            while(startVar != -1) {
                if(counterVar > 0) {
                    text = text.concat(hcStr.substring(endVar + strAndVarLength, startVar));
                }
                endVar = hcStr.indexOf(strAndVar, startVar + strAndVarLength);
                if(startVar + strAndVarLength == endVar) {
                    counterVar--;
                } else {
                    text = text.concat("{").concat(Integer.toString(counterVar)).concat("}"); // NOI18N
                    variables.add(hcStr.substring(startVar + strAndVarLength, endVar).trim());
                }
                startVar = hcStr.indexOf(strAndVar, endVar + strAndVarLength);
                counterVar++;
                if(startVar == -1) {
                    text = text.concat(hcStr.substring(endVar + strAndVarLength));
                }
            }
            key = text.toUpperCase(); // don't replace ' ' with '_' ! #168798
            i18nString.setKey(key);
            i18nString.setValue(text);
            i18nString.setComment(""); // NOI18N
            i18nString.setReplaceFormat(I18nUtil.getReplaceFormatItems().get(4));
            String[] arguments = new String[variables.size()];
            for (int i = 0; i < variables.size(); i++) {
                arguments[i] = variables.get(i);                
            }
            ((JavaI18nString)i18nString).setArguments(arguments);
        } else {
            i18nString.setKey(key);
            i18nString.setValue(text);
            i18nString.setComment(""); // NOI18N

            // If generation of field is set and replace format doesn't include identifier argument replace it with the default with identifier.
            if (isGenerateField() && i18nString.getReplaceFormat().indexOf("{identifier}") == -1) { // NOI18N
                i18nString.setReplaceFormat(I18nUtil.getReplaceFormatItems().get(0));
            }
        }
        return i18nString;
    }

    private static final String octalDigitChars
                                = "01234567";                           //NOI18N
    private static final String hexaDigitChars
                                = "0123456789abcdefABCDEF";             //NOI18N

    /**
     * Translates Java Unicode sequences (<code>&#x5c;u<i>nnnn</i></code>)
     * to the corresponding characters.
     * @param  text  text with or without Unicode sequences
     * @return  the same text with Unicode sequences replaced with corresponding
     *          characters; may be the same instance as the passed text
     *          if there were no valid Unicode sequences present in it
     * @author  Marian Petras
     */
    private static String decodeUnicodeSeq(String text) {
        final StringBuilder result = new StringBuilder(text.length());
        final char[] chars = text.toCharArray();

        final int stateInitial = 0;
        final int stateBackSlash = 1;
        final int stateUnicode = 2;
        final int stateOctalValue = 3;

        int state = stateInitial;
        int unicodeValue = 0;
        char[] unicodeValueChars = new char[3];
        int valueBytesRead = 0;
        int position;

        int charIndex = 0;
        while (charIndex < chars.length) {
            char c = chars[charIndex++];
            switch (state) {
                case stateInitial:
                    if (c == '\\') {
                        state = stateBackSlash;
                    } else {
                        result.append(c);
                    }
                    break;
                case stateBackSlash:
                    if (c == 'u') {
                        state = stateUnicode;
                    } else if ((c >= '0') && (c <= '3')) {
                        unicodeValue = c - '0';
                        assert (unicodeValue >= 0) && (unicodeValue <= 3);
                        valueBytesRead = 1;
                        state = stateOctalValue;
                    } else {
                        result.append('\\').append(c);
                        state = stateInitial;
                    }
                    break;
                case stateOctalValue:
                    position = octalDigitChars.indexOf(c);
                    if (position >= 0) {
                        unicodeValue = (unicodeValue << 3) | position;
                        valueBytesRead++;
                    } else {
                        charIndex--;    //handle the character in the next round
                    }
                    if ((position < 0) || (valueBytesRead == 3)) {
                        appendChar(result, unicodeValue);
                        state = stateInitial;
                        valueBytesRead = 0;
                        unicodeValue = 0;
                    }
                    break;
                case stateUnicode:
                    position = hexaDigitChars.indexOf(c);
                    if (position >= 0) {
                        if (position > 15) {   //one of [A-F] used
                            position -= 6;     //transform to lowercase
                        }
                        assert position <= 15;
                        unicodeValue = (unicodeValue << 4) | position;
                        if (++valueBytesRead == 4) {
                            appendChar(result, unicodeValue);
                            state = stateInitial;
                        } else {
                            unicodeValueChars[valueBytesRead - 1] = c;
                            /* keep the state at stateUnicode */
                        }
                    } else if (c == 'u') {
                        /*
                         * Handles \\u.... sequences with multiple
                         * 'u' characters, such as \\uuu1234 (which is legal).
                         */

                        /* keep the state at stateUnicode */
                    } else {
                        /* append the malformed Unicode sequence: */
                        result.append('\\');
                        result.append('u');
                        for (int i = 0; i < valueBytesRead; i++) {
                            result.append(unicodeValueChars[i]);
                        }
                        result.append(c);
                        state = stateInitial;
                    }
                    if (state != stateUnicode) {
                        valueBytesRead = 0;
                        unicodeValue = 0;
                    }
                    break;
                default:
                    assert false;
                    throw new IllegalStateException();
            } //switch (state)
        } //for-loop
        switch (state) {
            case stateInitial:
                break;
            case stateBackSlash:
                result.append('\\');
                break;
            case stateOctalValue:
                assert (valueBytesRead >= 0) && (valueBytesRead < 3);
                appendChar(result, unicodeValue);
                break;
            case stateUnicode:
                /* append the incomplete Unicode sequence: */
                assert (valueBytesRead >= 0) && (valueBytesRead < 4);
                result.append('\\').append('u');
                for (int i = 0; i < valueBytesRead; i++) {
                    result.append(unicodeValueChars[i]);
                }
                break;
            default:
                assert false;
                throw new IllegalStateException();
        }

        return result.toString();
    }

    /**
     * Appends a character to the given buffer.
     * 
     * @param  buf  buffer to which a character is to be added
     * @param  unicodeValue  Unicode value of the character to be appended;
     *                       must be in range from {@code 0} to {@code 65535}
     * @return  the passed buffer
     */
    private static final StringBuilder appendChar(StringBuilder buf,
                                                  int unicodeValue)
                throws IllegalArgumentException {
        if ((unicodeValue < 0) || (unicodeValue > 0xffff)) {
            throw new IllegalArgumentException("value out of range: "   //NOI18N
                                               + unicodeValue);
        }

        /* append the Unicode character: */
        if ((unicodeValue >= 0x20) && (unicodeValue != 0x7f)) {
            buf.append((char) unicodeValue);
        } else {
            buf.append('\\');
            switch (unicodeValue) {
                case 0x08:
                    buf.append('b');            //bell
                    break;
                case 0x09:
                    buf.append('t');            //tab
                    break;
                case 0x0a:
                    buf.append('n');            //NL
                    break;
                case 0x0c:
                    buf.append('f');            //FF
                    break;
                case 0x0d:
                    buf.append('r');            //CR
                    break;
                default:
                    buf.append('u');
                    for (int shift = 12; shift >= 0; shift -= 4) {
                        buf.append(hexaDigitChars.charAt(
                                ((unicodeValue >> shift) & 0xf)));
                    }
                    break;
            }
        }

        return buf;
    }
    
    /** Implements <code>I18nSupport</code> superclass abstract method. Gets info panel about found hard string. */
    public JPanel getInfo(HardCodedString hcString) {
        return new JavaInfoPanel(hcString, document);
    }

    /** Getter for identifier. */    
    public String getIdentifier() {
        if ((identifier == null) || (identifier == "")) {               //NOI18N
            createIdentifier();
        }
        return identifier;
    }

    /** Setter for identifier. */    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /** Getter for modifiers. */
    public Set<Modifier> getModifiers() {
        return modifiers;
    }
    
    /** Setter for modifiers. */
    public void setModifiers(Set<Modifier> modifiers) {
        this.modifiers = modifiers;
    }
    
    /** Getter for generate field property.*/
    public boolean isGenerateField() {
        return generateField;
    }
    
    /** Setter for generate field property. */
    public void setGenerateField(boolean generateField) {
        this.generateField = generateField;
    }
    
    /** Getter for init format property. */
    public String getInitFormat() {
        return initFormat;
    }
    
    /** Setter for init format property. */
    public void setInitFormat(String initFormat) {
        this.initFormat = initFormat;
    }

    /** Overrides superclass method. */
    @Override
    public PropertyPanel getPropertyPanel() {
        return new JavaPropertyPanel();
    }
    
    /** Overrides superclass method. 
     * @return true */
    @Override
    public boolean hasAdditionalCustomizer() {
        return true;
    }
    
    /** Overrides superclass method. 
     * @return <code>JavaReplacePanel</code> which offers to customize additional
     * source values (in our case for creating bundle field) */
    @Override
    public JPanel getAdditionalCustomizer() {
        if (additionalCustomizer == null) 
            additionalCustomizer = new JavaReplacePanel(this);
        return additionalCustomizer;
    }

    /** Overrides superclass method. 
     * Actuallay creates bundle field specified by user */
    @Override
    public void performAdditionalChanges() {
        // Creates field.
        createField();
    }

    /** Utility method. Creates identifier for this support instance. */
    public void createIdentifier() {
        String name;
        
        try {
            name = resourceHolder.getResource().getName();
        } catch (NullPointerException npe) {
            identifier = ""; // NOI18N
            return;
        }

        // first letter to lowercase
        if (name.length() > 0) {
            name = name.substring(0, 1).toLowerCase() + name.substring(1);
        } else {
            name = name.toLowerCase();
        }
        
        identifier = name;
    }

    /**
     * Task that adds a field to the internationalized Java source file.
     */
    private final class AddFieldTask implements Task<WorkingCopy> {

        /** name of the field to be added */
        private final String fieldName;

        AddFieldTask(String fieldName) {
            this.fieldName = fieldName;
        }

        public void run(WorkingCopy workingCopy) throws Exception {
            final TypeElement sourceClassElem = getClass(workingCopy);
            if (sourceClassElem == null) {
                return;
            }

            List<? extends javax.lang.model.element.Element> classMembers
                    = sourceClassElem.getEnclosedElements();
            List<? extends VariableElement> fields
                    = ElementFilter.fieldsIn(classMembers);
            if (containsField(fields, fieldName)) {
                return;
            }

            int targetPosition = findTargetPosition(classMembers, fields);

            final TreeMaker treeMaker = workingCopy.getTreeMaker();
            final Elements elements = workingCopy.getElements();
            final Trees trees = workingCopy.getTrees();
            final TreeUtilities treeUtilities = workingCopy.getTreeUtilities();

            TypeElement resourceBundleTypeElem = elements.getTypeElement(
                                            "java.util.ResourceBundle");//NOI18N
            assert resourceBundleTypeElem != null;

            ExpressionTree fieldDefaultValue
                    = treeUtilities.parseVariableInitializer(getInitString(),
                                                             new SourcePositions[1]);
            TreePath classTreePath = trees.getPath(sourceClassElem);
            Scope classScope = trees.getScope(classTreePath);
            if (classScope != null) {
                treeUtilities.attributeTree(fieldDefaultValue, classScope);
            }

            VariableTree field = treeMaker.Variable(
                    treeMaker.Modifiers(modifiers),
                    fieldName,
                    treeMaker.QualIdent(resourceBundleTypeElem),
                    GeneratorUtilities.get(workingCopy).importFQNs(fieldDefaultValue));

            ClassTree oldClassTree = (ClassTree) classTreePath.getLeaf();
            ClassTree newClassTree = (targetPosition != -1) 
                                     ? treeMaker.insertClassMember(oldClassTree, targetPosition, field)
                                     : treeMaker.addClassMember(oldClassTree, field);
            workingCopy.rewrite(oldClassTree, newClassTree);
        }

        /**
         * Finds the target position within the source class element.
         * In the current implementation, the target position is just below
         * the last static field of the class; if there is no static field
         * in the class, the target position is the top of the class.
         * 
         * @param  classMembers  list of all members of the class
         * @param  fields  list of the fields in the class
         * @return  target position ({@code 0}-based) of the field,
         *          or {@code -1} if the field should be added to the end
         *          of the class
         */
        private int findTargetPosition(
                List<? extends javax.lang.model.element.Element> classMembers,
                List<? extends VariableElement> fields) {
            if (fields.isEmpty()) {
                return 0;
            }

            int target = 0;
            boolean skippingStaticFields = false;
            Iterator<? extends javax.lang.model.element.Element> membersIt
                    = classMembers.iterator();
            for (int index = 0; membersIt.hasNext(); index++) {
                javax.lang.model.element.Element member = membersIt.next();
                ElementKind kind = member.getKind();
                if (kind.isField()
                        && (kind != ElementKind.ENUM_CONSTANT)
                        && member.getModifiers().contains(Modifier.STATIC)) {
                    /* it is a static field - skip it! */
                    skippingStaticFields = true;
                } else if (skippingStaticFields) {
                    /* we were skipping all static fields - until now */
                    skippingStaticFields = false;
                    target = index;
                }
            }

            return !skippingStaticFields ? target : -1;
        }

        /**
         * Finds a main top-level class or a nested class element
         * for {@code sourceDataObject} which should be initialized.
         */
        private TypeElement getClass(WorkingCopy workingCopy)
                                                            throws IOException {
            workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);

            final String preferredName = sourceDataObject.getName();
            TypeElement firstPublicNestedClass = null;
            
            List<? extends TypeElement> topClasses = workingCopy.getTopLevelElements();
            for (TypeElement topElement : topClasses) {
                ElementKind elementKind = topElement.getKind();
                if (!elementKind.isClass()) {
                    continue;
                }

                if (topElement.getSimpleName().contentEquals(preferredName)) {
                    return topElement;
                }

                if ((firstPublicNestedClass == null)
                        && topElement.getModifiers().contains(Modifier.PUBLIC)) {
                    firstPublicNestedClass = topElement;
                }
            }

            return firstPublicNestedClass;
        }

        /**
         * Checks whether the given class contains a field of the given name.
         * 
         * @param  clazz  class that should be searched
         * @param  fieldName  name of the field
         * @return  {@code true} if the class contains such a field,
         *          {@code false} otherwise
         */
        private boolean containsField(List<? extends VariableElement> fields,
                                      String fieldName) {
            if (!fields.isEmpty()) {
                for (VariableElement field : fields) {
                    if (field.getSimpleName().contentEquals(fieldName)) {
                        return true;
                    }
                }
            }
            return false;
        }
    
    }
    
    /**
     * Creates a new field which holds a reference to the resource holder
     * (resource bundle). The field is added to the internationalized file.
     */
    private void createField() {
        // Check if we have to generate field.
        if (!isGenerateField()) {
            return;
        }

        final JavaSource javaSource = JavaSource.forDocument(document);
        try {
            ModificationResult result
                    = javaSource.runModificationTask(new AddFieldTask(getIdentifier()));
            result.commit();
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
    }

    /** 
     * Helper method.
     * Gets the string, the piece of code which initializes field resource
     * bundle in the source.
     * E.g.:
     * <pre><code>java.util.ResourceBundle &lt;identifier name&gt;<br />
     *           = <b>java.util.ResourceBundle.getBundle(&quot;&lt;package name&gt;</b>&quot;)</code></pre>
     *
     * @return  String -&gt; piece of initilizing code.
     */
    public String getInitString() {
        String initJavaFormat = getInitFormat();

        // Create map.
        FileObject fo = resourceHolder.getResource().getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);

        
        Map<String,String> map = new HashMap<String,String>(3);

        map.put("bundleNameSlashes", cp.getResourceName(fo, '/', false));//NOI18N
        map.put("bundleNameDots", cp.getResourceName(fo, '.', false));  //NOI18N
        map.put("sourceFileName", (sourceDataObject != null)
                                  ? sourceDataObject.getPrimaryFile().getName()
                                  : "");                                //NOI18N

        return MapFormat.format(initJavaFormat, map);
        
    }
    
    /** Replacer for java sources used by enclosing class. */
    public static class JavaI18nReplacer implements I18nReplacer {
        
        /** Constructor.*/
        public JavaI18nReplacer() {
        }
        

        /** Replaces found hard coded string in source. 
         * @param hcString found hard coded string to-be replaced 
         * @param rbString holds replacing values */
        public void replace(final HardCodedString hcString,
                            final I18nString i18nString) {
            // comment out?
            if (i18nString.getKey() == null) {
                final StyledDocument document = i18nString.getSupport().getDocument();
                NbDocument.runAtomic(
                document,
                new Runnable() {
                    public void run() {
                        try {
                            // find the end of line
                            int idx = hcString.getEndPosition().getOffset() + 1;
                            String text = document.getText(idx, document.getLength() - idx);
                            for (int i = idx; idx-i < text.length() && text.charAt(idx-i) != '\n' ; idx++);

                            document.insertString(idx, " //NOI18N", null); //NOI18N
                        } catch (BadLocationException ble) {
                            NotifyDescriptor.Message message
                                    = new NotifyDescriptor.Message(
                                            NbBundle.getMessage(JavaI18nSupport.class,
                                                                "MSG_CouldNotReplace"),//NOI18N
                                            NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(message);
                        }
                    }
                });
                return;
            }

            if (!(i18nString instanceof JavaI18nString)) {
                throw new IllegalArgumentException(
                        "I18N module: i18nString have to be an instance of JavaI18nString.");//NOI18N
            }
            
            final String newCode = i18nString.getReplaceString();

            final StyledDocument document = i18nString.getSupport().getDocument();
            
            // Call runAtomic method to break guarded flag if it is necessary. (For non-guarded works as well).
            NbDocument.runAtomic(
            document,
            new Runnable() {
                public void run() {
                    try {
                        if (hcString.getLength() > 0) {
                            document.remove(hcString.getStartPosition().getOffset(),
                                            hcString.getLength());
                        }
                        if (newCode != null && newCode.length() > 0) {
                            document.insertString(hcString.getEndPosition().getOffset(),
                                                  newCode, null);
                        }
                    } catch (BadLocationException ble) {
                        NotifyDescriptor.Message message
                                = new NotifyDescriptor.Message(
                                        NbBundle.getMessage(JavaI18nSupport.class,
                                                            "MSG_CouldNotReplace"),//NOI18N
                                        NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(message);
                    }
                }
            });
        }
    } // End of nested class JavaI18nReplacer.

    
    /** Panel for showing info about hard coded string. */
    private static class JavaInfoPanel extends InfoPanel {
        
        /** Constructor. */
        public JavaInfoPanel(HardCodedString hcString, StyledDocument document) {
            super(hcString, document);
        }
        
        /** Implements superclass abstract method. */
        protected void setHardCodedString(HardCodedString hcString, StyledDocument document) {

            getStringText().setText(hcString == null ? ""               //NOI18N
                                                     : hcString.getText());
            
            int pos;

            String hardLine;
            
            if (hcString.getStartPosition() == null) {
                hardLine = "";                                          //NOI18N
            } else {
                pos = hcString.getStartPosition().getOffset();

                try {
                    Element paragraph = document.getParagraphElement(pos);
                    hardLine = document.getText(paragraph.getStartOffset(),
                                                paragraph.getEndOffset() - paragraph.getStartOffset())
                                       .trim();
                } catch (BadLocationException ble) {
                    hardLine = ""; // NOI18N
                }
            }

            getFoundInText().setText(hardLine);
            
            remove(getComponentLabel());
            remove(getComponentText());
            remove(getPropertyLabel());
            remove(getPropertyText());
        }
    } // End of JavaInfoPanel inner class.
    
    
    /** Factory for {@code JavaI18nSupport}. */
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.i18n.I18nSupport.Factory.class)
    public static class Factory extends I18nSupport.Factory {
        
        /** Implements interface. */
        public I18nSupport createI18nSupport(DataObject dataObject) {
            return new JavaI18nSupport(dataObject);
        }

        /** Gets class of supported <code>DataObject</code>.
         * @return <code>JavaDataObject</code> class or <code>null</code> 
         * if java module is not available */
        public Class getDataObjectClass() {
            // XXX Cleaner should be this code dependend on java module
            // -> I18n API needed.
            try {
                return Class.forName(
                    "org.netbeans.modules.java.JavaDataObject", // NOI18N
                    false,
                    Lookup.getDefault().lookup(ClassLoader.class));
            } catch (ClassNotFoundException cnfe) {
                return null;
            }
        }
    } // End of class Factory.
}
