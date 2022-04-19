package test;

public class Test {
    
    /**
     * {@snippet :
     * class HelloWorld {// @highlight substring=""
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight substring=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight substring
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight substring=
     * }
     * }
     * 
     * 
     * {@snippet :
     * class HelloWorld {// @highlight regex=""
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight regex=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight regex
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight type=""
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight type=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight type
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight type="xyz"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight type="highlighted" substring=" "
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @highlight substring= type="highlighted" 
     * }
     * }
     */
    
    public void errorsInHighlightTag(){}
    
    /**
     * {@snippet :
     * class HelloWorld {// @replace substring="" replacement="interface"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace replacement="interface"  substring=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace substring replacement="interface" 
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace regex="" replacement="interface"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace replacement="interface" regex=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace regex replacement="interface"
     * }
     * }
     * 
     * {@snippet :
     * class Helloclass {// @replace substring="class"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace regex="/bclass/b" 
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @replace substring
     * }
     * }
     * 
     * {@snippet :
     * class Helloclass {// @replace regex="\bclass\b"  replacement="interface"
     * }
     * }
     * 
     * {@snippet :
     * class HelloClass {// @replace substring="Class"  replacement=""
     * }
     * }
     */
    public void errorsInReplaceTag(){}
    
    /**
     * {@snippet :
     * class HelloWorld {// @link substring="" target="System#out"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link target="System#out"  substring=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link substring target="System#out" 
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link regex="" target="System#out"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link target="System#out" regex=
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link regex target="System#out"
     * }
     * }
     * 
     * {@snippet :
     * class Helloclass {// @link substring="class"
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link regex="/bclass/b" 
     * }
     * }
     * 
     * {@snippet :
     * class HelloWorld {// @link substring
     * }
     * }
     * 
     * {@snippet :
     * class Helloclass {// @link regex="\bclass\b"  target="System#out"
     * }
     * }
     * 
     * {@snippet :
     * class HelloClass {// @link substring="Class"  target="System#out"
     * }
     * }
     * 
     * {@snippet :
     * class Helloclass {// @link substring="Class"  target="System#out"
     * }
     * }
     * 
     * {@snippet :
     * class HelloClass {// @link substring=" "  target="System#out"
     * }
     * }
     */
    public void errorsInLinkTag(){}
    
    /**
     * {@snippet :
     * class HelloClass {// @highlight substring="Hello" region
     * }
     * }
     * 
     * {@snippet :
     * class HelloClass {// @highlight substring="Hello" region=rg1
     * }
     * }
     * 
     * {@snippet :
     * class HelloClass {// @highlight substring="Hello" region=rg1
     *      final int i = 10;// @highlight substring="int" region=rg2
     * }//@end region=rg1
     * }
     * 
     * {@snippet :
     * class HelloClass {// @highlight substring="Hello" region=rg1
     *      final int i = 10;// @highlight substring="int" region=rg2
     * }//@end region=rg2
     * }
     */
    public void errorsInUnpairedRegion(){}
    
     /**
     * {@snippet :
     * class HelloClass {// @highlight substring="Hello" 
     * }//@end
     * }
     */
    public void errorsInNoRegionToEnd(){}
    
}
