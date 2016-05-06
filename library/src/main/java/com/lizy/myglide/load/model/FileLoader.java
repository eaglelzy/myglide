package com.lizy.myglide.load.model;

import android.content.Context;

import com.lizy.myglide.Priority;
import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.data.DataFetcher;
import com.lizy.myglide.signature.ObjectKey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lizy on 16-5-6.
 */
public class FileLoader<Data> implements ModelLoader<File, Data> {
    private static final String TAG = "FileLoader";

    private final FileOpener<Data> fileOpener;

    public FileLoader(FileOpener<Data> fileOpener) {
        this.fileOpener = fileOpener;
    }

    @Override
    public LoadData<Data> buildLoadData(File model, int width, int height, Options Options) {
        return new LoadData<>(new ObjectKey(model), new FileFetcher<>(model, fileOpener));
    }

    @Override
    public boolean handles(File file) {
        return true;
    }

    interface FileOpener<Data> {
        Data open(File file) throws FileNotFoundException;
        void close(Data data) throws IOException;
        Class<Data> getDataClass();
    }

    public static class FileFetcher<Data> implements DataFetcher<Data> {

        private final File file;
        private final FileOpener<Data> opener;
        private Data data;

        public FileFetcher(File file, FileOpener<Data> opener) {
            this.file = file;
            this.opener = opener;
        }

        @Override
        public void loadData(Priority priority, DataCallback<? super Data> callback) {
            try {
                data = opener.open(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                callback.onLoadFailed(e);
                return;
            }
            callback.onDataReady(data);
        }

        @Override
        public void cleanup() {
            try {
                opener.close(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void cancel() {

        }

        @Override
        public Class<Data> getDataClass() {
            return opener.getDataClass();
        }

        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }

    public static class Factory<Data> implements ModelLoaderFactory<File, Data> {

        private final FileOpener<Data> opener;

        public Factory(FileOpener<Data> opener) {
            this.opener = opener;
        }

        @Override
        public ModelLoader<File, Data> build(Context context, MultiModelLoaderFactory multiFactory) {
            return new FileLoader<>(opener);
        }

        @Override
        public void teardown() {

        }
    }

    public static class StreamFactory extends Factory<InputStream> {
        public StreamFactory() {
            super(new FileOpener<InputStream>() {
                @Override
                public InputStream open(File file) throws FileNotFoundException {
                    return new FileInputStream(file);
                }

                @Override
                public void close(InputStream inputStream) throws IOException {
                    inputStream.close();
                }

                @Override
                public Class<InputStream> getDataClass() {
                    return InputStream.class;
                }
            });
        }
    }

}
