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

public val MyIconPack.Description: ImageVector
    get() {
        if (_description != null) {
            return _description!!
        }
        _description = Builder(name = "Description", defaultWidth = 24.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFF1f1f1f)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(14.0f, 2.0f)
                lineTo(6.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(4.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 1.99f, 2.0f)
                lineTo(18.0f, 22.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(20.0f, 8.0f)
                lineToRelative(-6.0f, -6.0f)
                close()
                moveTo(16.0f, 18.0f)
                lineTo(8.0f, 18.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(16.0f, 14.0f)
                lineTo(8.0f, 14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(13.0f, 9.0f)
                lineTo(13.0f, 3.5f)
                lineTo(18.5f, 9.0f)
                lineTo(13.0f, 9.0f)
                close()
            }
        }
        .build()
        return _description!!
    }

private var _description: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MyIconPack.Description, contentDescription = "")
    }
}
