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

package org.netbeans.modules.web.core.syntax.formatting;

import org.netbeans.editor.Syntax;
import org.netbeans.modules.web.core.syntax.*;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.editor.ext.java.JavaFormatter;
import org.netbeans.modules.web.core.syntax.deprecated.Jsp11Syntax;


/**
 * Formatter for writing java scriplet in jsp and tag files.
 * @author Petr Pisl
 */

public class JspJavaFormatter extends JavaFormatter{
	
    public JspJavaFormatter(Class kitClass) {
	super(kitClass);
    }

    @Override
    protected boolean acceptSyntax(Syntax syntax) {
        return (syntax instanceof Jsp11Syntax);
    }
    
    @Override
    protected void initFormatLayers() {

	addFormatLayer(new StripEndWhitespaceLayer());
	addFormatLayer(new JspJavaLayer());
    }

    public class JspJavaLayer extends JavaFormatter.JavaLayer {	    

        @Override
	protected FormatSupport createFormatSupport(FormatWriter fw) {
	    BaseDocument doc = (BaseDocument)fw.getDocument();
	    JspSyntaxSupport sup = new JspSyntaxSupport(doc);
	    try{
		TokenItem token = sup.getItemAtOrBefore(fw.getOffset());
		return new JspJavaFormatSupport(fw, token.getTokenContextPath());
	    }
	    catch (Exception e){
		e.printStackTrace(System.out);
	    }
	    return null;
	}
    }
}
