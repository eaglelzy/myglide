package com.lizy.myglide.load.model;

import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.data.DataFetcher;
import com.lizy.myglide.util.Preconditions;

import java.util.Collections;
import java.util.List;

/**
 * Created by lizy on 16-4-27.
 */
public interface ModelLoader<Model, Data> {

    class LoadData<Data> {
        public final Key sourceKey;
        public final List<Key> alternateKeys;
        public final DataFetcher<Data> fetcher;

        public LoadData(Key sourceKey, DataFetcher<Data> fetcher) {
            this(sourceKey, Collections.<Key>emptyList(), fetcher);
        }
        public LoadData(Key sourceKey, List<Key> alternateKeys, DataFetcher<Data> fetcher) {
            this.sourceKey = Preconditions.checkNotNull(sourceKey);
            this.alternateKeys = Preconditions.checkNotNull(alternateKeys);
            this.fetcher = Preconditions.checkNotNull(fetcher);
        }


    }

    LoadData<Data> buildLoadData(Model model, int width, int height, Options Options);

    boolean handles(Model model);
}
