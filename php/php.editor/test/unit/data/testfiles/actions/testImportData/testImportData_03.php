<?php

namespace Foo;

use Nette\Utils;

/**
 * Homepage presenter.
 *
 * @author     John Doe
 * @package    MyApplication
 */
class HomepagePresenter extends Presenter {

    public function renderDefault() {
        $this->template->anyVariable = 'any value';
        Strings::capitalize();
        Utils\Arrays::get($arr, $key);
        Html::el();
        new Request();
    }

}

?>