package me.onetwo.aiautotrade.common.enums

/**
 * 주문 상태
 */
enum class OrderState {
    /** 체결 대기 */
    WAIT,

    /** 예약 주문 대기 */
    WATCH,

    /** 체결 완료 */
    DONE,

    /** 주문 취소 */
    CANCEL
}
