<p>
    <?php if ($a): ?>
        foo
    <?php else: ?>
            bar
    <?php endif ?>
        </p>

        <ol>
    <?php foreach ($a as $k => $v): ?>
                <li>foo</li>
    <?php endforeach ?>
</ol>
