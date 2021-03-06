package com.lizy.myglide.load.model;

import android.content.Context;
import android.support.v4.util.Pools;

import com.lizy.myglide.Registry;
import com.lizy.myglide.util.pool.FactoryPools;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by lizy on 16-4-27.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class MultiModelLoaderFactoryTest {
    @Mock ModelLoaderFactory<String, String> firstFactory;
    @Mock ModelLoader<String, String> firstModelLoader;
    @Mock MultiModelLoaderFactory.Factory multiModelLoaderFactory;
    @Mock ModelLoaderFactory<String, String> secondeFactory;
    @Mock ModelLoader<String, String> secondeModelLoader;

    @Rule public ExpectedException exception = ExpectedException.none();

    private Pools.Pool<List<Exception>> exceptionListPool;
    private MultiModelLoaderFactory multiFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        exceptionListPool = FactoryPools.threadSafeList();
        multiFactory = new MultiModelLoaderFactory(RuntimeEnvironment.application,
                exceptionListPool, multiModelLoaderFactory);
        when(firstFactory.build(anyContext(), eq(multiFactory))).thenReturn(firstModelLoader);
        when(secondeFactory.build(anyContext(), eq(multiFactory))).thenReturn(secondeModelLoader);
    }

    @Test
    public void testAppend_addsModelLoaderForModelClass() {
        multiFactory.append(String.class, String.class, firstFactory);

        List<ModelLoader<String, ?>> loaders = multiFactory.build(String.class);
        assertThat(loaders).containsExactly(firstModelLoader);
    }

    @Test
    public void testAppend_addModelLoaderForModelAndDataClass() {
        multiFactory.append(String.class, String.class, firstFactory);

        ModelLoader<String, ?> modelLoader = multiFactory.build(String.class, String.class);
        assertThat(modelLoader).isEqualTo(firstModelLoader);
    }

    @Test
    public void testPrepend_addsModelLoaderForModelClass() {
        multiFactory.prepend(String.class, String.class, firstFactory);

        List<ModelLoader<String, ?>> modelLoaders = multiFactory.build(String.class);
        assertThat(modelLoaders).containsExactly(firstModelLoader);
    }

    @Test
    public void testPrepend_addsModelLoaderForModelAndDataClass() {
        multiFactory.prepend(String.class, String.class, firstFactory);

        ModelLoader<String, String> modelLoader = multiFactory.build(String.class, String.class);
        assertThat(modelLoader).isEqualTo(firstModelLoader);
    }

    @Test
    public void testReplace_addsModelLoaderForModelClass() {
        multiFactory.replace(String.class, String.class, firstFactory);

        List<ModelLoader<String, ?>> modelLoaders = multiFactory.build(String.class);
        assertThat(modelLoaders).containsExactly(firstModelLoader);
    }

    @Test
    public void testReplace_addsModelLoaderForModelAndDataClasses() {
        multiFactory.replace(String.class, String.class, firstFactory);

        ModelLoader<String, String> modelLoader = multiFactory.build(String.class, String.class);
        assertThat(modelLoader).isEqualTo(firstModelLoader);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReplace_returnsPreviouslyRegisteredFactories_withModelAndDataClasses() {
        ModelLoaderFactory<String, String> firstOtherFactory = mock(ModelLoaderFactory.class);
        ModelLoaderFactory<String, String> secondOtherFactory = mock(ModelLoaderFactory.class);
        multiFactory.append(String.class, String.class, firstOtherFactory);
        multiFactory.append(String.class, String.class, secondOtherFactory);

        List<ModelLoaderFactory<String, String>> removed =
                multiFactory.replace(String.class, String.class, firstFactory);
        assertThat(removed).containsExactly(firstOtherFactory, secondOtherFactory);
    }

    @Test
    public void testReplace_removesPreviouslyRegisteredFactories_withModelAndDataClasses() {
        appendFactoryFor(String.class, String.class);
        appendFactoryFor(String.class, String.class);

        multiFactory.replace(String.class, String.class, firstFactory);

        List<ModelLoader<String, ?>> modelLoaders = multiFactory.build(String.class);
        assertThat(modelLoaders).containsExactly(firstModelLoader);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemove_returnsPreviouslyRegisteredFactories_withModelAndDataClasses() {
        ModelLoaderFactory<String, String> other = mock(ModelLoaderFactory.class);
        multiFactory.append(String.class, String.class, other);
        multiFactory.append(String.class, String.class, firstFactory);

        List<ModelLoaderFactory<String, String>> removed =
                multiFactory.remove(String.class, String.class);
        assertThat(removed).containsExactly(firstFactory, other);
    }

    @Test
    public void testRemove_removesPreviouslyRegisteredFactories_withModelAndDataClasses() {
        appendFactoryFor(String.class, String.class);
        appendFactoryFor(String.class, String.class);

        multiFactory.remove(String.class, String.class);

        List<ModelLoader<String, ?>> modelLoaders = multiFactory.build(String.class);
        assertThat(modelLoaders).isEmpty();
    }

    @Test
    public void testBuild_withModelClass_returnsMultipleModelLoaders_ofGivenModelAndDataClasses() {
        ModelLoader<String, String> otherLoader = appendFactoryFor(String.class, String.class);
        multiFactory.append(String.class, String.class, firstFactory);

        List<ModelLoader<String, ?>> modelLoaders = multiFactory.build(String.class);
        assertThat(modelLoaders).containsExactly(otherLoader, firstModelLoader);
    }

    @Test
    public void
    testBuild_withModelClass_returnsMultipleModelLoaders_ofGivenModelClassWithDifferentDataClasses() {
        ModelLoader<String, Integer> otherLoader = appendFactoryFor(String.class, Integer.class);
        multiFactory.append(String.class, String.class, firstFactory);

        List<ModelLoader<String, ?>> modelLoaders = multiFactory.build(String.class);
        assertThat(modelLoaders).containsExactly(otherLoader, firstModelLoader);
    }

    @Test
    public void testBuild_withModelClass_excludesModelLoadersForOtherModelClasses() {
        multiFactory.append(String.class, String.class, firstFactory);
        List<ModelLoader<Integer, ?>> modelLoaders = multiFactory.build(Integer.class);
        assertThat(modelLoaders).doesNotContain(firstModelLoader);
    }

    @Test
    public void
    testBuild_withModelAndDataClasses_returnsMultipleModelLoaders_ofGivenModelAndDataClasses() {
        ModelLoader<String, String> otherLoader = appendFactoryFor(String.class, String.class);
        multiFactory.append(String.class, String.class, firstFactory);

        List<ModelLoader<String, String>> modelLoaders = buildModelLoaders(String.class, String.class);
        assertThat(modelLoaders).containsExactly(otherLoader, firstModelLoader);
    }

    @Test
    public void testBuild_withModelAndDataClasses_excludesModelLoadersForOtherDataClasses() {
        multiFactory.append(String.class, String.class, firstFactory);

        exception.expect(Registry.NoModelLoaderAvailableException.class);
        multiFactory.build(String.class, Integer.class);
    }

    @Test
    public void testBuild_withModelAndDataClasses_excludesModelLoadersForOtherModelClasses() {
        multiFactory.append(String.class, String.class, firstFactory);

        exception.expect(Registry.NoModelLoaderAvailableException.class);
        multiFactory.build(Integer.class, String.class);
    }

    @Test
    public void testBuild_withModelClass_doesNotMatchSubclassesOfModelClass() {
        ModelLoader<String, Object> subclass = appendFactoryFor(String.class, Object.class);
        List<ModelLoader<Object, ?>> modelLoaders = multiFactory.build(Object.class);
        assertThat(modelLoaders).doesNotContain(subclass);
    }

    @Test
    public void testBuild_withModelClass_matchesSuperclassesOfModelClass() {
        ModelLoader<Object, Object> superclass = appendFactoryFor(Object.class, Object.class);
        List<ModelLoader<String, ?>> modelLoaders = multiFactory.build(String.class);
        assertThat(modelLoaders).contains(superclass);
    }

    @Test
    public void testBuild_withModelAndDataClass_doesNotMatchSubclassesOfModelClass() {
        appendFactoryFor(String.class, Object.class);
        exception.expect(Registry.NoModelLoaderAvailableException.class);
        multiFactory.build(Object.class, Object.class);
    }

    @Test
    public void testBuild_withModelAndDataClass_doesNotMatchSubclassesOfDataClass() {
        appendFactoryFor(Object.class, String.class);
        exception.expect(Registry.NoModelLoaderAvailableException.class);
        multiFactory.build(Object.class, Object.class);
    }

    @Test
    public void testBuild_withModelAndDataClass_doesMatchSuperclassesOfModelClass() {
        ModelLoader<Object, Object> firstSuperClass = appendFactoryFor(Object.class, Object.class);
        ModelLoader<Object, Object> secondSuperClass = appendFactoryFor(Object.class, Object.class);
        List<ModelLoader<String, Object>> modelLoaders = buildModelLoaders(String.class, Object.class);
        assertThat(modelLoaders).containsExactly(firstSuperClass, secondSuperClass);
    }

    @Test
    public void testBuild_withModelAndDataClass_matchesSuperclassesOfDataClass() {
        ModelLoader<Object, Object> firstSuperClass = appendFactoryFor(Object.class, Object.class);
        ModelLoader<Object, Object> secondSuperClass = appendFactoryFor(Object.class, Object.class);
        List<ModelLoader<Object, String>> modelLoaders = buildModelLoaders(Object.class, String.class);
        assertThat(modelLoaders).containsExactly(firstSuperClass, secondSuperClass);
    }

    @Test
    public void testBuild_withModelAndDataClass_matchesSuperclassOfModelAndDataClass() {
        ModelLoader<Object, Object> firstSuperclass = appendFactoryFor(Object.class, Object.class);
        ModelLoader<Object, Object> secondSuperclass = appendFactoryFor(Object.class, Object.class);
        List<ModelLoader<String, String>> modelLoaders = buildModelLoaders(String.class, String.class);
        assertThat(modelLoaders).containsExactly(firstSuperclass, secondSuperclass);
    }

    @Test
    public void testBuild_respectsAppendOrder() {
        ModelLoader<String, String> first = appendFactoryFor(String.class, String.class);
        ModelLoader<String, String> second = appendFactoryFor(String.class, String.class);
        ModelLoader<String, String> third = appendFactoryFor(String.class, String.class);
        List<ModelLoader<String, String>> modelLoaders = buildModelLoaders(String.class, String.class);
        assertThat(modelLoaders).containsExactly(first, second, third).inOrder();
    }

    @Test
    public void testBuild_respectsPrependOrder() {
        ModelLoader<String, String> first = prependFactoryFor(String.class, String.class);
        ModelLoader<String, String> second = prependFactoryFor(String.class, String.class);
        ModelLoader<String, String> third = prependFactoryFor(String.class, String.class);
        List<ModelLoader<String, String>> modelLoaders = buildModelLoaders(String.class, String.class);
        assertThat(modelLoaders).containsExactly(third, second, first).inOrder();
    }
    @SuppressWarnings("unchecked")
    private <X, Y> List<ModelLoader<X, Y>> buildModelLoaders(Class<X> modelClass,
                                                             Class<Y> dataClass) {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        multiFactory.build(modelClass, dataClass);
        verify(multiModelLoaderFactory).build(captor.capture(), eq(exceptionListPool));

        List<ModelLoader> captured = captor.getValue();
        List<ModelLoader<X, Y>> result = new ArrayList<>(captured.size());
        for (ModelLoader modelLoader : captured) {
            result.add(modelLoader);
        }
        return result;
    }

    private <Model, Data> ModelLoader<Model, Data> registerFactoryFor(Class<Model> modelClass,
                             Class<Data> dataClass,
                             boolean append) {
        ModelLoaderFactory<Model, Data> factory = mock(ModelLoaderFactory.class);
        ModelLoader<Model, Data> modelLoader = mock(ModelLoader.class);
        when(factory.build(anyContext(), eq(multiFactory))).thenReturn(modelLoader);
        if (append) {
            multiFactory.append(modelClass, dataClass, factory);
        } else {
            multiFactory.prepend(modelClass, dataClass, factory);
        }
        return modelLoader;
    }

    private <X, Y> ModelLoader<X, Y> appendFactoryFor(Class<X> modelClass, Class<Y> dataClass) {
        return registerFactoryFor(modelClass, dataClass, true /*append*/);
    }

    private <X, Y> ModelLoader<X, Y> prependFactoryFor(Class<X> modelClass, Class<Y> dataClass) {
        return registerFactoryFor(modelClass, dataClass, false /*append*/);
    }

    private static Context anyContext() {
        return any(Context.class);
    }
}
