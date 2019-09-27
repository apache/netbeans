<?php
    class ClsDeclarationTest 
        extends Cls2DeclarationTest 
    {
    }
    
    class Cls2DeclarationTest 
        implements ClsBaseDeclarationTest 
    {
    }

    class Cls3DeclartionTest
        implements AnInterface
        extends AnClass
    {
    }
?>