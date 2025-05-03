package com.crossevol.wordbook.ui.svgicons

import androidx.compose.ui.graphics.vector.ImageVector
import com.crossevol.wordbook.ui.svgicons.myiconpack.Description
import com.crossevol.wordbook.ui.svgicons.myiconpack.Save
import com.crossevol.wordbook.ui.svgicons.myiconpack.SendAndArchive
import com.crossevol.wordbook.ui.svgicons.myiconpack.Unarchive
import kotlin.String
import kotlin.collections.List as ____KtList
import kotlin.collections.Map as ____KtMap

public object MyIconPack

private var __AllIcons: ____KtList<ImageVector>? = null

public val MyIconPack.AllIcons: ____KtList<ImageVector>
  get() {
    if (__AllIcons != null) {
      return __AllIcons!!
    }
    __AllIcons= listOf(Description, Save, SendAndArchive, Unarchive)
    return __AllIcons!!
  }

private var __AllIconsNamed: ____KtMap<String, ImageVector>? = null

public val MyIconPack.AllIconsNamed: ____KtMap<String, ImageVector>
  get() {
    if (__AllIconsNamed != null) {
      return __AllIconsNamed!!
    }
    __AllIconsNamed= mapOf("description" to Description, "save" to Save, "sendandarchive" to
        SendAndArchive, "unarchive" to Unarchive)
    return __AllIconsNamed!!
  }
