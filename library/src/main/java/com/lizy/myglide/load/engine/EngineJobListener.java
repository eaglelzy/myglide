package com.lizy.myglide.load.engine;

import com.lizy.myglide.load.Key;

/**
 * Created by lizy on 16-5-5.
 */
public interface EngineJobListener {
  void onEngineJobComplete(Key key, EngineResource<?> resource);

  void onEngineJobCancelled(EngineJob engineJob, Key key);
}
