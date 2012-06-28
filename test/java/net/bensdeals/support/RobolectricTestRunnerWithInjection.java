package net.bensdeals.support;

import com.google.inject.name.Names;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import net.bensdeals.activity.DealsApplication;
import net.bensdeals.network.ImageLoader;
import net.bensdeals.provider.CacheDirProvider;
import net.bensdeals.util.TestImageLoader;
import org.junit.runners.model.InitializationError;
import roboguice.config.AbstractAndroidModule;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class RobolectricTestRunnerWithInjection extends RobolectricTestRunner {
    public RobolectricTestRunnerWithInjection(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    public void beforeTest(Method method) {
        super.beforeTest(method);
    }

    @Override
    public void prepareTest(Object test) {
        DealsApplication application = (DealsApplication) Robolectric.application;
        application.setModule(new TestApplicationModule());
        application.getInjector().injectMembers(test);
    }

    public static class TestApplicationModule extends AbstractAndroidModule {
        @Override
        protected void configure() {
            bind(File.class).annotatedWith(Names.named("cacheDir")).toProvider(CacheDirProvider.class);
            bind(ExecutorService.class).annotatedWith(Names.named("download")).toInstance(newFixedThreadPool(3));
            bind(ExecutorService.class).annotatedWith(Names.named("draw")).toInstance(newFixedThreadPool(2));
            bind(ImageLoader.class).to(TestImageLoader.class);
        }
    }
}

