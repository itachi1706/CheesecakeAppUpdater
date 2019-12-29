package com.itachi1706.appupdater.Util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kenneth on 20/8/2016.
 * for com.itachi1706.appupdater.Util in AppUpdater
 */
public final class ValidationHelper {

    public static final int GOOGLE_PLAY = 1;    //com.android.vending, com.google.android.feedback
    public static final int AMAZON = 2;         //com.amazon.venezia
    public static final int SIDELOAD = 0;

    private static List<String> playstoreList = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));
    private static List<String> amazonList = new ArrayList<>(Collections.singletonList("com.amazon.venezia"));

    public static boolean checkNotSideloaded(Context context) {
        List<String> mergedList = new ArrayList<>();
        mergedList.addAll(playstoreList);
        mergedList.addAll(amazonList);

        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());

        return installer != null && mergedList.contains(installer);
    }

    public static boolean checkSideloaded(Context context) {
        return !checkNotSideloaded(context);
    }

    public static String getInstallLocation(Context context) {
        return getInstallLocation(context, context.getPackageName());
    }

    public static String getInstallLocation(Context context, String packageName) {
        return context.getPackageManager().getInstallerPackageName(packageName);
    }

    public static int checkInstallLocation(Context context) {
        return checkInstallLocation(context, context.getPackageName());
    }

    public static int checkInstallLocation(Context context, String packageName) {
        final String installer = getInstallLocation(context, packageName);
        if (installer == null) return SIDELOAD;
        if (playstoreList.contains(installer)) return GOOGLE_PLAY;
        if (amazonList.contains(installer)) return AMAZON;
        return SIDELOAD;
    }

    // Signature Validation
    @NonNull
    public static String getSignatureForValidation(Context context) {
        PackageManager pm = context.getPackageManager();
        Signature[] signatures;
        try {
            PackageInfo pInfo;
            try {
                pInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            } catch (RuntimeException e) {
                Log.e("ValidationHelper", "Failed to get package info. Signature cannot be validated");
                return "error";
            }
            signatures = pInfo.signatures;
            return getSignatureString(signatures[0]).trim();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e("ValidationHelper", "Failed to get package info. Signature cannot be validated");
            return "error";
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e("ValidationHelper", "Algorithm not recognized on this Android Version, signature cannot be validated");
            return "error";
        }
    }

    public static X509Certificate getCert(byte[] cert) throws CertificateException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(cert);
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        return (X509Certificate) cf.generateCertificate(inputStream);
    }

    public static String getSignatureString(Signature sig) throws NoSuchAlgorithmException {
        byte[] cert = sig.toByteArray();
        try {
            return bytesToHex(MessageDigest.getInstance("SHA1").digest(getCert(cert).getEncoded()));
        } catch (CertificateException e) {
            Log.e("Signature", "Cannot Create Signature, Falling back");
            Log.e("Signature", "Error: " + e.getLocalizedMessage());
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(sig.toByteArray());
            return bytesToHex(md.digest());
        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hexChars.length; i++) {
            if (i % 2 == 0 && i != 0) sb.append(":");
            sb.append(hexChars[i]);
        }
        return sb.toString();
    }


}
