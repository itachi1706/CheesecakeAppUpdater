package com.itachi1706.appupdater.extlib.fingerprint;

/**
 * @migrated
 */
public interface FingerprintLibCallback {

    void onFingerprintLibReady(FingerprintLib fingerprintLib);

    void onFingerprintLibListening(boolean newFingerprint);

    void onFingerprintLibAuthenticated(FingerprintLib fingerprintLib);

    void onFingerprintLibError(FingerprintLib fingerprintLib, FingerprintLibErrorType type, Exception e);
}