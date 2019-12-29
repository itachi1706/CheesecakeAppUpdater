package com.itachi1706.appupdater.extlib.fingerprint;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

@TargetApi(Build.VERSION_CODES.M)
class MarshmallowUtils {

    private MarshmallowUtils() {
    }

    public static boolean isFingerprintRegistered(FingerprintLib fingerprintLib) {
        if (!isFingerprintAuthAvailable(fingerprintLib)) return false;
        //noinspection ResourceType
        return fingerprintLib.mKeyguardManager.isKeyguardSecure() && fingerprintLib.mFingerprintManager.hasEnrolledFingerprints();
    }

    public static boolean isFingerprintAuthAvailable(FingerprintLib fingerprintLib) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false;
        int granted = ContextCompat.checkSelfPermission(fingerprintLib.mContext, Manifest.permission.USE_FINGERPRINT);
        if (granted != PackageManager.PERMISSION_GRANTED) return false;
        //noinspection ResourceType
        return fingerprintLib.mFingerprintManager.isHardwareDetected() && fingerprintLib.mFingerprintManager.hasEnrolledFingerprints();
    }

    public static void initBase(Context context, FingerprintLibBase fingerprintLib) {
        fingerprintLib.mKeyguardManager = context.getSystemService(KeyguardManager.class);
        fingerprintLib.mFingerprintManager = context.getSystemService(FingerprintManager.class);
        try {
            fingerprintLib.mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }
        try {
            fingerprintLib.mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
        }
        try {
            fingerprintLib.mCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }
    }

    public static boolean initCipher(FingerprintLibBase fingerprintLib) {
        try {
            fingerprintLib.mKeyStore.load(null);
            SecretKey key = (SecretKey) fingerprintLib.mKeyStore.getKey(fingerprintLib.mKeyName, null);
            fingerprintLib.mCipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }
}