package net.thucydides.core.webdriver;

import net.thucydides.core.annotations.TestCaseAnnotations;
import net.thucydides.core.guice.Injectors;
import net.thucydides.core.pages.Pages;
import net.thucydides.core.steps.StepAnnotations;
import net.thucydides.core.steps.StepFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.SessionId;

/**
 * A utility class that provides services to initialize web testing and reporting-related fields in arbitrary objects.
 * It is designed to help integrate Thucydides into other testing tools such as Cucumber.
 */
public class ThucydidesWebDriverSupport {

    private static final ThreadLocal<WebdriverManager> webdriverManagerThreadLocal = new ThreadLocal<WebdriverManager>();
    private static final ThreadLocal<Pages> pagesThreadLocal = new ThreadLocal<Pages>();
    private static final ThreadLocal<StepFactory> stepFactoryThreadLocal = new ThreadLocal<StepFactory>();

    public static void initialize() {
        initialize(null);
    }

    public static void initialize(String requestedDriver) {
        initialize(Injectors.getInjector().getInstance(WebdriverManager.class), requestedDriver);
    }

    public static void initialize(WebdriverManager webdriverManager, String requestedDriver) {
        setupWebdriverManager(webdriverManager, requestedDriver);
        initPagesObjectUsing(getDriver());
        initStepFactoryUsing(getPages());
    }

    public static boolean isInitialised() {
        return (webdriverManagerThreadLocal.get() != null);
    }

    private static boolean webdriversInitialized() {
        return (webdriverManagerThreadLocal.get() != null);
    }

    private static void lazyInitalize() {
        if (!webdriversInitialized()) {
            initialize();
        }
    }

    public static void initializeFieldsIn(final Object testCase) {
        injectDriverInto(testCase);
        injectAnnotatedPagesObjectInto(testCase);
    }

    public static StepFactory getStepFactory() {
        lazyInitalize();
        return stepFactoryThreadLocal.get();
    }

    public static void useDriver(WebDriver driver) {
        getWebdriverManager().registerDriver(driver);
    }


    public static WebDriver getDriver() {
        return getWebdriverManager().getWebdriver();
    }

    public static void closeCurrentDrivers() {
        if (webdriversInitialized()) {
            getWebdriverManager().closeAllCurrentDrivers();
        }
    }

    public static void closeAllDrivers() {
        if (webdriversInitialized()) {
            getWebdriverManager().closeAllDrivers();
        }
    }
//
//    private static WebdriverManager configuredWebdriverManager() {
//        Injectors.getInjector().getInstance(WebdriverManager.class);
//    }

    private static void setupWebdriverManager(WebdriverManager webdriverManager , String requestedDriver) {
//        WebdriverManager webdriverManager = configuredWebdriverManager();//Injectors.getInjector().getInstance(WebdriverManager.class);
        webdriverManager.overrideDefaultDriverType(requestedDriver);
        webdriverManagerThreadLocal.set(webdriverManager);
    }

    private static void initStepFactoryUsing(final Pages pagesObject) {
        stepFactoryThreadLocal.set(new StepFactory(pagesObject));
    }

    public static WebdriverManager getWebdriverManager(WebDriverFactory webDriverFactory, Configuration configuration) {
        initialize(new SerenityWebdriverManager(webDriverFactory, configuration), "");
        return webdriverManagerThreadLocal.get();
    }

    public static WebdriverManager getWebdriverManager() {
        lazyInitalize();
        return webdriverManagerThreadLocal.get();
    }

    private static void initPagesObjectUsing(final WebDriver driver) {
        pagesThreadLocal.set(new Pages(driver));
    }

    public static Pages getPages() {
        lazyInitalize();
        return pagesThreadLocal.get();
    }

    /**
     * Instantiate the @Managed-annotated WebDriver instance with current WebDriver.
     */
    protected static void injectDriverInto(final Object testCase) {
        TestCaseAnnotations.forTestCase(testCase).injectDrivers(getWebdriverManager());
    }

    /**
     * Instantiates the @ManagedPages-annotated Pages instance using current WebDriver.
     */
    protected static void injectAnnotatedPagesObjectInto(final Object testCase) {
        StepAnnotations.injectOptionalAnnotatedPagesObjectInto(testCase, getPages());
    }

    public static <T extends WebDriver> T getProxiedDriver() {
        return (T) ((WebDriverFacade) getDriver()).getProxiedDriver();
    }

    public static Class<? extends WebDriver> getDriverClass() {
        return  ((WebDriverFacade) getDriver()).getDriverClass();
    }

    public static SessionId getSessionId() {
        return getWebdriverManager().getSessionId();

    }

    public static String getCurrentDriverName() {
        if (getWebdriverManager() == null) {
            return "";
        }
        return getWebdriverManager().getCurrentDriverType();
    }

    public static String getDriversUsed() {
        if (getWebdriverManager() == null) {
            return "";
        }
        return getWebdriverManager().getActiveDriverTypes().get(0);
    }

    public static boolean isDriverInstantiated() {
        return getWebdriverManager().isDriverInstantiated();
    }
}
