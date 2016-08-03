package com.github.sgwhp.openapm.sample;

/**
 * Created by user on 2016/8/3.
 */
public class TestMesurement {

    public TestMesurement(){

    }

    public void calculate(){
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            System.out.println("wocaonima");
        }
    }


}
