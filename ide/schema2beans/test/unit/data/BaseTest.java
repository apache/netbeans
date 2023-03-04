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

