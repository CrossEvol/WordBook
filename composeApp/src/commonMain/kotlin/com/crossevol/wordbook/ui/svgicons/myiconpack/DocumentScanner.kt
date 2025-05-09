package com.crossevol.wordbook.ui.svgicons.myiconpack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.crossevol.wordbook.ui.svgicons.MyIconPack
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.Unit

public val MyIconPack.DocumentScanner: ImageVector
    get() {
        if (_documentScanner != null) {
            return _documentScanner!!
        }
        _documentScanner = Builder(name = "DocumentScanner", defaultWidth = 24.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFF1f1f1f)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(7.0f, 3.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(2.0f)
                verticalLineTo(1.0f)
                horizontalLineToRelative(5.0f)
                verticalLineTo(3.0f)
                close()
                moveTo(22.0f, 6.0f)
                verticalLineTo(1.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(22.0f)
                close()
                moveTo(7.0f, 21.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineTo(2.0f)
                verticalLineToRelative(5.0f)
                horizontalLineToRelative(5.0f)
                verticalLineTo(21.0f)
                close()
                moveTo(20.0f, 18.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(-5.0f)
                horizontalLineTo(20.0f)
                close()
                moveTo(19.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                verticalLineTo(18.0f)
                close()
                moveTo(15.0f, 8.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(6.0f)
                verticalLineTo(8.0f)
                close()
                moveTo(15.0f, 11.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(6.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(15.0f, 14.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(6.0f)
                verticalLineTo(14.0f)
                close()
            }
        }
        .build()
        return _documentScanner!!
    }

private var _documentScanner: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MyIconPack.DocumentScanner, contentDescription = "")
    }
}
