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
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.crossevol.wordbook.ui.svgicons.MyIconPack
import kotlin.Unit

public val MyIconPack.SendAndArchive: ImageVector
    get() {
        if (_sendAndArchive != null) {
            return _sendAndArchive!!
        }
        _sendAndArchive = Builder(name = "SendAndArchive", defaultWidth = 24.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFF1f1f1f)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(21.0f, 10.0f)
                horizontalLineToRelative(-3.0f)
                lineTo(2.0f, 3.0f)
                verticalLineToRelative(7.0f)
                lineToRelative(9.0f, 2.0f)
                lineToRelative(-9.0f, 2.0f)
                verticalLineToRelative(7.0f)
                lineToRelative(8.0f, -3.5f)
                lineTo(10.0f, 21.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(9.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-9.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(21.0f, 21.0f)
                horizontalLineToRelative(-9.0f)
                verticalLineToRelative(-9.0f)
                horizontalLineToRelative(9.0f)
                verticalLineToRelative(9.0f)
                close()
                moveTo(16.5f, 20.0f)
                lineTo(13.0f, 16.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(2.0f)
                lineToRelative(-3.5f, 4.0f)
                close()
            }
        }
        .build()
        return _sendAndArchive!!
    }

private var _sendAndArchive: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MyIconPack.SendAndArchive, contentDescription = "")
    }
}
