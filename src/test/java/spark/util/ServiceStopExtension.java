package spark.util;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import spark.Service;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * Stops one or more directly-held {@link Service} instances after all tests in the class,
 * with a bounded wait rather than the unbounded {@code Service#awaitStop()}.
 * <p>Register with a static field:
 * <pre>{@code
 * @RegisterExtension
 * static ServiceStopExtension ext = new ServiceStopExtension(() -> first, () -> second);
 * }</pre>
 * Suppliers are used rather than the {@code Service} instances themselves because the
 * instances are typically still null when this field initializes - they're usually assigned
 * later, inside {@code @BeforeAll}. The suppliers are only invoked once {@code afterAll} runs,
 * by which point the instances are guaranteed to have been created.
 */
public class ServiceStopExtension implements AfterAllCallback {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final List<Supplier<Service>> serviceSuppliers;

    @SafeVarargs
    public ServiceStopExtension(Supplier<Service>... serviceSuppliers) {
        this.serviceSuppliers = List.of(serviceSuppliers);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        var services = serviceSuppliers.stream().map(Supplier::get).toList();
        services.forEach(Service::stop);
        services.forEach(service -> service.awaitStop(DEFAULT_TIMEOUT));
    }
}
