/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */


import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.netbeans.modules.schema2beans.BaseBean;

public class BaseTest
{
	String testName;
	String testMsg;
    String documentDir = null;

	Document doc;
	
	OutputStream out;
	
	BaseTest()
	{
		this.testName = this.getClass().getName();
		this.out = System.out;
		this.doc = null;
	}

    String getDocumentName() {
        return getClass().getName()+".xml";
    }

    String getFullDocumentName() {
        if (documentDir == null)
            return getDocumentName();
        return documentDir + getDocumentName();
    }

    void setDocumentDir(String dir) {
        documentDir = dir;
    }

	void readDocument()
	{
        this.readDocument(getDocumentName());
	}
		
	void readDocument(String name)
	{
		/*
		 *	The other way to create the object graph:
		 *
		 *	DDFactory.register("book", "book.Book");
		 *	book = DDFactory.create(in, "book");
		 *
		 */
        if (documentDir != null)
            name = documentDir + name;
		try
		{
			FileInputStream in = new FileInputStream(name);
			
			out("creating the DOM document");
			DocumentBuilderFactory dbf = 
					DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			this.doc = db.parse(in);
			in.close();
		}
		catch (Throwable t)
		{
            t.printStackTrace();
			throw new RuntimeException("DD creation failed: " +
									   t.getMessage());
		}

		if (doc == null)
		{
			err("doc is null");
			return;
		}
	}
	
	void setTest(String msg)
	{
		this.testMsg = msg;
	}

	void setOutputStream(OutputStream out)
	{
		this.out = out;
	}

    void print(String s) {
		try {
			this.out.write(s.getBytes());
		} catch(Exception e) {
			throw new RuntimeException("outputStream.write failed: " +
									   e.getMessage());
		}
    }

	void println(String s) {
        print(s);
        print("\n");
	}
	
	void err(String s)
	{
		println("### Error - " + s);
	}
	
 	void out(String s, String s2)
	{
		println(this.testName + " - " + s + "\n" + s2);
	}
	
 	void out(String s)
	{
		println(this.testName + " - " + s);
	}

 	void out(int value)
	{
		println(this.testName + " - " + value);
	}

    void out(String[] s) {
        out("{");
        for (int i = 0; i < s.length; ++i)
            out("\t"+s[i]);
        out("}");
    }

    void out(BaseBean bean) {
        bean.dumpXml();
    }

    void print(Object o) {
        if (o == null) {
            print("null");
            return;
        }
        Class cls = o.getClass();
        if (cls.isArray()) {
            print("{");
            Object[] arr = (Object[]) o;
            for (int i = 0; i < arr.length; ++i) {
                if (i > 0)
                    print(", ");
                print(arr[i]);
            }
            print("}");
        } else {
            print(""+o);
        }
    }
	
	void check(boolean success, String str)
	{
		if (success)
			println(this.testName + " " + this.testMsg +
					((str != null)?" "+str:"") + " -> OK");
		else
			println(this.testName + " " + this.testMsg +
					((str != null)?" "+str:"") + " -> FAILED");
	}
	
	void check(boolean success)
	{
		this.check(success, null);
	}


    int getKMemUsage() {
	Runtime rt = Runtime.getRuntime();

	try { 
	    rt.gc();
	    Thread.sleep(1000L);
	    rt.gc();
	    Thread.sleep(1000L);
	    rt.gc();
	    Thread.sleep(1000L);
	    rt.gc();
	} catch(Exception e) {
	}

	long used = rt.totalMemory()-rt.freeMemory();
	return (int)(used/1024L);
    }

    void printMemUsage() {
	out("Memory usage: " + getKMemUsage() + "k");
    }


	public void run()
		throws Exception
	{
		throw new RuntimeException("This run() method should be subclassed");
	}
}

