package com.itachi1706.appupdater.Objects;

/**
 * Created by Kenneth on 17/3/2018.
 * for com.itachi1706.appupdater.Objects in CheesecakeUtilities
 */
@SuppressWarnings("unused")
public class CAAnalytics {

    private int sdkver, appVerCode;
    private String sdkString, dName, dManufacturer, dCPU, appVer, dFingerprint, dCodename, dTags, sdkPatch;
    private boolean debug;

    public CAAnalytics() {}

    public int getSdkver() {
        return sdkver;
    }

    public void setSdkver(int sdkver) {
        this.sdkver = sdkver;
    }

    public int getAppVerCode() {
        return appVerCode;
    }

    public void setAppVerCode(int appVerCode) {
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
