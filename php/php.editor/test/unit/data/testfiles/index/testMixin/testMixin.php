<?php
namespace A;

/**
 * @mixin B\C3|C2
 */
class C1
{
}

class C2
{
}

namespace A\B;
class C3
{
}

namespace Mixin;

/**
 * @mixin \A\B\C3
 */
class MixinParent
{
}

/**
 * @mixin \A\C1
 */
class Mixin extends MixinParent
{
}
