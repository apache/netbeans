/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.validation;

import java.net.URL;
import java.util.*;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.validation.ValidationContext;
import org.netbeans.modules.html.editor.lib.api.validation.ValidationException;
import org.netbeans.modules.html.editor.lib.api.validation.ValidationResult;
import org.netbeans.modules.html.editor.lib.api.validation.Validator;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service=Validator.class, position=10)
public class ValidatorImpl implements Validator {
    
    private static final Pattern TEMPLATING_MARKS_PATTERN = Pattern.compile("@@@"); //NOI18N
    private static final String TEMPLATING_MARKS_MASK = "   "; //NOI18N

    @Override
    public ValidationResult validate(ValidationContext context) throws ValidationException {
        assert canValidate(context.getVersion());
        
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            NbValidationTransaction validatorTransaction = 
                    NbValidationTransaction.create(context.getVersion()); //NOI18N

//            //simulate the "in body" mode if the code is a fragment
//            if(context.getSyntaxAnalyzerResult().getDetectedHtmlVersion() == null) {
//                validatorTransaction.setBodyFragmentContextMode(true);
//            }

            FileObject file = context.getFile();
            URL sourceFileURL = file != null ? URLMapper.findURL(file, URLMapper.EXTERNAL) : null;

            Set<String> filteredNamespaces = Collections.emptySet();
            if(context.isFeatureEnabled("filter.foreign.namespaces")) { //NOI18N
                filteredNamespaces = context.getSyntaxAnalyzerResult().getAllDeclaredNamespaces().keySet();
                filteredNamespaces.remove("http://www.w3.org/1999/xhtml"); //NOI18N
            }

            String encoding = file != null ? FileEncodingQuery.getEncoding(file).name() : "UTF-8"; //NOI18N
            
//            StringBuilder content  = new StringBuilder();
//            try {
//                Reader sourceReader = context.getSourceReader();
//                char[] b = new char[1024];
//                int read;
//                while((read = sourceReader.read(b)) > 0) {
//                    content.append(new String(b, 0, read));
//                }
//                sourceReader.reset();
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//            System.out.println("----------------------------------------");
//            System.out.println(content.toString());
//            System.out.println("----------------------------------------");
//            
//            validatorTransaction.validateCode(new StringReader(content.toString()), 
            
            validatorTransaction.validateCode(context.getSourceReader(), 
                    sourceFileURL != null ? sourceFileURL.toExternalForm() : null, 
                    filteredNamespaces, 
                    encoding);

            Collection<ProblemDescription> problems = new LinkedList<>(validatorTransaction.getFoundProblems(ProblemDescription.WARNING));
            
            if(context.getSyntaxAnalyzerResult().getDetectedHtmlVersion() == null) {
                //1. unknown doctype, the HtmlSourceVersionQuery is used
                //some of the "missing doctype" errors should be suppressed

                //2. the code might be just a fragment of code which usually belongs to the body of the
                //complete document. In such case the Error: Required children missing from element "head"
                //should be filtered as well
                filterCodeFragmentProblems(context, problems);
            }

            return new ValidationResult(this, context, problems, problems.isEmpty());

        } catch (SAXException ex) {
            throw new ValidationException(ex);
        } finally {
            //ensure the thread's context classloader hasn't been changed during the validator code execution
            if(Thread.currentThread().getContextClassLoader() != contextClassLoader) {
                Logger.getAnonymousLogger().info("Thread's context ClassLoader has been changed during the validation.nu code execution! See issue 195626 for more info"); //NOI18N
                //let's recover
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
        }

    }

    @Override
    public String getValidatorName() {
        return "validator.nu"; //NOI18N
    }

    @Override
    public boolean canValidate(HtmlVersion version) {
        switch(version) {
            case HTML32:
            case HTML40_FRAMESET:
            case HTML40_STRICT:
            case HTML40_TRANSATIONAL:
            case HTML41_FRAMESET:
            case HTML41_STRICT:
            case HTML41_TRANSATIONAL:
            case XHTML10_FRAMESET:
            case XHTML10_TRANSATIONAL:
            case XHTML10_STICT:
            case HTML5:
            case XHTML5:
                return true;
            default:
                return false;
        }
    }

    private void filterCodeFragmentProblems(ValidationContext context, Collection<ProblemDescription> problems) {
        for(Iterator<ProblemDescription> itr = problems.iterator(); itr.hasNext();) {
            ProblemDescription problem = itr.next();
            if(problem.getText().startsWith("Error: Start tag seen without seeing a doctype first.")
                    || (problem.getText().startsWith("Error: Element \"head\" is missing a required instance of child element") && !containsHeadElement(context)) ) {
                itr.remove();
            }
        }
    }

    private boolean containsHeadElement(ValidationContext context) {
        Iterator<Element> head = context.getSyntaxAnalyzerResult().getElementsIterator();
        //limit the search to the beginning of the file
        int limit = 20;
        while(head.hasNext()) {
            Element se = head.next();
            if(limit-- == 0) {
                break;
            }
            if(se.type() == ElementType.OPEN_TAG) {
                OpenTag ot = (OpenTag)se;
                if(LexerUtils.equals("head", ot.unqualifiedName(), true, true)) { //NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    static String maskTemplatingMarks(String code) {
        return TEMPLATING_MARKS_PATTERN.matcher(code).replaceAll(TEMPLATING_MARKS_MASK);
    }


}
