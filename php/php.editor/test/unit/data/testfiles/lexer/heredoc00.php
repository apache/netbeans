<?php
$name="Petr";
$age=10;
echo <<<HEREDOC
Name: $name<br>
My age is: $age<br>
HEREDOC;
$name="Honza";

$x = <<<ENDOFHEREDOC
This is another heredoc test.
With another line in it.
ENDOFHEREDOC;


$x = <<<ENDOFHEREDOC
This is a heredoc test.
NOTREALLYEND;
Another line
NOTENDEITHER;
ENDOFHEREDOCWILLBESOON
Now let's finish it
ENDOFHEREDOC;


print "{$x}";
?>