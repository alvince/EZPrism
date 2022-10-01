package cn.alvince.droidprism.log.page

import cn.alvince.zanpakuto.core.time.Duration
import cn.alvince.zanpakuto.core.time.Timestamp

/**
 * Create by bytedance on 2022/10/1
 *
 * @author zhangyang.alvince@bytedance.com
 */
interface IPageDurationCalc {
    val showDuration: Duration

    fun setUserVisible(isVisibleToUser: Boolean)
}

class EZPageDurationCalc : IPageDurationCalc {

    override val showDuration get() = _showDuration

    private var isUserVisible = false
    private var lastVisibleTime = Timestamp.ZERO
    private var _showDuration = Duration.ZERO

    override fun setUserVisible(isVisibleToUser: Boolean) {
        if (isVisibleToUser == isUserVisible) {
            return
        }
        isUserVisible = isVisibleToUser
        if (isVisibleToUser) {
            lastVisibleTime = Timestamp.now()
            _showDuration = Duration.ZERO
            return
        }
        _showDuration = Timestamp.now() - lastVisibleTime
    }
}
