<?php

if (isset($_POST['submit_step_1']) && ()) { //<- there is an obvious mistake here
  echo "I am wrong condition because of empty brackets"; 
} else {
  echo "I am part of code that won't be reached because of wrong condition in if statement";
}

?>