package com.github.sgwhp.openapm.sample.util;

/**
 * Created by user on 2016/8/1.
 */
import android.util.*;

public class AndroidEncoder implements Encoder
{
    public String encode(final byte[] bytes) {
        return Base64.encodeToString(bytes, 0);
    }
}
