<?php
namespace NsFoo\NsBar {
    interface NsInterfaceName {}
}

namespace No\Ns {
    use \NsFoo\NsBar\NsInterfaceName;
    class NoNsClassName implements NsInterfaceName {}
}
?>