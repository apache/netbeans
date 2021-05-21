<?php

if ($a > $b):
echo $a . " is greater than " . $b;
echo $a . " equals " . $b;
else:
echo $a . " is neither greater than or equal to " . $b;
echo 2;
endif;


while ($i <= 10):
echo $i;
$i++;
endwhile;

for ($i = 1; ; $i++):
if ($i > 10)
{
break;
}
echo $i;
endfor;

foreach ($a as $v):
echo "\$a[$i] => $v.\n";
$i++;
endforeach;


switch ($i):
case 0:
echo "i equals 0";
break;
case 1:
echo "i equals 1";
break;
case 2:
echo "i equals 2";
break;
default:
echo "i is not equal to 0, 1 or 2";
endswitch;

?>