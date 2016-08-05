package com.github.sgwhp.openapm.sample.crashes;

import com.github.sgwhp.openapm.sample.harvest.crash.Crash;
import java.util.List;

/**
 * Created by user on 2016/8/1.
 */
public interface CrashStore {
    void store(Crash p0);

    List<Crash> fetchAll();

    int count();

    void clear();

    void delete(Crash p0);
}
