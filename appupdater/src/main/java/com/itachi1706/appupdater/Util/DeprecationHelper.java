package com.itachi1706.appupdater.Util;

import android.content.Context;
import android.os.Build;
import android.text.Spanned;

/**
 * Created by Kenneth on 12/5/2017.
 * for com.itachi1706.appupdater.Util in CheesecakeUtilities
 * @migrated
 */
@SuppressWarnings("deprecation")
public class DeprecationHelper {

    public static class Html {
        public static Spanned fromHtml(String source) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return android.text.Html.fromHtml(source);
            else return android.text.Html.fromHtml(source, android.text.Html.FROM_HTML_MODE_LEGACY);
        }
    }

    public static class StatFs {
        public static long getBlockSize(android.os.StatFs statFs) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) return statFs.getBlockSizeLong();
            else return statFs.getBlockSize();
        }

        public static long getBlockCount(android.os.StatFs statFs) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) return statFs.getBlockCountLong();
            else return statFs.getBlockCount();
        }

        public static long getAvailableBlocks(android.os.StatFs statFs) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) return statFs.getAvailableBlocksLong();
            else return statFs.getAvailableBlocks();
        }
    }

    public static class TextView {
        public static void setTextAppearance(android.widget.TextView textView, Context context, int resId) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) textView.setTextAppearance(context, resId);
            else textView.setTextAppearance(resId);
        }
    }
}
