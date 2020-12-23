package com.znq.libcompiler;

/**
 * @desc:
 * @author: ningqiang.zhao
 * @time: 2020-12-14 17:19
 **/
class ClassInfo {
    private static final String GET_KEY = "%s.getKey()";
    final String domainClassName;
    final String declareClassName;
    final String declareString;

    private String memberKey;

    private String memberName;

    private boolean isMap = false;

    public String getMemberKey() {
        return memberKey;
    }

    public ClassInfo(String domianClassName, String declareClassName) {
        this(domianClassName, declareClassName, null);
    }

    public ClassInfo(String domainClassName, String declareClassName, String declareString) {
        this.domainClassName = domainClassName;
        this.declareClassName = declareClassName;
        this.declareString = declareString;
    }

    public String getMemberName() {
        return memberName;
    }


    public void setMemberName(String memberName, boolean isMapKey) {
        this.memberName = memberName;
        this.isMap = isMapKey;
        if (isMapKey) {
            this.memberKey = String.format(GET_KEY, memberName);
        }
    }


    public boolean isMap() {
        return isMap;
    }

    @Override
    public String toString() {
        return "ClassInfo{" +
                "domainClassName='" + domainClassName + '\'' +
                ", declareClassName='" + declareClassName + '\'' +
                ", declareString='" + declareString + '\'' +
                '}';
    }
}