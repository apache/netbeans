<?php

?>
<!DOCTYPE html>
<html>
    <body>
        <?php if(1 === 1): ?>
            <?php \Test\TestClass1::callMe()?>
        <?php else: ?>
            <?php \Test\TestClass2::callMe()?>
        <?php endif; ?>
    </body>
</html>
