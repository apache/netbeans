<?php
//START

class ClassName {

    function __construct() {
        if (true) { //OK
            if (true) { //OK
                if (true) { //HINT
                }
            }

            while (true) {  //OK
                if (true) { //OK
                    if (true) { //HINT
                        if (true) {
                        }
                    }
                    break;
                }
            }

            while (true) { //OK
                while (true) { //HINT
                }
            }

            do { //OK
                do { //HINT
                } while (true);
            } while (true);

            foreach ($array as $value) { //OK
                if (true) { //OK
                    break;
                }
                foreach ($array as $value) { //HINT
                }
            }

            for ($i = 0; $i < count($array); $i++) { //OK
                if (true) { //OK
                    break;
                }
                for ($j = 0; $j < count($array); $j++) { //HINT
                }
            }

            if (true) { //OK
                if (true) { //HINT
                }
            } else { //OK
            }

            if (true) { //OK
            } elseif (true) { //OK
                if (true) { //HINT
                }
            } elseif (true) { //OK
            } else { //OK
            }

            if (true) { //OK
            } else if (true) { //OK
                if (true) { //HINT
                }
            } else if (true) { //OK
            } else { //OK
            }

            while (true)
                while (true) {} //HINT

            do
                while (true) {} //HINT
            while (true);

            if (true)
                if (true) {} //HINT

            if (true)
                if (true) {} //HINT
            else
                if (true) {} //HINT
        }
    }

}

//END
?>