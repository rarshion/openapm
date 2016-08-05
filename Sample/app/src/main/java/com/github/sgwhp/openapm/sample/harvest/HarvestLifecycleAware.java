package com.github.sgwhp.openapm.sample.harvest;

/**
 * Created by user on 2016/8/1.
 */
public interface HarvestLifecycleAware {
    void onHarvestStart();

    void onHarvestStop();

    void onHarvestBefore();

    void onHarvest();

    void onHarvestFinalize();

    void onHarvestError();

    void onHarvestSendFailed();

    void onHarvestComplete();

    void onHarvestConnected();

    void onHarvestDisconnected();

    void onHarvestDisabled();
}
