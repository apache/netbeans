/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package action;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
*
* @author jprox
*/
@SuppressWarnings({"", ""})
public class WholeFile {

static {

}

public void method(int[] array) {
for (int i = 0; i < array.length; i++) {
int j = array[i];            
System.out.println(j);
}
try {
FileReader reader = new FileReader("");
    HashMap hashMap;
    Map map;
    List l;
    LinkedList ll;
    ArrayList al;
           
} catch(IOException ioe) {
ioe.printStackTrace();
} finally {
System.out.println("finally");
//close
}
}

class Inner {
public void test(int a) {
switch(a) {
case 1:
break;
case 2:
break;
}            
}
}
}
