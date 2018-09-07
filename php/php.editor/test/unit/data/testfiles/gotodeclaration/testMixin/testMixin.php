<?php

namespace Mixin\A;

/**
 * @mixin MixinA2
 */
class MixinA1
{
}

class MixinA2
{
}

namespace Mixin\B;

/**
 * @mixin \Mixin\A\MixinA1
 */
class MixinB1
{
}

/**
 * @mixin \Mixin\A\MixinA2|MixinB1
 */
class MixinB2
{
}
