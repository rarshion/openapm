package com.github.sgwhp.openapm.sample;

/**
 * Created by user on 2016/8/1.
 */
public class testMesurement {

    public void calculate(){
        try {
            Thread.sleep(500L);
            System.out.println("enter exception");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
