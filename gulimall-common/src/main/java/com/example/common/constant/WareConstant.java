package com.example.common.constant;

public class WareConstant {
    public enum PurchaseEnum {
        CREATE("新建", 0),
        ASSIGNED("已分配", 1),
        RECEIVED("已领取", 2),
        COMPLETED("已完成", 3),
        UNUSUAL("有异常", 4);

        private final String msg;
        private final int code;

        PurchaseEnum(String msg, int code) {
            this.msg = msg;
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public int getCode() {
            return code;
        }
    }
    public enum PurchaseDetailEnum {
        CREATE("新建",0),
        ASSIGNED("已分配",1),
        PURCHASING("正在采购",2),
        COMPLETED("已完成",3),
        FAIL("采购失败",4)
        ;

        private final String msg;
        private final int code;

        PurchaseDetailEnum(String msg, int code){
            this.msg = msg;
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
