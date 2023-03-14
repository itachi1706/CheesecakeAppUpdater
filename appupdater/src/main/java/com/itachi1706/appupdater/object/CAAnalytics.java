package com.itachi1706.appupdater.object;

/**
 * Created by Kenneth on 17/3/2018.
 * for com.itachi1706.appupdater.Objects in CheesecakeUtilities
 */
@SuppressWarnings("unused")
public class CAAnalytics {

    private int sdkver;
    private long appVerCode;
    private String sdkString;
    private String dName;
    private String dManufacturer;
    private String dCPU;
    private String appVer;
    private String dFingerprint;
    private String dCodename;
    private String dTags;
    private String sdkPatch;
    private boolean debug;

    public CAAnalytics() {
        // JSON Constructor
    }

    public int getSdkver() {
        return sdkver;
    }

    public void setSdkver(int sdkver) {
        this.sdkver = sdkver;
    }

    public long getAppVerCode() {
        return appVerCode;
    }

    public void setAppVerCode(long appVerCode) {
        this.appVerCode = appVerCode;
    }

    public String getSdkString() {
        return sdkString;
    }

    public void setSdkString(String sdkString) {
        this.sdkString = sdkString;
    }

    public String getdName() {
        return dName;
    }

    public void setdName(String dName) {
        this.dName = dName;
    }

    public String getdManufacturer() {
        return dManufacturer;
    }

    public void setdManufacturer(String dManufacturer) {
        this.dManufacturer = dManufacturer;
    }

    public String getAppVer() {
        return appVer;
    }

    public void setAppVer(String appVer) {
        this.appVer = appVer;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getdCPU() {
        return dCPU;
    }

    public void setdCPU(String dCPU) {
        this.dCPU = dCPU;
    }

    public String getdFingerprint() {
        return dFingerprint;
    }

    public void setdFingerprint(String dFingerprint) {
        this.dFingerprint = dFingerprint;
    }

    public String getdCodename() {
        return dCodename;
    }

    public void setdCodename(String dCodename) {
        this.dCodename = dCodename;
    }

    public String getdTags() {
        return dTags;
    }

    public void setdTags(String dTags) {
        this.dTags = dTags;
    }

    public String getSdkPatch() {
        return sdkPatch;
    }

    public void setSdkPatch(String sdkPatch) {
        this.sdkPatch = sdkPatch;
    }
}
