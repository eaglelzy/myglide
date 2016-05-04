package com.lizy.myglide.manager;

import com.lizy.myglide.request.target.Target;
import com.lizy.myglide.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by lizy on 16-5-4.
 */
public class TargetTracker implements LifecycleListener {

    private final Set<Target<?>> targets =
            Collections.newSetFromMap(new WeakHashMap<Target<?>, Boolean>());

    public void track(Target<?> target) {
        targets.add(target);
    }

    public void untrack(Target<?> target) {
        targets.remove(target);
    }

    @Override
    public void onStart() {
        for (Target<?> target : Util.getSnapshot(targets)) {
            target.onStart();
        }
    }

    @Override
    public void onStop() {
        for (Target<?> target : Util.getSnapshot(targets)) {
            target.onStop();
        }
    }

    @Override
    public void onDestroy() {
        for (Target<?> target : Util.getSnapshot(targets)) {
            target.onDestroy();
        }
    }

    public List<Target<?>> getAll() {
        return new ArrayList<>(targets);
    }

    public void clear() {
        targets.clear();
    }

}
