package com.itachi1706.appupdater.extlib.fingerprint;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import androidx.annotation.RequiresPermission;

@TargetApi(Build.VERSION_CODES.M)
class AuthenticationHandler extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal mCancellationSignal;
    private boolean mSelfCancelled;
    private FingerprintManager.CryptoObject mCryptoObject;
    private Context mContext;

    private FingerprintLib mFingerprintLib;

    public AuthenticationHandler(FingerprintLib fingerprintLib, FingerprintManager.CryptoObject cryptoObject) {
        mFingerprintLib = fingerprintLib;
        mCryptoObject = cryptoObject;
        mContext = fingerprintLib.mContext.getApplicationContext();
    }

    public boolean isReadyToStart() {
        return mCancellationSignal == null;
    }

    @RequiresPermission(Manifest.permission.USE_FINGERPRINT)
    public void start() {
        mCancellationSignal = new CancellationSignal();
        mSelfCancelled = false;
        mFingerprintLib.mFingerprintManager.authenticate(mCryptoObject, mCancellationSignal, 0 /* flags */, this, null);
    }

    public void stop() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    // Callbacks from FingerprintManager

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        if (!mSelfCancelled) {
            if (mFingerprintLib.mCallback != null)
                mFingerprintLib.mCallback.onFingerprintLibError(mFingerprintLib, FingerprintLibErrorType.UNRECOVERABLE_ERROR, new Exception(errString.toString()));
        }
        stop();
        mFingerprintLib.mFingerprintManager = mContext.getSystemService(FingerprintManager.class);
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        if (mFingerprintLib.mCallback != null)
            mFingerprintLib.mCallback.onFingerprintLibError(mFingerprintLib, FingerprintLibErrorType.FINGERPRINT_NOT_RECOGNIZED, new Exception("Fingerprint not recognized, try again."));
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        if (mFingerprintLib.mCallback != null)
            mFingerprintLib.mCallback.onFingerprintLibError(mFingerprintLib, FingerprintLibErrorType.HELP_ERROR, new Exception(helpString.toString()));
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        if (mFingerprintLib.mCallback != null)
            mFingerprintLib.mCallback.onFingerprintLibAuthenticated(mFingerprintLib);
        stop();
    }
}