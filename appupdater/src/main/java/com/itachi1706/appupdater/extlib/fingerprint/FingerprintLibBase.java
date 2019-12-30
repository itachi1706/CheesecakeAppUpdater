package com.itachi1706.appupdater.extlib.fingerprint;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.view.inputmethod.InputMethodManager;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

/**
 * @migrated
 */
class FingerprintLibBase {

    protected FingerprintLibBase(@NonNull Activity context, @NonNull String keyName, @NonNull FingerprintLibCallback callback) {
        mContext = context;
        mKeyName = keyName;
        mCallback = callback;

        mInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            MarshmallowUtils.initBase(context, this);
    }

    protected void deinitBase() {
        mKeyName = null;
        mContext = null;
        mKeyguardManager = null;
        mFingerprintManager = null;
        mKeyStore = null;
        mKeyGenerator = null;
        mCipher = null;
    }

    protected String mKeyName;
    protected Context mContext;
    protected KeyguardManager mKeyguardManager;
    protected FingerprintManager mFingerprintManager;
    protected InputMethodManager mInputMethodManager;
    protected KeyStore mKeyStore;
    protected KeyGenerator mKeyGenerator;
    protected Cipher mCipher;
    protected FingerprintLibCallback mCallback;

    public void setCallback(@NonNull FingerprintLibCallback callback) {
        this.mCallback = callback;
    }

    /**
     * Initialize the {@link Cipher} instance with the created key in the {@link #recreateKey()}
     * method.
     *
     * @return {@code true} if initialization is successful, {@code false} if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    protected boolean initCipher() {
        return MarshmallowUtils.initCipher(this);
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public final void recreateKey() {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            mKeyGenerator.init(new KeyGenParameterSpec.Builder(mKeyName,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}