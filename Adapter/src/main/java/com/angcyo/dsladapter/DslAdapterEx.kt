package com.angcyo.dsladapter

import android.animation.Animator
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.data.Page


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

//<editor-fold desc="Item操作">

/**
 * 通过条件, 查找[DslAdapterItem].
 *
 * @param useFilterList 是否使用过滤后的数据源. 通常界面上显示的是过滤后的数据, 所有add的数据源在非过滤列表中
 * */
fun DslAdapter.findItem(
    useFilterList: Boolean = true,
    predicate: (DslAdapterItem) -> Boolean
): DslAdapterItem? {
    return getDataList(useFilterList).find(predicate)
}

fun DslAdapter.updateItem(
    payload: Any? = DslAdapterItem.PAYLOAD_UPDATE_PART,
    useFilterList: Boolean = true,
    predicate: (DslAdapterItem) -> Boolean
): DslAdapterItem? {
    return findItem(useFilterList, predicate)?.apply {
        updateAdapterItem(payload, useFilterList)
    }
}

fun DslAdapter.findItemByTag(
    tag: String?,
    useFilterList: Boolean = true
): DslAdapterItem? {
    if (tag == null) {
        return null
    }
    return findItem(useFilterList) {
        it.itemTag == tag
    }
}

fun DslAdapter.findItemByGroup(
    groups: List<String>,
    useFilterList: Boolean = true
): List<DslAdapterItem> {
    return getDataList(useFilterList).findItemByGroup(groups)
}

/**通过Tag查找item*/
fun List<DslAdapterItem>.findItemByTag(tag: String?): DslAdapterItem? {
    if (tag == null) {
        return null
    }
    return find {
        it.itemTag == tag
    }
}

/**通过group查找item*/
fun List<DslAdapterItem>.findItemByGroup(groups: List<String>): List<DslAdapterItem> {
    val result = mutableListOf<DslAdapterItem>()

    groups.forEach { group ->
        forEach {
            if (it.itemGroups.contains(group)) {
                result.add(it)
            }
        }
    }
    return result
}

fun DslAdapter.dslItem(@LayoutRes layoutId: Int, config: DslAdapterItem.() -> Unit = {}) {
    val item = DslAdapterItem()
    item.itemLayoutId = layoutId
    addLastItem(item)
    item.config()
}

fun <T : DslAdapterItem> DslAdapter.dslItem(
    dslItem: T,
    config: T.() -> Unit = {}
) {
    dslCustomItem(dslItem, config)
}

fun <T : DslAdapterItem> DslAdapter.dslCustomItem(
    dslItem: T,
    config: T.() -> Unit = {}
) {
    addLastItem(dslItem)
    dslItem.config()
}

/**空的占位item*/
fun DslAdapter.renderEmptyItem(
    height: Int = 120 * dpi,
    color: Int = Color.TRANSPARENT,
    action: DslAdapterItem.() -> Unit = {}
) {
    val adapterItem = DslAdapterItem()
    adapterItem.itemLayoutId = R.layout.base_empty_item
    adapterItem.itemBindOverride = { itemHolder, _, _, _ ->
        itemHolder.itemView.setBackgroundColor(color)
        itemHolder.itemView.setWidthHeight(-1, height)
    }
    adapterItem.action()
    addLastItem(adapterItem)
}

/**换个贴切的名字*/
fun DslAdapter.render(action: DslAdapter.() -> Unit) {
    this.action()
}

fun DslAdapter.renderItem(count: Int = 1, init: DslAdapterItem.(index: Int) -> Unit) {
    for (i in 0 until count) {
        val adapterItem = DslAdapterItem()
        adapterItem.init(i)
        addLastItem(adapterItem)
    }
}

fun <T> DslAdapter.renderItem(data: T, init: DslAdapterItem.() -> Unit) {
    val adapterItem = DslAdapterItem()
    adapterItem.itemData = data
    adapterItem.init()
    addLastItem(adapterItem)
}

/**获取所有指定类型的数据集合*/
inline fun <reified ItemData> DslAdapter.getAllItemData(useFilterList: Boolean = true): List<ItemData> {
    val result = mutableListOf<ItemData>()
    val itemList = getDataList(useFilterList)
    for (item in itemList) {
        if (item.itemData is ItemData) {
            result.add(item.itemData as ItemData)
        }
    }
    return result
}

/**枚举所有Item*/
fun DslAdapter.eachItem(
    useFilterList: Boolean = true,
    action: (index: Int, dslAdapterItem: DslAdapterItem) -> Unit
) {
    getDataList(useFilterList).forEachIndexed(action)
}

//</editor-fold desc="Item操作">

//<editor-fold desc="payload">

/**是否包含指定的[payload]*/
fun Iterable<*>.containsPayload(any: Any): Boolean {
    var result = false
    for (payload in this) {
        result = if (payload is Iterable<*>) {
            payload.containsPayload(any)
        } else {
            payload == any
        }
        if (result) {
            break
        }
    }
    return result
}

/**是否要更新媒体, 比如:图片*/
fun Iterable<*>.isUpdateMedia(): Boolean {
    return count() <= 0 || containsPayload(DslAdapterItem.PAYLOAD_UPDATE_MEDIA)
}

/**需要更新媒体的负载*/
fun mediaPayload(): List<Int> =
    listOf(DslAdapterItem.PAYLOAD_UPDATE_PART, DslAdapterItem.PAYLOAD_UPDATE_MEDIA)

//</editor-fold desc="payload">

//<editor-fold desc="AdapterStatus">

fun DslAdapter.adapterStatus() = dslAdapterStatusItem.itemState

fun DslAdapter.isAdapterStatusLoading() =
    dslAdapterStatusItem.itemState == DslAdapterStatusItem.ADAPTER_STATUS_LOADING

fun DslAdapter.justRunFilterParams() = defaultFilterParams!!.apply {
    justRun = true
    asyncDiff = false
}

fun DslAdapter.toLoading(filterParams: FilterParams = justRunFilterParams()) {
    setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_LOADING, filterParams)
}

fun DslAdapter.toEmpty(filterParams: FilterParams = justRunFilterParams()) {
    setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_EMPTY, filterParams)
}

fun DslAdapter.toError(filterParams: FilterParams = justRunFilterParams()) {
    setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_ERROR, filterParams)
}

fun DslAdapter.toNone(filterParams: FilterParams = defaultFilterParams!!) {
    setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE, filterParams)
}

fun DslAdapter.toLoadMoreError() {
    setLoadMore(DslLoadMoreItem.LOAD_MORE_ERROR)
}

/**加载更多技术*/
fun DslAdapter.toLoadMoreEnd() {
    setLoadMore(DslLoadMoreItem.LOAD_MORE_NORMAL)
}

/**无更多*/
fun DslAdapter.toLoadNoMore() {
    setLoadMore(DslLoadMoreItem.LOAD_MORE_NO_MORE)
}

/**快速同时监听刷新/加载更多的回调*/
fun DslAdapter.onRefreshOrLoadMore(action: (itemHolder: DslViewHolder, loadMore: Boolean) -> Unit) {
    dslAdapterStatusItem.onRefresh = {
        action(it, false)
    }
    dslLoadMoreItem.onLoadMore = {
        action(it, true)
    }
}

//</editor-fold desc="AdapterStatus">

//<editor-fold desc="Update">

/**立即更新*/
fun DslAdapter.updateNow(filterParams: FilterParams = justRunFilterParams()) =
    updateItemDepend(filterParams)

/**延迟通知*/
fun DslAdapter.delayNotify(filterParams: FilterParams = FilterParams(notifyDiffDelay = 300)) {
    updateItemDepend(filterParams)
}

//</editor-fold desc="Update">

val RecyclerView._dslAdapter: DslAdapter? get() = adapter as? DslAdapter?

fun View?.mH(def: Int = 0): Int {
    return this?.measuredHeight ?: def
}

fun View?.mW(def: Int = 0): Int {
    return this?.measuredWidth ?: def
}

fun View.getChildOrNull(index: Int): View? {
    return if (this is ViewGroup) {
        this.getChildOrNull(index)
    } else {
        this
    }
}

/**获取指定位置[index]的[child], 如果有.*/
fun ViewGroup.getChildOrNull(index: Int): View? {
    return if (index in 0 until childCount) {
        getChildAt(index)
    } else {
        null
    }
}

fun ViewGroup.forEach(recursively: Boolean = false, map: (index: Int, child: View) -> Unit) {
    eachChild(recursively, map)
}

/**枚举所有child view
 * [recursively] 递归所有子view*/
fun ViewGroup.eachChild(recursively: Boolean = false, map: (index: Int, child: View) -> Unit) {
    for (index in 0 until childCount) {
        val childAt = getChildAt(index)
        map.invoke(index, childAt)
        if (recursively && childAt is ViewGroup) {
            childAt.eachChild(recursively, map)
        }
    }
}

/**[androidx/core/animation/Animator.kt:82]*/
inline fun Animator.addListener(
    crossinline onEnd: (animator: Animator) -> Unit = {},
    crossinline onStart: (animator: Animator) -> Unit = {},
    crossinline onCancel: (animator: Animator) -> Unit = {},
    crossinline onRepeat: (animator: Animator) -> Unit = {}
): Animator.AnimatorListener {
    val listener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animator: Animator) = onRepeat(animator)
        override fun onAnimationEnd(animator: Animator) = onEnd(animator)
        override fun onAnimationCancel(animator: Animator) = onCancel(animator)
        override fun onAnimationStart(animator: Animator) = onStart(animator)
    }
    addListener(listener)
    return listener
}