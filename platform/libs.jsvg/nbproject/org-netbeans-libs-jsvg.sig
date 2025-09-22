#Signature file v4.1
#Version 2.1

CLSS public final com.github.weisj.jsvg.SVGDocument
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.SVGRenderingHints
fld public final static java.awt.RenderingHints$Key KEY_CACHE_OFFSCREEN_IMAGE
fld public final static java.awt.RenderingHints$Key KEY_IMAGE_ANTIALIASING
fld public final static java.awt.RenderingHints$Key KEY_MASK_CLIP_RENDERING
fld public final static java.awt.RenderingHints$Key KEY_SOFT_CLIPPING
fld public final static java.lang.Object VALUE_IMAGE_ANTIALIASING_OFF
fld public final static java.lang.Object VALUE_IMAGE_ANTIALIASING_ON
fld public final static java.lang.Object VALUE_MASK_CLIP_RENDERING_ACCURACY
fld public final static java.lang.Object VALUE_MASK_CLIP_RENDERING_DEFAULT
fld public final static java.lang.Object VALUE_MASK_CLIP_RENDERING_FAST
fld public final static java.lang.Object VALUE_NO_CACHE
fld public final static java.lang.Object VALUE_SOFT_CLIPPING_OFF
fld public final static java.lang.Object VALUE_SOFT_CLIPPING_ON
fld public final static java.lang.Object VALUE_USE_CACHE
supr java.lang.Object
hfds P_KEY_CACHE_OFFSCREEN_IMAGE,P_KEY_IMAGE_ANTIALIASING,P_KEY_MASK_CLIP_RENDERING,P_KEY_SOFT_CLIPPING
hcls Key,Value

CLSS public final !enum com.github.weisj.jsvg.attributes.Animatable
fld public final static com.github.weisj.jsvg.attributes.Animatable NO
fld public final static com.github.weisj.jsvg.attributes.Animatable YES
meth public static com.github.weisj.jsvg.attributes.Animatable valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.Animatable[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.Animatable>

CLSS public final com.github.weisj.jsvg.attributes.AttributeParser
supr java.lang.Object

CLSS public final !enum com.github.weisj.jsvg.attributes.ColorInterpolation
intf com.github.weisj.jsvg.attributes.HasMatchName
supr java.lang.Enum

CLSS public final com.github.weisj.jsvg.attributes.Coordinate
supr java.lang.Object

CLSS public abstract interface !annotation com.github.weisj.jsvg.attributes.Default
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation

CLSS public final !enum com.github.weisj.jsvg.attributes.FillRule
fld public final int awtWindingRule
fld public final static com.github.weisj.jsvg.attributes.FillRule EvenOdd
fld public final static com.github.weisj.jsvg.attributes.FillRule Inherit
fld public final static com.github.weisj.jsvg.attributes.FillRule Nonzero
supr java.lang.Enum

CLSS public abstract interface com.github.weisj.jsvg.attributes.HasMatchName

CLSS public final !enum com.github.weisj.jsvg.attributes.Inherited
fld public final static com.github.weisj.jsvg.attributes.Inherited NO
fld public final static com.github.weisj.jsvg.attributes.Inherited YES
meth public static com.github.weisj.jsvg.attributes.Inherited valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.Inherited[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.Inherited>

CLSS public abstract com.github.weisj.jsvg.attributes.MarkerOrientation
supr java.lang.Object

CLSS public final static !enum com.github.weisj.jsvg.attributes.MarkerOrientation$MarkerType
 outer com.github.weisj.jsvg.attributes.MarkerOrientation
fld public final static com.github.weisj.jsvg.attributes.MarkerOrientation$MarkerType END
fld public final static com.github.weisj.jsvg.attributes.MarkerOrientation$MarkerType MID
fld public final static com.github.weisj.jsvg.attributes.MarkerOrientation$MarkerType START
meth public static com.github.weisj.jsvg.attributes.MarkerOrientation$MarkerType valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.MarkerOrientation$MarkerType[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.MarkerOrientation$MarkerType>

CLSS public final !enum com.github.weisj.jsvg.attributes.MarkerUnitType
fld public final static com.github.weisj.jsvg.attributes.MarkerUnitType StrokeWidth
fld public final static com.github.weisj.jsvg.attributes.MarkerUnitType UserSpaceOnUse
meth public static com.github.weisj.jsvg.attributes.MarkerUnitType valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.MarkerUnitType[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.MarkerUnitType>

CLSS public final !enum com.github.weisj.jsvg.attributes.Overflow
fld public final static com.github.weisj.jsvg.attributes.Overflow Auto
fld public final static com.github.weisj.jsvg.attributes.Overflow Hidden
fld public final static com.github.weisj.jsvg.attributes.Overflow Scroll
fld public final static com.github.weisj.jsvg.attributes.Overflow Visible
meth public boolean establishesClip()
meth public static com.github.weisj.jsvg.attributes.Overflow valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.Overflow[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.Overflow>
hfds establishesClip

CLSS public final com.github.weisj.jsvg.attributes.PaintOrder
supr java.lang.Object

CLSS public final static !enum com.github.weisj.jsvg.attributes.PaintOrder$Phase
 outer com.github.weisj.jsvg.attributes.PaintOrder
fld public final static com.github.weisj.jsvg.attributes.PaintOrder$Phase FILL
fld public final static com.github.weisj.jsvg.attributes.PaintOrder$Phase MARKERS
fld public final static com.github.weisj.jsvg.attributes.PaintOrder$Phase STROKE
meth public static com.github.weisj.jsvg.attributes.PaintOrder$Phase valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.PaintOrder$Phase[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.PaintOrder$Phase>

CLSS public abstract interface !annotation com.github.weisj.jsvg.attributes.PresentationAttribute
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public final com.github.weisj.jsvg.attributes.PreserveAspectRatio
supr java.lang.Object

CLSS public final !enum com.github.weisj.jsvg.attributes.PreserveAspectRatio$Align
supr java.lang.Enum

CLSS public final static !enum com.github.weisj.jsvg.attributes.PreserveAspectRatio$MeetOrSlice
 outer com.github.weisj.jsvg.attributes.PreserveAspectRatio
fld public final static com.github.weisj.jsvg.attributes.PreserveAspectRatio$MeetOrSlice Meet
fld public final static com.github.weisj.jsvg.attributes.PreserveAspectRatio$MeetOrSlice Slice
meth public static com.github.weisj.jsvg.attributes.PreserveAspectRatio$MeetOrSlice valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.PreserveAspectRatio$MeetOrSlice[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.PreserveAspectRatio$MeetOrSlice>

CLSS public final !enum com.github.weisj.jsvg.attributes.SpreadMethod
fld public final static com.github.weisj.jsvg.attributes.SpreadMethod Pad
fld public final static com.github.weisj.jsvg.attributes.SpreadMethod Reflect
fld public final static com.github.weisj.jsvg.attributes.SpreadMethod Repeat
supr java.lang.Enum
hfds cycleMethod

CLSS public abstract interface com.github.weisj.jsvg.attributes.SuffixUnit

CLSS public final !enum com.github.weisj.jsvg.attributes.UnitType
fld public final static com.github.weisj.jsvg.attributes.UnitType ObjectBoundingBox
fld public final static com.github.weisj.jsvg.attributes.UnitType UserSpaceOnUse
supr java.lang.Enum

CLSS public final !enum com.github.weisj.jsvg.attributes.VectorEffect
intf com.github.weisj.jsvg.attributes.HasMatchName
supr java.lang.Enum

CLSS public final com.github.weisj.jsvg.attributes.ViewBox
supr java.awt.geom.Rectangle2D$Float

CLSS public final !enum com.github.weisj.jsvg.attributes.filter.BlendMode
intf com.github.weisj.jsvg.attributes.HasMatchName
supr java.lang.Enum

CLSS public final !enum com.github.weisj.jsvg.attributes.filter.ColorChannel
fld public final static com.github.weisj.jsvg.attributes.filter.ColorChannel A
fld public final static com.github.weisj.jsvg.attributes.filter.ColorChannel B
fld public final static com.github.weisj.jsvg.attributes.filter.ColorChannel G
fld public final static com.github.weisj.jsvg.attributes.filter.ColorChannel R
meth public int value(int)
meth public static com.github.weisj.jsvg.attributes.filter.ColorChannel valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.filter.ColorChannel[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.filter.ColorChannel>
hfds index

CLSS public final !enum com.github.weisj.jsvg.attributes.filter.CompositeMode
fld public final static com.github.weisj.jsvg.attributes.filter.CompositeMode Arithmetic
fld public final static com.github.weisj.jsvg.attributes.filter.CompositeMode Atop
fld public final static com.github.weisj.jsvg.attributes.filter.CompositeMode In
fld public final static com.github.weisj.jsvg.attributes.filter.CompositeMode Lighter
fld public final static com.github.weisj.jsvg.attributes.filter.CompositeMode Out
fld public final static com.github.weisj.jsvg.attributes.filter.CompositeMode Over
fld public final static com.github.weisj.jsvg.attributes.filter.CompositeMode Xor
meth public static com.github.weisj.jsvg.attributes.filter.CompositeMode valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.filter.CompositeMode[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.filter.CompositeMode>

CLSS public final !enum com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel
fld public final static com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel BackgroundAlpha
fld public final static com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel BackgroundImage
fld public final static com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel FillPaint
fld public final static com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel LastResult
fld public final static com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel SourceAlpha
fld public final static com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel SourceGraphic
fld public final static com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel StrokePaint
intf com.github.weisj.jsvg.attributes.filter.FilterChannelKey
supr java.lang.Enum

CLSS public abstract !enum com.github.weisj.jsvg.attributes.filter.EdgeMode
fld public final static com.github.weisj.jsvg.attributes.filter.EdgeMode Duplicate
fld public final static com.github.weisj.jsvg.attributes.filter.EdgeMode None
fld public final static com.github.weisj.jsvg.attributes.filter.EdgeMode Wrap
supr java.lang.Enum

CLSS public abstract interface com.github.weisj.jsvg.attributes.filter.EdgeMode$ConvolveOperation

CLSS public abstract interface com.github.weisj.jsvg.attributes.filter.FilterChannelKey

CLSS public com.github.weisj.jsvg.attributes.filter.FilterChannelKey$StringKey
intf com.github.weisj.jsvg.attributes.filter.FilterChannelKey
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.attributes.filter.LayoutBounds
supr java.lang.Object

CLSS public com.github.weisj.jsvg.attributes.filter.LayoutBounds$ComputeFlags
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.attributes.filter.LayoutBounds$Data
supr java.lang.Object

CLSS public final !enum com.github.weisj.jsvg.attributes.filter.TransferFunctionType
fld public final static com.github.weisj.jsvg.attributes.filter.TransferFunctionType Discrete
fld public final static com.github.weisj.jsvg.attributes.filter.TransferFunctionType Gamma
fld public final static com.github.weisj.jsvg.attributes.filter.TransferFunctionType Identity
fld public final static com.github.weisj.jsvg.attributes.filter.TransferFunctionType Linear
fld public final static com.github.weisj.jsvg.attributes.filter.TransferFunctionType Table
meth public static com.github.weisj.jsvg.attributes.filter.TransferFunctionType valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.filter.TransferFunctionType[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.filter.TransferFunctionType>

CLSS public final com.github.weisj.jsvg.attributes.font.AWTSVGFont
intf com.github.weisj.jsvg.attributes.font.SVGFont
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.attributes.font.AttributeFontSpec
intf com.github.weisj.jsvg.nodes.prototype.Mutator
supr com.github.weisj.jsvg.attributes.font.FontSpec

CLSS public final com.github.weisj.jsvg.attributes.font.FontParser
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.attributes.font.FontResolver
supr java.lang.Object

CLSS public abstract interface com.github.weisj.jsvg.attributes.font.FontSize

CLSS public com.github.weisj.jsvg.attributes.font.FontSpec
supr java.lang.Object

CLSS public final !enum com.github.weisj.jsvg.attributes.font.FontStretch
intf com.github.weisj.jsvg.attributes.HasMatchName
supr java.lang.Enum

CLSS public final com.github.weisj.jsvg.attributes.font.LengthFontSize
intf com.github.weisj.jsvg.attributes.font.FontSize
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.attributes.font.MeasurableFontSpec
supr com.github.weisj.jsvg.attributes.font.FontSpec

CLSS public final com.github.weisj.jsvg.attributes.font.NumberFontWeight
cons public init(float)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public int weight(int)
meth public java.lang.String toString()
supr java.lang.Object
hfds weight

CLSS public final !enum com.github.weisj.jsvg.attributes.font.PredefinedFontSize
intf com.github.weisj.jsvg.attributes.HasMatchName
intf com.github.weisj.jsvg.attributes.font.FontSize
supr java.lang.Enum

CLSS public abstract !enum com.github.weisj.jsvg.attributes.font.PredefinedFontWeight
fld public final static com.github.weisj.jsvg.attributes.font.PredefinedFontWeight Bold
fld public final static com.github.weisj.jsvg.attributes.font.PredefinedFontWeight Bolder
fld public final static com.github.weisj.jsvg.attributes.font.PredefinedFontWeight Lighter
fld public final static com.github.weisj.jsvg.attributes.font.PredefinedFontWeight Normal
fld public final static com.github.weisj.jsvg.attributes.font.PredefinedFontWeight Number
fld public final static int BOLD_WEIGHT = 700
fld public final static int NORMAL_WEIGHT = 400
meth public abstract int weight(int)
meth public static com.github.weisj.jsvg.attributes.font.PredefinedFontWeight valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.font.PredefinedFontWeight[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.font.PredefinedFontWeight>

CLSS public abstract interface com.github.weisj.jsvg.attributes.font.SVGFont

CLSS public final com.github.weisj.jsvg.attributes.paint.AwtSVGPaint
intf com.github.weisj.jsvg.attributes.paint.SimplePaintSVGPaint
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.attributes.paint.DefaultPaintParser
intf com.github.weisj.jsvg.attributes.paint.PaintParser
supr java.lang.Object
hfds LOGGER

CLSS public abstract interface com.github.weisj.jsvg.attributes.paint.PaintParser
fld public final static java.awt.Color DEFAULT_COLOR

CLSS public final com.github.weisj.jsvg.attributes.paint.PredefinedPaints
fld public final static com.github.weisj.jsvg.attributes.paint.AwtSVGPaint DEFAULT_PAINT
fld public final static com.github.weisj.jsvg.attributes.paint.SVGPaint CONTEXT_FILL
fld public final static com.github.weisj.jsvg.attributes.paint.SVGPaint CONTEXT_STROKE
fld public final static com.github.weisj.jsvg.attributes.paint.SVGPaint CURRENT_COLOR
fld public final static com.github.weisj.jsvg.attributes.paint.SVGPaint INHERITED
fld public final static com.github.weisj.jsvg.attributes.paint.SVGPaint NONE
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.attributes.paint.RGBColor
fld public final static com.github.weisj.jsvg.attributes.paint.RGBColor DEFAULT
fld public final static com.github.weisj.jsvg.attributes.paint.RGBColor INHERITED
intf com.github.weisj.jsvg.attributes.value.ColorValue
intf java.awt.Paint
supr java.lang.Object
hfds a,b,color,g,r

CLSS public abstract interface com.github.weisj.jsvg.attributes.paint.SVGPaint

CLSS public abstract interface com.github.weisj.jsvg.attributes.paint.SimplePaintSVGPaint
intf com.github.weisj.jsvg.attributes.paint.SVGPaint

CLSS public final !enum com.github.weisj.jsvg.attributes.stroke.LineCap
fld public final static com.github.weisj.jsvg.attributes.stroke.LineCap Butt
fld public final static com.github.weisj.jsvg.attributes.stroke.LineCap Round
fld public final static com.github.weisj.jsvg.attributes.stroke.LineCap Square
meth public int awtCode()
meth public static com.github.weisj.jsvg.attributes.stroke.LineCap valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.stroke.LineCap[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.stroke.LineCap>
hfds awtCode

CLSS public final !enum com.github.weisj.jsvg.attributes.stroke.LineJoin
fld public final static com.github.weisj.jsvg.attributes.stroke.LineJoin Bevel
fld public final static com.github.weisj.jsvg.attributes.stroke.LineJoin Miter
fld public final static com.github.weisj.jsvg.attributes.stroke.LineJoin Round
meth public int awtCode()
meth public static com.github.weisj.jsvg.attributes.stroke.LineJoin valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.stroke.LineJoin[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.stroke.LineJoin>
hfds awtCode

CLSS public final com.github.weisj.jsvg.attributes.stroke.StrokeResolver
supr java.lang.Object

CLSS public final !enum com.github.weisj.jsvg.attributes.text.DominantBaseline
intf com.github.weisj.jsvg.attributes.HasMatchName
supr java.lang.Enum

CLSS public final !enum com.github.weisj.jsvg.attributes.text.GlyphRenderMethod
fld public final static com.github.weisj.jsvg.attributes.text.GlyphRenderMethod Align
fld public final static com.github.weisj.jsvg.attributes.text.GlyphRenderMethod Stretch
meth public static com.github.weisj.jsvg.attributes.text.GlyphRenderMethod valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.text.GlyphRenderMethod[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.text.GlyphRenderMethod>

CLSS public final !enum com.github.weisj.jsvg.attributes.text.LengthAdjust
fld public final static com.github.weisj.jsvg.attributes.text.LengthAdjust Spacing
fld public final static com.github.weisj.jsvg.attributes.text.LengthAdjust SpacingAndGlyphs
meth public static com.github.weisj.jsvg.attributes.text.LengthAdjust valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.text.LengthAdjust[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.text.LengthAdjust>

CLSS public final !enum com.github.weisj.jsvg.attributes.text.Side
fld public final static com.github.weisj.jsvg.attributes.text.Side Left
fld public final static com.github.weisj.jsvg.attributes.text.Side Right
meth public static com.github.weisj.jsvg.attributes.text.Side valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.text.Side[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.text.Side>

CLSS public final !enum com.github.weisj.jsvg.attributes.text.Spacing
fld public final static com.github.weisj.jsvg.attributes.text.Spacing Auto
fld public final static com.github.weisj.jsvg.attributes.text.Spacing Exact
meth public static com.github.weisj.jsvg.attributes.text.Spacing valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.text.Spacing[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.text.Spacing>

CLSS public final !enum com.github.weisj.jsvg.attributes.text.TextAnchor
fld public final static com.github.weisj.jsvg.attributes.text.TextAnchor End
fld public final static com.github.weisj.jsvg.attributes.text.TextAnchor Middle
fld public final static com.github.weisj.jsvg.attributes.text.TextAnchor Start
meth public static com.github.weisj.jsvg.attributes.text.TextAnchor valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.attributes.text.TextAnchor[] values()
supr java.lang.Enum<com.github.weisj.jsvg.attributes.text.TextAnchor>

CLSS public abstract interface com.github.weisj.jsvg.attributes.value.ColorValue

CLSS public abstract interface com.github.weisj.jsvg.attributes.value.LengthValue

CLSS public abstract interface com.github.weisj.jsvg.attributes.value.PercentageValue

CLSS public com.github.weisj.jsvg.geometry.AWTSVGShape
intf com.github.weisj.jsvg.geometry.MeasurableShape
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.geometry.FillRuleAwareAWTSVGShape
supr com.github.weisj.jsvg.geometry.AWTSVGShape

CLSS public abstract interface com.github.weisj.jsvg.geometry.MeasurableLength

CLSS public abstract interface com.github.weisj.jsvg.geometry.MeasurableShape
intf com.github.weisj.jsvg.geometry.MeasurableLength
intf com.github.weisj.jsvg.geometry.SVGShape

CLSS public final com.github.weisj.jsvg.geometry.SVGCircle
intf com.github.weisj.jsvg.geometry.MeasurableShape
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.geometry.SVGEllipse
intf com.github.weisj.jsvg.geometry.MeasurableShape
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.geometry.SVGLine
intf com.github.weisj.jsvg.geometry.MeasurableShape
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.geometry.SVGRectangle
intf com.github.weisj.jsvg.geometry.MeasurableShape
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.geometry.SVGRoundRectangle
intf com.github.weisj.jsvg.geometry.MeasurableShape
supr java.lang.Object

CLSS public abstract interface com.github.weisj.jsvg.geometry.SVGShape

CLSS public com.github.weisj.jsvg.geometry.mesh.Bezier
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.geometry.mesh.CoonPatch
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.geometry.mesh.CoonValues
cons public init(java.awt.geom.Point2D$Float,java.awt.geom.Point2D$Float,java.awt.geom.Point2D$Float,java.awt.geom.Point2D$Float)
fld public final java.awt.geom.Point2D$Float east
fld public final java.awt.geom.Point2D$Float north
fld public final java.awt.geom.Point2D$Float south
fld public final java.awt.geom.Point2D$Float west
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.geometry.mesh.Subdivided<%0 extends java.lang.Object>
cons public init({com.github.weisj.jsvg.geometry.mesh.Subdivided%0},{com.github.weisj.jsvg.geometry.mesh.Subdivided%0},{com.github.weisj.jsvg.geometry.mesh.Subdivided%0},{com.github.weisj.jsvg.geometry.mesh.Subdivided%0})
fld public final {com.github.weisj.jsvg.geometry.mesh.Subdivided%0} northEast
fld public final {com.github.weisj.jsvg.geometry.mesh.Subdivided%0} northWest
fld public final {com.github.weisj.jsvg.geometry.mesh.Subdivided%0} southEast
fld public final {com.github.weisj.jsvg.geometry.mesh.Subdivided%0} southWest
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.geometry.noise.PerlinTurbulence
supr java.lang.Object
hfds BM,BSize,PerlinN,RAND_a,RAND_m,RAND_q,RAND_r,fGradient,numOctaves,uLatticeSelector,xFrequency,yFrequency

CLSS public final static com.github.weisj.jsvg.geometry.noise.PerlinTurbulence$StitchInfo
 outer com.github.weisj.jsvg.geometry.noise.PerlinTurbulence
cons public init()
supr java.lang.Object
hfds height,width,wrapX,wrapY

CLSS public abstract interface com.github.weisj.jsvg.geometry.path.BezierPathCommand

CLSS public final com.github.weisj.jsvg.geometry.path.BuildHistory
supr java.lang.Object

CLSS public abstract com.github.weisj.jsvg.geometry.path.PathCommand
supr java.lang.Object
hfds isRelative,nodeCount

CLSS public final com.github.weisj.jsvg.geometry.path.PathParser
supr com.github.weisj.jsvg.util.ParserBase
hfds LOGGER,currentCommand

CLSS public final com.github.weisj.jsvg.geometry.path.Terminal
supr com.github.weisj.jsvg.geometry.path.PathCommand

CLSS public final com.github.weisj.jsvg.geometry.size.Angle
supr java.lang.Object

CLSS public final !enum com.github.weisj.jsvg.geometry.size.AngleUnit
supr java.lang.Enum

CLSS public final com.github.weisj.jsvg.geometry.size.FloatInsets
cons public init()
cons public init(float,float,float,float)
meth public boolean equals(java.lang.Object)
meth public float bottom()
meth public float left()
meth public float right()
meth public float top()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds bottom,left,right,top

CLSS public final com.github.weisj.jsvg.geometry.size.FloatSize
fld public float height
fld public float width
supr java.awt.geom.Dimension2D

CLSS public final com.github.weisj.jsvg.geometry.size.Length
intf com.github.weisj.jsvg.attributes.value.LengthValue
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.geometry.size.MeasureContext
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.geometry.size.Percentage
intf com.github.weisj.jsvg.attributes.value.PercentageValue
intf java.lang.Comparable
supr java.lang.Object

CLSS public final !enum com.github.weisj.jsvg.geometry.size.Unit
intf com.github.weisj.jsvg.attributes.SuffixUnit
supr java.lang.Enum

CLSS public final com.github.weisj.jsvg.geometry.util.GeometryUtil
supr java.lang.Object
hfds EPS

CLSS public final static !enum com.github.weisj.jsvg.geometry.util.GeometryUtil$Space
 outer com.github.weisj.jsvg.geometry.util.GeometryUtil
fld public final static com.github.weisj.jsvg.geometry.util.GeometryUtil$Space DEVICE
fld public final static com.github.weisj.jsvg.geometry.util.GeometryUtil$Space ROOT
fld public final static com.github.weisj.jsvg.geometry.util.GeometryUtil$Space USER
meth public static com.github.weisj.jsvg.geometry.util.GeometryUtil$Space valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.geometry.util.GeometryUtil$Space[] values()
supr java.lang.Enum<com.github.weisj.jsvg.geometry.util.GeometryUtil$Space>

CLSS public final com.github.weisj.jsvg.geometry.util.PathLengthCalculator
cons public init()
meth public double segmentLength(int,double[])
supr java.lang.Object
hfds x,xStart,y,yStart

CLSS public final com.github.weisj.jsvg.geometry.util.ReversePathIterator
cons public init(java.awt.geom.PathIterator)
cons public init(java.awt.geom.PathIterator,int)
intf java.awt.geom.PathIterator
meth public boolean isDone()
meth public int currentSegment(double[])
meth public int currentSegment(float[])
meth public int getWindingRule()
meth public void next()
supr java.lang.Object
hfds coordIndex,coordinates,segmentIndex,segmentTypes,windingRule

CLSS public final com.github.weisj.jsvg.geometry.util.SegmentIteratorWithLookBehind
supr java.lang.Object

CLSS public final static com.github.weisj.jsvg.geometry.util.SegmentIteratorWithLookBehind$Segment
 outer com.github.weisj.jsvg.geometry.util.SegmentIteratorWithLookBehind
fld public boolean moveHappened
fld public float xEnd
fld public float xStart
fld public float yEnd
fld public float yStart
meth public double length()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract com.github.weisj.jsvg.nodes.AbstractPolyShape
supr com.github.weisj.jsvg.nodes.ShapeNode

CLSS public abstract com.github.weisj.jsvg.nodes.AbstractSVGNode
intf com.github.weisj.jsvg.nodes.SVGNode
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.nodes.Anchor
fld public final static java.lang.String TAG = "a"
intf com.github.weisj.jsvg.nodes.prototype.ShapedContainer
supr com.github.weisj.jsvg.nodes.container.CommonRenderableContainerNode

CLSS public final com.github.weisj.jsvg.nodes.Circle
fld public final static java.lang.String TAG = "circle"
supr com.github.weisj.jsvg.nodes.ShapeNode

CLSS public final com.github.weisj.jsvg.nodes.ClipPath
fld public final static java.lang.String TAG = "clippath"
intf com.github.weisj.jsvg.nodes.prototype.ShapedContainer
supr com.github.weisj.jsvg.nodes.container.ContainerNode
hfds DEBUG,clipPathUnits,isValid,surfaceSupplier

CLSS public final com.github.weisj.jsvg.nodes.Defs
fld public final static java.lang.String TAG = "defs"
supr com.github.weisj.jsvg.nodes.container.ContainerNode

CLSS public final com.github.weisj.jsvg.nodes.Desc
fld public final static java.lang.String TAG = "desc"
supr com.github.weisj.jsvg.nodes.MetaSVGNode

CLSS public final com.github.weisj.jsvg.nodes.Ellipse
fld public final static java.lang.String TAG = "ellipse"
supr com.github.weisj.jsvg.nodes.ShapeNode

CLSS public final com.github.weisj.jsvg.nodes.Group
fld public final static java.lang.String TAG = "g"
intf com.github.weisj.jsvg.nodes.prototype.ShapedContainer
supr com.github.weisj.jsvg.nodes.container.CommonRenderableContainerNode

CLSS public final com.github.weisj.jsvg.nodes.Image
fld public final static java.lang.String TAG = "image"
supr com.github.weisj.jsvg.nodes.RenderableSVGNode
hfds LOGGER,height,imgResource,overflow,preserveAspectRatio,width,x,y

CLSS public final com.github.weisj.jsvg.nodes.Line
fld public final static java.lang.String TAG = "line"
supr com.github.weisj.jsvg.nodes.ShapeNode

CLSS public final com.github.weisj.jsvg.nodes.LinearGradient
fld public final static java.lang.String TAG = "lineargradient"
intf com.github.weisj.jsvg.attributes.paint.SVGPaint
supr com.github.weisj.jsvg.nodes.container.ContainerNode
hfds x1,x2,y1,y2

CLSS public final com.github.weisj.jsvg.nodes.Marker
fld public final static java.lang.String TAG = "marker"
supr com.github.weisj.jsvg.nodes.container.BaseInnerViewContainer
hfds markerHeight,markerUnits,markerWidth,orientation,refX,refY

CLSS public final com.github.weisj.jsvg.nodes.Mask
fld public final static java.lang.String TAG = "mask"
intf com.github.weisj.jsvg.nodes.prototype.Instantiator
supr com.github.weisj.jsvg.nodes.container.CommonRenderableContainerNode
hfds DEBUG,height,maskContentUnits,maskUnits,surfaceSupplier,width,x,y

CLSS public abstract com.github.weisj.jsvg.nodes.MetaSVGNode
intf com.github.weisj.jsvg.nodes.SVGNode
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.nodes.Metadata
fld public final static java.lang.String TAG = "metadata"
supr com.github.weisj.jsvg.nodes.MetaSVGNode

CLSS public final com.github.weisj.jsvg.nodes.Path
fld public final static java.lang.String TAG = "path"
supr com.github.weisj.jsvg.nodes.ShapeNode

CLSS public final com.github.weisj.jsvg.nodes.Pattern
fld public final static java.lang.String TAG = "pattern"
intf com.github.weisj.jsvg.attributes.paint.SVGPaint
intf com.github.weisj.jsvg.nodes.prototype.Instantiator
intf com.github.weisj.jsvg.nodes.prototype.ShapedContainer
supr com.github.weisj.jsvg.nodes.container.BaseInnerViewContainer
hfds height,patternContentUnits,patternTransform,patternUnits,width,x,y

CLSS public final com.github.weisj.jsvg.nodes.Polygon
fld public final static java.lang.String TAG = "polygon"
supr com.github.weisj.jsvg.nodes.AbstractPolyShape

CLSS public final com.github.weisj.jsvg.nodes.Polyline
fld public final static java.lang.String TAG = "polyline"
supr com.github.weisj.jsvg.nodes.AbstractPolyShape

CLSS public final com.github.weisj.jsvg.nodes.RadialGradient
fld public final static java.lang.String TAG = "radialgradient"
intf com.github.weisj.jsvg.attributes.paint.SVGPaint
supr com.github.weisj.jsvg.nodes.container.ContainerNode
hfds cx,cy,fr,fx,fy,r

CLSS public final com.github.weisj.jsvg.nodes.Rect
fld public final static java.lang.String TAG = "rect"
supr com.github.weisj.jsvg.nodes.ShapeNode

CLSS public abstract com.github.weisj.jsvg.nodes.RenderableSVGNode
intf com.github.weisj.jsvg.nodes.prototype.HasGeometryContext$ByDelegate
intf com.github.weisj.jsvg.nodes.prototype.Renderable
supr com.github.weisj.jsvg.nodes.AbstractSVGNode
hfds geometryContext,isVisible

CLSS public final com.github.weisj.jsvg.nodes.SVG
supr com.github.weisj.jsvg.nodes.container.CommonInnerViewContainer

CLSS public abstract interface com.github.weisj.jsvg.nodes.SVGNode

CLSS public abstract com.github.weisj.jsvg.nodes.ShapeNode
intf com.github.weisj.jsvg.nodes.prototype.HasFontContext
intf com.github.weisj.jsvg.nodes.prototype.HasPaintContext
intf com.github.weisj.jsvg.nodes.prototype.HasShape
intf com.github.weisj.jsvg.nodes.prototype.HasVectorEffects
intf com.github.weisj.jsvg.nodes.prototype.Instantiator
supr com.github.weisj.jsvg.nodes.RenderableSVGNode
hfds fontSize,fontSizeAdjust,markerEnd,markerMid,markerStart,paintContext,pathLength,shape,vectorEffects

CLSS public final com.github.weisj.jsvg.nodes.SolidColor
fld public final static java.lang.String TAG = "solidcolor"
intf com.github.weisj.jsvg.attributes.paint.SimplePaintSVGPaint
supr com.github.weisj.jsvg.nodes.AbstractSVGNode
hfds color

CLSS public final com.github.weisj.jsvg.nodes.Stop
supr com.github.weisj.jsvg.nodes.AbstractSVGNode

CLSS public final com.github.weisj.jsvg.nodes.Style
fld public final static java.lang.String TAG = "style"
supr com.github.weisj.jsvg.nodes.MetaSVGNode
hfds data,styleSheet

CLSS public final com.github.weisj.jsvg.nodes.Symbol
fld public final static java.lang.String TAG = "symbol"
supr com.github.weisj.jsvg.nodes.container.CommonInnerViewContainer
hfds refX,refY

CLSS public final com.github.weisj.jsvg.nodes.Title
fld public final static java.lang.String TAG = "title"
supr com.github.weisj.jsvg.nodes.MetaSVGNode

CLSS public final com.github.weisj.jsvg.nodes.Use
intf com.github.weisj.jsvg.nodes.prototype.HasContext
intf com.github.weisj.jsvg.nodes.prototype.HasShape
intf com.github.weisj.jsvg.nodes.prototype.Instantiator
supr com.github.weisj.jsvg.nodes.RenderableSVGNode

CLSS public final com.github.weisj.jsvg.nodes.View
fld public final static java.lang.String TAG = "view"
supr com.github.weisj.jsvg.nodes.MetaSVGNode

CLSS public final com.github.weisj.jsvg.nodes.animation.Animate
fld public final static java.lang.String TAG = "animate"
supr com.github.weisj.jsvg.nodes.animation.BaseAnimationNode

CLSS public final com.github.weisj.jsvg.nodes.animation.AnimateTransform
fld public final static java.lang.String TAG = "animatetransform"
supr com.github.weisj.jsvg.nodes.animation.BaseAnimationNode
hfds type

CLSS public abstract com.github.weisj.jsvg.nodes.animation.BaseAnimationNode
supr com.github.weisj.jsvg.nodes.AbstractSVGNode

CLSS public final com.github.weisj.jsvg.nodes.animation.Set
fld public final static java.lang.String TAG = "set"
supr com.github.weisj.jsvg.nodes.MetaSVGNode

CLSS public abstract com.github.weisj.jsvg.nodes.container.BaseContainerNode
intf com.github.weisj.jsvg.nodes.prototype.Container
supr com.github.weisj.jsvg.nodes.AbstractSVGNode
hfds EXHAUSTIVE_CHECK,LOGGER

CLSS public abstract com.github.weisj.jsvg.nodes.container.BaseInnerViewContainer
fld protected com.github.weisj.jsvg.attributes.PreserveAspectRatio preserveAspectRatio
fld protected com.github.weisj.jsvg.attributes.ViewBox viewBox
supr com.github.weisj.jsvg.nodes.container.CommonRenderableContainerNode
hfds overflow

CLSS public abstract com.github.weisj.jsvg.nodes.container.CommonInnerViewContainer
fld protected com.github.weisj.jsvg.geometry.size.Length height
fld protected com.github.weisj.jsvg.geometry.size.Length width
fld protected com.github.weisj.jsvg.geometry.size.Length x
fld protected com.github.weisj.jsvg.geometry.size.Length y
intf com.github.weisj.jsvg.nodes.prototype.ShapedContainer
supr com.github.weisj.jsvg.nodes.container.BaseInnerViewContainer

CLSS public abstract com.github.weisj.jsvg.nodes.container.CommonRenderableContainerNode
intf com.github.weisj.jsvg.nodes.prototype.HasContext$ByDelegate
intf com.github.weisj.jsvg.nodes.prototype.HasGeometryContext$ByDelegate
intf com.github.weisj.jsvg.nodes.prototype.Renderable
supr com.github.weisj.jsvg.nodes.container.BaseContainerNode

CLSS public abstract com.github.weisj.jsvg.nodes.container.ContainerNode
supr com.github.weisj.jsvg.nodes.container.BaseContainerNode

CLSS public abstract com.github.weisj.jsvg.nodes.filter.AbstractBlendComposite
intf java.awt.Composite
supr java.lang.Object
hfds convertToLinearRGB

CLSS public abstract interface com.github.weisj.jsvg.nodes.filter.AbstractBlendComposite$Blender

CLSS public abstract com.github.weisj.jsvg.nodes.filter.AbstractFilterPrimitive
intf com.github.weisj.jsvg.nodes.filter.FilterPrimitive
supr com.github.weisj.jsvg.nodes.AbstractSVGNode
hfds filterPrimitiveBase

CLSS public final com.github.weisj.jsvg.nodes.filter.BlendModeComposite
supr com.github.weisj.jsvg.nodes.filter.AbstractBlendComposite

CLSS public abstract interface com.github.weisj.jsvg.nodes.filter.Channel

CLSS public final com.github.weisj.jsvg.nodes.filter.ChannelStorage
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.nodes.filter.CompositeModeComposite
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.nodes.filter.DummyFilterPrimitive
supr com.github.weisj.jsvg.nodes.filter.AbstractFilterPrimitive

CLSS public final com.github.weisj.jsvg.nodes.filter.FeBlend
fld public final static java.lang.String TAG = "feblend"
supr com.github.weisj.jsvg.nodes.filter.AbstractFilterPrimitive
hfds composite

CLSS public final com.github.weisj.jsvg.nodes.filter.FeColorMatrix
supr com.github.weisj.jsvg.nodes.filter.AbstractFilterPrimitive

CLSS public final static com.github.weisj.jsvg.nodes.filter.FeColorMatrix$LuminanceToAlphaFilter
 outer com.github.weisj.jsvg.nodes.filter.FeColorMatrix
cons public init()
meth public int filterRGB(int,int,int)
supr com.github.weisj.jsvg.util.ColorSpaceAwareRGBImageFilter

CLSS public com.github.weisj.jsvg.nodes.filter.FeComponentTransfer
fld public final static java.lang.String TAG = "fecomponenttransfer"
intf com.github.weisj.jsvg.nodes.filter.FilterPrimitive
supr com.github.weisj.jsvg.nodes.container.ContainerNode
hfds filterPrimitiveBase,linearRGBlookupTable,sRGBlookupTable

CLSS public final com.github.weisj.jsvg.nodes.filter.FeComposite
fld public final static java.lang.String TAG = "fecomposite"
supr com.github.weisj.jsvg.nodes.filter.AbstractFilterPrimitive
hfds composite

CLSS public final com.github.weisj.jsvg.nodes.filter.FeDisplacementMap
fld public final static java.lang.String TAG = "fedisplacementmap"
supr com.github.weisj.jsvg.nodes.filter.AbstractFilterPrimitive
hfds inputChannel2,scale,xChannelSelector,yChannelSelector

CLSS public final com.github.weisj.jsvg.nodes.filter.FeDropShadow
fld protected final com.github.weisj.jsvg.attributes.filter.FilterChannelKey outerLastResult
fld public final static java.lang.String TAG = "fedropshadow"
intf com.github.weisj.jsvg.nodes.filter.FilterPrimitive
supr com.github.weisj.jsvg.nodes.filter.AbstractFilterPrimitive
hfds primitives

CLSS public final com.github.weisj.jsvg.nodes.filter.FeFlood
fld public final static java.lang.String TAG = "feflood"
supr com.github.weisj.jsvg.nodes.filter.AbstractFilterPrimitive
hfds floodColor,floodOpacity

CLSS public final com.github.weisj.jsvg.nodes.filter.FeGaussianBlur
fld public final static java.lang.String TAG = "fegaussianblur"
supr com.github.weisj.jsvg.nodes.filter.AbstractFilterPrimitive
hfds BOX_BLUR_APPROXIMATION_THRESHOLD,KERNEL_PRECISION,SQRT_2_PI,THREE_QUARTER_SQRT_2_PI,edgeMode,onlyAlpha,stdDeviation,xBlur,xCurrent,yBlur,yCurrent

CLSS public final com.github.weisj.jsvg.nodes.filter.FeMerge
fld public final static java.lang.String TAG = "feMerge"
intf com.github.weisj.jsvg.nodes.filter.FilterPrimitive
supr com.github.weisj.jsvg.nodes.container.ContainerNode
hfds filterPrimitiveBase,inputChannels

CLSS public final com.github.weisj.jsvg.nodes.filter.FeMergeNode
fld public final static java.lang.String TAG = "feMergeNode"
supr com.github.weisj.jsvg.nodes.AbstractSVGNode
hfds inputChannel

CLSS public final com.github.weisj.jsvg.nodes.filter.FeOffset
fld public final static java.lang.String TAG = "feOffset"
supr com.github.weisj.jsvg.nodes.filter.AbstractFilterPrimitive
hfds dx,dy

CLSS public final com.github.weisj.jsvg.nodes.filter.FeTurbulence
fld public final static java.lang.String TAG = "feturbulence"
supr com.github.weisj.jsvg.nodes.filter.AbstractFilterPrimitive
hfds baseFrequency,numOctaves,seed,type

CLSS public final com.github.weisj.jsvg.nodes.filter.FeTurbulence$TurbulenceChannel
intf com.github.weisj.jsvg.nodes.filter.Channel
intf com.github.weisj.jsvg.nodes.filter.PixelProvider
supr java.lang.Object
hfds bufferedImage,channels,imageHeight,imageWidth,perlinTurbulence,tileBounds,type

CLSS public final static !enum com.github.weisj.jsvg.nodes.filter.FeTurbulence$Type
 outer com.github.weisj.jsvg.nodes.filter.FeTurbulence
fld public final static com.github.weisj.jsvg.nodes.filter.FeTurbulence$Type Turbulence
fld public final static com.github.weisj.jsvg.nodes.filter.FeTurbulence$Type fractalNoise
meth public static com.github.weisj.jsvg.nodes.filter.FeTurbulence$Type valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.nodes.filter.FeTurbulence$Type[] values()
supr java.lang.Enum<com.github.weisj.jsvg.nodes.filter.FeTurbulence$Type>

CLSS public final com.github.weisj.jsvg.nodes.filter.Filter
fld public final static java.lang.String TAG = "filter"
supr com.github.weisj.jsvg.nodes.container.ContainerNode
hfds DEFAULT_FILTER_COORDINATE_X,DEFAULT_FILTER_COORDINATE_Y,DEFAULT_FILTER_HEIGHT,DEFAULT_FILTER_WIDTH,LOGGER,NO_CLIP_BOUNDS,colorInterpolation,filterPrimitiveUnits,filterUnits,height,isValid,width,x,y

CLSS public final com.github.weisj.jsvg.nodes.filter.Filter$FilterBounds
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.nodes.filter.Filter$FilterInfo
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.nodes.filter.FilterContext
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.nodes.filter.FilterLayoutContext
supr java.lang.Object

CLSS public abstract interface com.github.weisj.jsvg.nodes.filter.FilterPrimitive

CLSS public final com.github.weisj.jsvg.nodes.filter.FilterPrimitiveBase
supr java.lang.Object

CLSS public com.github.weisj.jsvg.nodes.filter.ImageProducerChannel
intf com.github.weisj.jsvg.nodes.filter.Channel
intf com.github.weisj.jsvg.nodes.filter.PixelProvider
supr java.lang.Object

CLSS public abstract interface com.github.weisj.jsvg.nodes.filter.PixelProvider
 anno 0 java.lang.FunctionalInterface()
meth public abstract int pixelAt(double,double)

CLSS public abstract com.github.weisj.jsvg.nodes.filter.TransferFunctionElement
supr com.github.weisj.jsvg.nodes.AbstractSVGNode
hfds IDENTITY_LOOKUP_TABLE,channel,lookupTable,type

CLSS public final static !enum com.github.weisj.jsvg.nodes.filter.TransferFunctionElement$Channel
 outer com.github.weisj.jsvg.nodes.filter.TransferFunctionElement
fld public final static com.github.weisj.jsvg.nodes.filter.TransferFunctionElement$Channel Alpha
fld public final static com.github.weisj.jsvg.nodes.filter.TransferFunctionElement$Channel Blue
fld public final static com.github.weisj.jsvg.nodes.filter.TransferFunctionElement$Channel Green
fld public final static com.github.weisj.jsvg.nodes.filter.TransferFunctionElement$Channel Red
meth public static com.github.weisj.jsvg.nodes.filter.TransferFunctionElement$Channel valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.nodes.filter.TransferFunctionElement$Channel[] values()
supr java.lang.Enum<com.github.weisj.jsvg.nodes.filter.TransferFunctionElement$Channel>

CLSS public final com.github.weisj.jsvg.nodes.filter.TransferFunctionElement$FeFuncA
fld public final static java.lang.String TAG = "fefunca"
supr com.github.weisj.jsvg.nodes.filter.TransferFunctionElement

CLSS public final com.github.weisj.jsvg.nodes.filter.TransferFunctionElement$FeFuncB
fld public final static java.lang.String TAG = "fefuncb"
supr com.github.weisj.jsvg.nodes.filter.TransferFunctionElement

CLSS public final com.github.weisj.jsvg.nodes.filter.TransferFunctionElement$FeFuncG
fld public final static java.lang.String TAG = "fefuncg"
supr com.github.weisj.jsvg.nodes.filter.TransferFunctionElement

CLSS public final com.github.weisj.jsvg.nodes.filter.TransferFunctionElement$FeFuncR
fld public final static java.lang.String TAG = "fefuncr"
supr com.github.weisj.jsvg.nodes.filter.TransferFunctionElement

CLSS public final com.github.weisj.jsvg.nodes.mesh.MeshGradient
fld public final static java.lang.String TAG = "meshgradient"
intf com.github.weisj.jsvg.attributes.paint.SVGPaint
supr com.github.weisj.jsvg.nodes.container.ContainerNode
hfds gradientUnits,x,y

CLSS public final com.github.weisj.jsvg.nodes.mesh.MeshPatch
supr com.github.weisj.jsvg.nodes.container.ContainerNode

CLSS public final com.github.weisj.jsvg.nodes.mesh.MeshRow
fld public final static java.lang.String TAG = "meshrow"
supr com.github.weisj.jsvg.nodes.container.ContainerNode

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.Container

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.HasClip

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.HasContext
innr public abstract interface static ByDelegate
intf com.github.weisj.jsvg.nodes.prototype.HasFontContext
intf com.github.weisj.jsvg.nodes.prototype.HasFontRenderContext
intf com.github.weisj.jsvg.nodes.prototype.HasPaintContext

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.HasContext$ByDelegate
intf com.github.weisj.jsvg.nodes.prototype.HasContext

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.HasFilter

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.HasFontContext

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.HasFontRenderContext

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.HasGeometryContext
innr public abstract interface static ByDelegate
intf com.github.weisj.jsvg.nodes.prototype.HasClip
intf com.github.weisj.jsvg.nodes.prototype.HasFilter
intf com.github.weisj.jsvg.nodes.prototype.Transformable

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.HasGeometryContext$ByDelegate
intf com.github.weisj.jsvg.nodes.prototype.HasGeometryContext

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.HasPaintContext

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.HasShape
intf com.github.weisj.jsvg.nodes.SVGNode

CLSS public final static !enum com.github.weisj.jsvg.nodes.prototype.HasShape$Box
 outer com.github.weisj.jsvg.nodes.prototype.HasShape
fld public final static com.github.weisj.jsvg.nodes.prototype.HasShape$Box BoundingBox
fld public final static com.github.weisj.jsvg.nodes.prototype.HasShape$Box StrokeBox
meth public static com.github.weisj.jsvg.nodes.prototype.HasShape$Box valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.nodes.prototype.HasShape$Box[] values()
supr java.lang.Enum<com.github.weisj.jsvg.nodes.prototype.HasShape$Box>

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.HasVectorEffects

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.Instantiator

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.Mutator

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.Renderable

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.ShapedContainer
intf com.github.weisj.jsvg.nodes.prototype.Container
intf com.github.weisj.jsvg.nodes.prototype.HasShape

CLSS public abstract interface com.github.weisj.jsvg.nodes.prototype.Transformable

CLSS public final !enum com.github.weisj.jsvg.nodes.prototype.spec.Category
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category Animation
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category BasicShape
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category Container
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category Descriptive
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category FilterPrimitive
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category Gradient
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category Graphic
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category GraphicsReferencing
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category None
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category Shape
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category Structural
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category TextContent
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category TextContentChild
fld public final static com.github.weisj.jsvg.nodes.prototype.spec.Category TransferFunctionElement
supr java.lang.Enum
hfds effectivelyAllowed

CLSS public abstract interface !annotation com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract com.github.weisj.jsvg.nodes.prototype.spec.Category[] value()

CLSS public abstract interface !annotation com.github.weisj.jsvg.nodes.prototype.spec.NotImplemented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface !annotation com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean any()
meth public abstract !hasdefault boolean charData()
meth public abstract !hasdefault com.github.weisj.jsvg.nodes.prototype.spec.Category[] categories()
meth public abstract !hasdefault java.lang.Class<? extends com.github.weisj.jsvg.nodes.SVGNode>[] anyOf()

CLSS public com.github.weisj.jsvg.nodes.text.AbstractGlyphRun$PaintableEmoji
supr java.lang.Object

CLSS public com.github.weisj.jsvg.nodes.text.EmojiGlyph
supr com.github.weisj.jsvg.nodes.text.Glyph

CLSS public com.github.weisj.jsvg.nodes.text.Glyph
supr java.lang.Object

CLSS public abstract com.github.weisj.jsvg.nodes.text.GlyphRunTextOutput
intf com.github.weisj.jsvg.nodes.text.TextOutput
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.nodes.text.NullTextOutput
fld public final static com.github.weisj.jsvg.nodes.text.NullTextOutput INSTANCE
intf com.github.weisj.jsvg.nodes.text.TextOutput
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.nodes.text.Text
fld protected com.github.weisj.jsvg.geometry.size.Length[] dx
fld protected com.github.weisj.jsvg.geometry.size.Length[] dy
fld protected com.github.weisj.jsvg.geometry.size.Length[] x
fld protected com.github.weisj.jsvg.geometry.size.Length[] y
fld protected float[] rotate
fld public final static java.lang.String TAG = "text"
intf com.github.weisj.jsvg.nodes.prototype.HasGeometryContext$ByDelegate
supr com.github.weisj.jsvg.nodes.container.BaseContainerNode
hfds geometryContext

CLSS public final com.github.weisj.jsvg.nodes.text.TextMetrics
cons public init(double,double,int,double,int)
meth public double fixedGlyphLength()
meth public double glyphLength()
meth public double letterSpacingLength()
meth public double totalAdjustableLength()
meth public int controllableLetterSpacingCount()
meth public int glyphCount()
meth public java.lang.String toString()
supr java.lang.Object
hfds controllableLetterSpacingCount,fixedGlyphLength,glyphCount,glyphLength,letterSpacingLength

CLSS public abstract interface com.github.weisj.jsvg.nodes.text.TextOutput

CLSS public final com.github.weisj.jsvg.nodes.text.TextPath
fld public final static java.lang.String TAG = "textpath"
intf com.github.weisj.jsvg.nodes.prototype.HasContext$ByDelegate
intf com.github.weisj.jsvg.nodes.prototype.HasShape
intf com.github.weisj.jsvg.nodes.prototype.HasVectorEffects
intf com.github.weisj.jsvg.nodes.prototype.Renderable
intf com.github.weisj.jsvg.nodes.text.TextSegment$RenderableSegment
supr com.github.weisj.jsvg.nodes.container.BaseContainerNode
hfds DEBUG,pathShape,renderMethod,side,spacing,startOffset

CLSS public abstract interface com.github.weisj.jsvg.nodes.text.TextSegment$RenderableSegment

CLSS public final static !enum com.github.weisj.jsvg.nodes.text.TextSegment$RenderableSegment$UseTextLengthForCalculation
 outer com.github.weisj.jsvg.nodes.text.TextSegment$RenderableSegment
fld public final static com.github.weisj.jsvg.nodes.text.TextSegment$RenderableSegment$UseTextLengthForCalculation NO
fld public final static com.github.weisj.jsvg.nodes.text.TextSegment$RenderableSegment$UseTextLengthForCalculation YES
meth public static com.github.weisj.jsvg.nodes.text.TextSegment$RenderableSegment$UseTextLengthForCalculation valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.nodes.text.TextSegment$RenderableSegment$UseTextLengthForCalculation[] values()
supr java.lang.Enum<com.github.weisj.jsvg.nodes.text.TextSegment$RenderableSegment$UseTextLengthForCalculation>

CLSS public final com.github.weisj.jsvg.nodes.text.TextSpan
fld protected com.github.weisj.jsvg.geometry.size.Length[] dx
fld protected com.github.weisj.jsvg.geometry.size.Length[] dy
fld protected com.github.weisj.jsvg.geometry.size.Length[] x
fld protected com.github.weisj.jsvg.geometry.size.Length[] y
fld protected float[] rotate
fld public final static java.lang.String TAG = "tspan"
supr com.github.weisj.jsvg.nodes.container.BaseContainerNode

CLSS public final com.github.weisj.jsvg.parser.AsynchronousResourceLoader
intf com.github.weisj.jsvg.parser.ResourceLoader
supr java.lang.Object
hfds LOGGER

CLSS public final com.github.weisj.jsvg.parser.AttributeNode
supr java.lang.Object

CLSS public final static !enum com.github.weisj.jsvg.parser.AttributeNode$ElementRelation
 outer com.github.weisj.jsvg.parser.AttributeNode
fld public final static com.github.weisj.jsvg.parser.AttributeNode$ElementRelation GEOMETRY_DATA
fld public final static com.github.weisj.jsvg.parser.AttributeNode$ElementRelation PAINTED_CHILD
fld public final static com.github.weisj.jsvg.parser.AttributeNode$ElementRelation TEMPLATE
meth public static com.github.weisj.jsvg.parser.AttributeNode$ElementRelation valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.parser.AttributeNode$ElementRelation[] values()
supr java.lang.Enum<com.github.weisj.jsvg.parser.AttributeNode$ElementRelation>

CLSS public com.github.weisj.jsvg.parser.DefaultParserProvider
intf com.github.weisj.jsvg.parser.ParserProvider
supr java.lang.Object

CLSS public com.github.weisj.jsvg.parser.DocumentLimits
cons public init(int,int,int)
fld public final static com.github.weisj.jsvg.parser.DocumentLimits DEFAULT
fld public final static int DEFAULT_MAX_NESTING_DEPTH = 30
fld public final static int DEFAULT_MAX_PATH_COUNT = 2000
fld public final static int DEFAULT_MAX_USE_NESTING_DEPTH = 15
meth public int maxNestingDepth()
meth public int maxPathCount()
meth public int maxUseNestingDepth()
supr java.lang.Object
hfds maxNestingDepth,maxPathCount,maxUseNestingDepth

CLSS public abstract interface com.github.weisj.jsvg.parser.DomProcessor

CLSS public abstract interface com.github.weisj.jsvg.parser.ElementLoader

CLSS public final com.github.weisj.jsvg.parser.LoadHelper
supr java.lang.Object

CLSS public abstract interface com.github.weisj.jsvg.parser.LoaderContext

CLSS public abstract interface com.github.weisj.jsvg.parser.LoaderContext$Builder

CLSS public final com.github.weisj.jsvg.parser.NodeSupplier
supr java.lang.Object
hfds constructorMap

CLSS public com.github.weisj.jsvg.parser.ParsedDocument
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.parser.ParsedElement
supr java.lang.Object

CLSS public abstract interface com.github.weisj.jsvg.parser.ParserProvider

CLSS public abstract interface com.github.weisj.jsvg.parser.ResourceLoader

CLSS public abstract interface com.github.weisj.jsvg.parser.ResourcePolicy
fld public final static com.github.weisj.jsvg.parser.ResourcePolicy ALLOW_ALL
fld public final static com.github.weisj.jsvg.parser.ResourcePolicy ALLOW_RELATIVE
fld public final static com.github.weisj.jsvg.parser.ResourcePolicy DENY_ALL
fld public final static com.github.weisj.jsvg.parser.ResourcePolicy DENY_EXTERNAL

CLSS public final com.github.weisj.jsvg.parser.SVGDocumentBuilder
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.parser.SVGLoader
supr java.lang.Object

CLSS public final !enum com.github.weisj.jsvg.parser.SeparatorMode
fld public final static com.github.weisj.jsvg.parser.SeparatorMode COMMA_AND_WHITESPACE
fld public final static com.github.weisj.jsvg.parser.SeparatorMode COMMA_ONLY
fld public final static com.github.weisj.jsvg.parser.SeparatorMode SEMICOLON_ONLY
fld public final static com.github.weisj.jsvg.parser.SeparatorMode WHITESPACE_ONLY
meth public boolean allowWhitespace()
meth public char separator()
meth public static com.github.weisj.jsvg.parser.SeparatorMode valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.parser.SeparatorMode[] values()
supr java.lang.Enum<com.github.weisj.jsvg.parser.SeparatorMode>
hfds allowWhitespace,separator

CLSS public final com.github.weisj.jsvg.parser.StaxSVGLoader
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.parser.SwingUIFuture
intf com.github.weisj.jsvg.parser.UIFuture
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.parser.SynchronousResourceLoader
intf com.github.weisj.jsvg.parser.ResourceLoader
supr java.lang.Object

CLSS public abstract interface com.github.weisj.jsvg.parser.UIFuture

CLSS public final com.github.weisj.jsvg.parser.ValueUIFuture
intf com.github.weisj.jsvg.parser.UIFuture
supr java.lang.Object
hfds value

CLSS public abstract interface com.github.weisj.jsvg.parser.css.CssParser

CLSS public final com.github.weisj.jsvg.parser.css.StyleProperty
supr java.lang.Object

CLSS public abstract interface com.github.weisj.jsvg.parser.css.StyleSheet

CLSS public abstract interface com.github.weisj.jsvg.parser.css.StyleSheet$RuleConsumer

CLSS public com.github.weisj.jsvg.parser.resources.ImageResource
intf com.github.weisj.jsvg.parser.resources.RenderableResource
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.parser.resources.MissingImageResource
intf com.github.weisj.jsvg.parser.resources.RenderableResource
supr java.lang.Object
hfds SIZE,missingImage

CLSS public abstract interface com.github.weisj.jsvg.parser.resources.RenderableResource

CLSS public com.github.weisj.jsvg.parser.resources.SVGResource
intf com.github.weisj.jsvg.parser.resources.RenderableResource
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.renderer.AnimationState
cons public init(long,long)
fld public final static com.github.weisj.jsvg.renderer.AnimationState NO_ANIMATION
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public long timestamp()
supr java.lang.Object
hfds currentTime,startTime

CLSS public final com.github.weisj.jsvg.renderer.ContextElementAttributes
supr java.lang.Object

CLSS public com.github.weisj.jsvg.renderer.ElementBounds
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.renderer.FontRenderContext
supr java.lang.Object

CLSS public com.github.weisj.jsvg.renderer.Graphics2DOutput
intf com.github.weisj.jsvg.renderer.Output
supr java.lang.Object
hfds g

CLSS public final com.github.weisj.jsvg.renderer.GraphicsUtil
supr java.lang.Object
hfds LOGGER

CLSS public abstract interface com.github.weisj.jsvg.renderer.GraphicsUtil$DisposablePaint

CLSS public abstract interface com.github.weisj.jsvg.renderer.GraphicsUtil$WrappingPaint

CLSS public final com.github.weisj.jsvg.renderer.MaskedPaint
intf com.github.weisj.jsvg.renderer.GraphicsUtil$DisposablePaint
intf com.github.weisj.jsvg.renderer.GraphicsUtil$WrappingPaint
intf java.awt.Paint
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.renderer.NodeRenderer
supr java.lang.Object

CLSS public com.github.weisj.jsvg.renderer.NullOutput
intf com.github.weisj.jsvg.renderer.Output
intf com.github.weisj.jsvg.renderer.Output$SafeState
supr java.lang.Object

CLSS public abstract interface com.github.weisj.jsvg.renderer.Output

CLSS public abstract interface static com.github.weisj.jsvg.renderer.Output$SafeState
 outer com.github.weisj.jsvg.renderer.Output
meth public abstract void restore()

CLSS public final com.github.weisj.jsvg.renderer.PaintContext
intf com.github.weisj.jsvg.nodes.prototype.Mutator
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.renderer.RenderContext
supr java.lang.Object

CLSS public final static !enum com.github.weisj.jsvg.renderer.RenderContext$EstablishRootMeasure
 outer com.github.weisj.jsvg.renderer.RenderContext
fld public final static com.github.weisj.jsvg.renderer.RenderContext$EstablishRootMeasure No
fld public final static com.github.weisj.jsvg.renderer.RenderContext$EstablishRootMeasure Yes
meth public static com.github.weisj.jsvg.renderer.RenderContext$EstablishRootMeasure valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.renderer.RenderContext$EstablishRootMeasure[] values()
supr java.lang.Enum<com.github.weisj.jsvg.renderer.RenderContext$EstablishRootMeasure>

CLSS public com.github.weisj.jsvg.renderer.ShapeOutput
intf com.github.weisj.jsvg.renderer.Output
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.renderer.ShapeRenderer
supr java.lang.Object
hfds DEBUG_MARKERS

CLSS public final com.github.weisj.jsvg.renderer.ShapeRenderer$PaintShape
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.renderer.ShapeRenderer$ShapeMarkerInfo
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.renderer.ShapeRenderer$ShapePaintContext
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.renderer.StrokeContext
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.renderer.TransformedPaint
intf java.awt.Paint
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.renderer.awt.AwtComponentPlatformSupport
intf com.github.weisj.jsvg.renderer.awt.PlatformSupport
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.renderer.awt.JComponentPlatformSupport
intf com.github.weisj.jsvg.renderer.awt.PlatformSupport
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.renderer.awt.NullPlatformSupport
fld public final static com.github.weisj.jsvg.renderer.awt.NullPlatformSupport INSTANCE
intf com.github.weisj.jsvg.renderer.awt.PlatformSupport
supr java.lang.Object

CLSS public abstract interface com.github.weisj.jsvg.renderer.awt.PlatformSupport

CLSS public abstract interface static com.github.weisj.jsvg.renderer.awt.PlatformSupport$TargetSurface
 outer com.github.weisj.jsvg.renderer.awt.PlatformSupport
meth public abstract void repaint()

CLSS public abstract com.github.weisj.jsvg.renderer.jdk.SVGMultipleGradientPaint
intf java.awt.Paint
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.renderer.jdk.SVGRadialGradientPaint
supr com.github.weisj.jsvg.renderer.jdk.SVGMultipleGradientPaint

CLSS public final com.github.weisj.jsvg.util.AttributeUtil
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.util.AttributeUtil$AxisPair
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.util.BlittableImage
supr java.lang.Object

CLSS public abstract interface com.github.weisj.jsvg.util.BlittableImage$BufferSurfaceSupplier

CLSS public com.github.weisj.jsvg.util.CachedSurfaceSupplier
supr java.lang.Object

CLSS public com.github.weisj.jsvg.util.CachedSurfaceSupplier$ResourceCleaner
supr java.lang.Object

CLSS public abstract com.github.weisj.jsvg.util.ColorSpaceAwareRGBImageFilter
cons public init()
meth protected int pack(int[])
meth protected int[] getRGB(int)
meth public void setConvertToLinear(boolean)
supr java.awt.image.RGBImageFilter
hfds convertToLinear,tmp

CLSS public final com.github.weisj.jsvg.util.ColorUtil
supr java.lang.Object
hfds LinearRGBtoSRGB,LinearRGBtoSRGBPre,SRGBtoLinearRGB,SRGBtoLinearRGBPre

CLSS public final com.github.weisj.jsvg.util.ConstantProvider
intf com.github.weisj.jsvg.util.Provider
supr java.lang.Object

CLSS public com.github.weisj.jsvg.util.GraphicsResetHelper
intf com.github.weisj.jsvg.renderer.Output$SafeState
supr java.lang.Object
hfds graphics,originalComposite,originalPaint,originalStroke,originalTransform

CLSS public final com.github.weisj.jsvg.util.ImageUtil
supr java.lang.Object

CLSS public final static !enum com.github.weisj.jsvg.util.ImageUtil$Premultiplied
 outer com.github.weisj.jsvg.util.ImageUtil
fld public final static com.github.weisj.jsvg.util.ImageUtil$Premultiplied No
fld public final static com.github.weisj.jsvg.util.ImageUtil$Premultiplied Yes
meth public static com.github.weisj.jsvg.util.ImageUtil$Premultiplied valueOf(java.lang.String)
meth public static com.github.weisj.jsvg.util.ImageUtil$Premultiplied[] values()
supr java.lang.Enum<com.github.weisj.jsvg.util.ImageUtil$Premultiplied>

CLSS public final com.github.weisj.jsvg.util.LazyProvider
intf com.github.weisj.jsvg.util.Provider
supr java.lang.Object

CLSS public com.github.weisj.jsvg.util.ParserBase
supr java.lang.Object

CLSS public final com.github.weisj.jsvg.util.PathUtil
supr java.lang.Object

CLSS public abstract interface com.github.weisj.jsvg.util.Provider

CLSS public final com.github.weisj.jsvg.util.ResourceUtil
supr java.lang.Object
hfds LOGGER,SUPPORTED_MIME_TYPES

CLSS public final com.github.weisj.jsvg.util.ShapeUtil
supr java.lang.Object
hfds NON_RECTILINEAR_TRANSFORM_MASK

CLSS public final com.github.weisj.jsvg.util.SystemUtil
fld public final static boolean isMacOS
fld public final static java.lang.String OS_NAME
supr java.lang.Object

CLSS public abstract interface java.awt.Composite
meth public abstract java.awt.CompositeContext createContext(java.awt.image.ColorModel,java.awt.image.ColorModel,java.awt.RenderingHints)

CLSS public abstract interface java.awt.Paint
intf java.awt.Transparency
meth public abstract java.awt.PaintContext createContext(java.awt.image.ColorModel,java.awt.Rectangle,java.awt.geom.Rectangle2D,java.awt.geom.AffineTransform,java.awt.RenderingHints)

CLSS public abstract interface java.awt.Shape
meth public abstract boolean contains(double,double)
meth public abstract boolean contains(double,double,double,double)
meth public abstract boolean contains(java.awt.geom.Point2D)
meth public abstract boolean contains(java.awt.geom.Rectangle2D)
meth public abstract boolean intersects(double,double,double,double)
meth public abstract boolean intersects(java.awt.geom.Rectangle2D)
meth public abstract java.awt.Rectangle getBounds()
meth public abstract java.awt.geom.PathIterator getPathIterator(java.awt.geom.AffineTransform)
meth public abstract java.awt.geom.PathIterator getPathIterator(java.awt.geom.AffineTransform,double)
meth public abstract java.awt.geom.Rectangle2D getBounds2D()

CLSS public abstract interface java.awt.Transparency
fld public final static int BITMASK = 2
fld public final static int OPAQUE = 1
fld public final static int TRANSLUCENT = 3
meth public abstract int getTransparency()

CLSS public abstract java.awt.geom.Dimension2D
cons protected init()
intf java.lang.Cloneable
meth public abstract double getHeight()
meth public abstract double getWidth()
meth public abstract void setSize(double,double)
meth public java.lang.Object clone()
meth public void setSize(java.awt.geom.Dimension2D)
supr java.lang.Object

CLSS public abstract interface java.awt.geom.PathIterator
fld public final static int SEG_CLOSE = 4
fld public final static int SEG_CUBICTO = 3
fld public final static int SEG_LINETO = 1
fld public final static int SEG_MOVETO = 0
fld public final static int SEG_QUADTO = 2
fld public final static int WIND_EVEN_ODD = 0
fld public final static int WIND_NON_ZERO = 1
meth public abstract boolean isDone()
meth public abstract int currentSegment(double[])
meth public abstract int currentSegment(float[])
meth public abstract int getWindingRule()
meth public abstract void next()

CLSS public abstract java.awt.geom.Rectangle2D
cons protected init()
fld public final static int OUT_BOTTOM = 8
fld public final static int OUT_LEFT = 1
fld public final static int OUT_RIGHT = 4
fld public final static int OUT_TOP = 2
innr public static Double
innr public static Float
meth public abstract int outcode(double,double)
meth public abstract java.awt.geom.Rectangle2D createIntersection(java.awt.geom.Rectangle2D)
meth public abstract java.awt.geom.Rectangle2D createUnion(java.awt.geom.Rectangle2D)
meth public abstract void setRect(double,double,double,double)
meth public boolean contains(double,double)
meth public boolean contains(double,double,double,double)
meth public boolean equals(java.lang.Object)
meth public boolean intersects(double,double,double,double)
meth public boolean intersectsLine(double,double,double,double)
meth public boolean intersectsLine(java.awt.geom.Line2D)
meth public int hashCode()
meth public int outcode(java.awt.geom.Point2D)
meth public java.awt.geom.PathIterator getPathIterator(java.awt.geom.AffineTransform)
meth public java.awt.geom.PathIterator getPathIterator(java.awt.geom.AffineTransform,double)
meth public java.awt.geom.Rectangle2D getBounds2D()
meth public static void intersect(java.awt.geom.Rectangle2D,java.awt.geom.Rectangle2D,java.awt.geom.Rectangle2D)
meth public static void union(java.awt.geom.Rectangle2D,java.awt.geom.Rectangle2D,java.awt.geom.Rectangle2D)
meth public void add(double,double)
meth public void add(java.awt.geom.Point2D)
meth public void add(java.awt.geom.Rectangle2D)
meth public void setFrame(double,double,double,double)
meth public void setRect(java.awt.geom.Rectangle2D)
supr java.awt.geom.RectangularShape

CLSS public static java.awt.geom.Rectangle2D$Float
 outer java.awt.geom.Rectangle2D
cons public init()
cons public init(float,float,float,float)
fld public float height
fld public float width
fld public float x
fld public float y
intf java.io.Serializable
meth public boolean isEmpty()
meth public double getHeight()
meth public double getWidth()
meth public double getX()
meth public double getY()
meth public int outcode(double,double)
meth public java.awt.geom.Rectangle2D createIntersection(java.awt.geom.Rectangle2D)
meth public java.awt.geom.Rectangle2D createUnion(java.awt.geom.Rectangle2D)
meth public java.awt.geom.Rectangle2D getBounds2D()
meth public java.lang.String toString()
meth public void setRect(double,double,double,double)
meth public void setRect(float,float,float,float)
meth public void setRect(java.awt.geom.Rectangle2D)
supr java.awt.geom.Rectangle2D

CLSS public abstract java.awt.geom.RectangularShape
cons protected init()
intf java.awt.Shape
intf java.lang.Cloneable
meth public abstract boolean isEmpty()
meth public abstract double getHeight()
meth public abstract double getWidth()
meth public abstract double getX()
meth public abstract double getY()
meth public abstract void setFrame(double,double,double,double)
meth public boolean contains(java.awt.geom.Point2D)
meth public boolean contains(java.awt.geom.Rectangle2D)
meth public boolean intersects(java.awt.geom.Rectangle2D)
meth public double getCenterX()
meth public double getCenterY()
meth public double getMaxX()
meth public double getMaxY()
meth public double getMinX()
meth public double getMinY()
meth public java.awt.Rectangle getBounds()
meth public java.awt.geom.PathIterator getPathIterator(java.awt.geom.AffineTransform,double)
meth public java.awt.geom.Rectangle2D getFrame()
meth public java.lang.Object clone()
meth public void setFrame(java.awt.geom.Point2D,java.awt.geom.Dimension2D)
meth public void setFrame(java.awt.geom.Rectangle2D)
meth public void setFrameFromCenter(double,double,double,double)
meth public void setFrameFromCenter(java.awt.geom.Point2D,java.awt.geom.Point2D)
meth public void setFrameFromDiagonal(double,double,double,double)
meth public void setFrameFromDiagonal(java.awt.geom.Point2D,java.awt.geom.Point2D)
supr java.lang.Object

CLSS public abstract interface java.awt.image.ImageConsumer
fld public final static int COMPLETESCANLINES = 4
fld public final static int IMAGEABORTED = 4
fld public final static int IMAGEERROR = 1
fld public final static int RANDOMPIXELORDER = 1
fld public final static int SINGLEFRAME = 16
fld public final static int SINGLEFRAMEDONE = 2
fld public final static int SINGLEPASS = 8
fld public final static int STATICIMAGEDONE = 3
fld public final static int TOPDOWNLEFTRIGHT = 2
meth public abstract void imageComplete(int)
meth public abstract void setColorModel(java.awt.image.ColorModel)
meth public abstract void setDimensions(int,int)
meth public abstract void setHints(int)
meth public abstract void setPixels(int,int,int,int,java.awt.image.ColorModel,byte[],int,int)
meth public abstract void setPixels(int,int,int,int,java.awt.image.ColorModel,int[],int,int)
meth public abstract void setProperties(java.util.Hashtable<?,?>)

CLSS public java.awt.image.ImageFilter
cons public init()
fld protected java.awt.image.ImageConsumer consumer
intf java.awt.image.ImageConsumer
intf java.lang.Cloneable
meth public java.awt.image.ImageFilter getFilterInstance(java.awt.image.ImageConsumer)
meth public java.lang.Object clone()
meth public void imageComplete(int)
meth public void resendTopDownLeftRight(java.awt.image.ImageProducer)
meth public void setColorModel(java.awt.image.ColorModel)
meth public void setDimensions(int,int)
meth public void setHints(int)
meth public void setPixels(int,int,int,int,java.awt.image.ColorModel,byte[],int,int)
meth public void setPixels(int,int,int,int,java.awt.image.ColorModel,int[],int,int)
meth public void setProperties(java.util.Hashtable<?,?>)
supr java.lang.Object

CLSS public abstract java.awt.image.RGBImageFilter
cons public init()
fld protected boolean canFilterIndexColorModel
fld protected java.awt.image.ColorModel newmodel
fld protected java.awt.image.ColorModel origmodel
meth public abstract int filterRGB(int,int,int)
meth public java.awt.image.IndexColorModel filterIndexColorModel(java.awt.image.IndexColorModel)
meth public void filterRGBPixels(int,int,int,int,int[],int,int)
meth public void setColorModel(java.awt.image.ColorModel)
meth public void setPixels(int,int,int,int,java.awt.image.ColorModel,byte[],int,int)
meth public void setPixels(int,int,int,int,java.awt.image.ColorModel,int[],int,int)
meth public void substituteColorModel(java.awt.image.ColorModel,java.awt.image.ColorModel)
supr java.awt.image.ImageFilter

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

