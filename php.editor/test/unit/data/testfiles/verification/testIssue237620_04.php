<?php

class ClassName {

    function __construct() {

    }

    public function CreatePdf() {

        foreach($this->Pages as $page)
        {
            /* @var $page Element */

            //calculate sizes
            $page->Measure();

            //arrange elements
            $page->Arrange();

            //render
            $page->Render($this->pdfWriter);
        }
    }

}

?>