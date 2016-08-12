package com.github.sgwhp.openapm.agent.util;

import java.io.*;

/**
 * Created by wuhongping on 15-11-23.
 */
public class StreamUtil {
    public static final int BUFFER_SIZE = 8192;

    public static int copy(InputStream is, OutputStream os)
            throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        int count = 0;
        int i;
        while ((i = is.read(buf)) != -1) {
            os.write(buf, 0, i);
            count += i;
        }
        return count;
    }

    public static void closeInputStreamIgnoreException(InputStream is){
        if(is != null){
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void writeToFile(byte[] bytes, String className) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File("/Users/rarshion/AndroidStudioProjects/Android_pro/openapm/class_file/" + className + ".class"));
            //fos = new FileOutputStream(new File("E:\\openapm\\wocaonima.class"));
            fos.write(bytes);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
