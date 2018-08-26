<?php
//START

class DOMDocument {
    function loadHTML() {}
    function loadHTMLFile() {}
    function loadXML() {}
}

$file = @fopen("", "");

@unlink("");

$mysql = @mysql_connect();

@ob_end_clean();

@ob_end_flush();

$dom = new DOMDocument();
@$dom->loadHTML("");
@$dom->loadHTMLFile("");
@$dom->loadXML("");

@mkdir(__DIR__);

$ivString = @iconv('utf-8','cp1251','test');

//END
?>