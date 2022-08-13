package com.example.common.constant;

public class ProductConstant {
    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性","base"),ATTR_TYPE_SALE(0,"销售属性", "sale");
        private final Integer code;
        private final String msg;
        private final String value;
        AttrEnum(Integer code, String msg, String value) {
            this.code = code;
            this.msg = msg;
            this.value = value;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        public String getValue() { return value; }
    }
}
