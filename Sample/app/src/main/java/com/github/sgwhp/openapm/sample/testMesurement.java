package com.github.sgwhp.openapm.sample;

/**
 * Created by user on 2016/8/1.
 */
public class testMesurement {

    public void calculate(){
        //Thread.sleep(500L);
        System.out.println("enter exception");

        //throw new RuntimeException("calculate with exception");
        /*
        final long currentTimeMillis = System.currentTimeMillis();
        final boolean b = false;
        System.out.println("start: " + currentTimeMillis);
        try {
            System.out.println("enter exception");
            final long currentTimeMillis2 = System.currentTimeMillis();
            System.out.println("end: " + currentTimeMillis2);
            System.out.println("[hello<->MeMethodAdapter<->rarshion]\ntime cost :" + (currentTimeMillis2 - currentTimeMillis) + " has error: " + b);
        }
        catch (Throwable t) {
            final boolean b2 = true;
            final long currentTimeMillis3 = System.currentTimeMillis();
            System.out.println("end: " + currentTimeMillis3);
            System.out.println("[hello<->MeMethodAdapter<->rarshion]\ntime cost :" + (currentTimeMillis3 - currentTimeMillis) + " has error: " + b2);
            throw t;
        }
        */
    }
}
