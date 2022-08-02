package com.sampleOldversion.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshEndpointAutoConfiguration;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.cloud.kubernetes.config.ConfigMapPropertySourceLocator;
import org.springframework.cloud.kubernetes.config.SecretsPropertySourceLocator;
import org.springframework.cloud.kubernetes.config.reload.ConfigReloadProperties;
import org.springframework.cloud.kubernetes.config.reload.ConfigurationChangeDetector;
import org.springframework.cloud.kubernetes.config.reload.ConfigurationUpdateStrategy;
import org.springframework.cloud.kubernetes.config.reload.EventBasedConfigurationChangeDetector;
import org.springframework.cloud.kubernetes.config.reload.PollingConfigurationChangeDetector;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.fabric8.kubernetes.client.KubernetesClient;

@Configuration
@ConditionalOnProperty(value = "spring.cloud.kubernetes.config.enabled", matchIfMissing = true)
@AutoConfigureAfter({ RefreshEndpointAutoConfiguration.class, RefreshAutoConfiguration.class })
@EnableConfigurationProperties(ConfigReloadProperties.class)
public class NewConfigAutoReload {

	/**
	 * Configuration reload must be enabled explicitly.
	 */
	@ConditionalOnProperty(value = "spring.cloud.kubernetes.reload.enabled")
	@EnableScheduling
	@EnableAsync
	protected static class ConfigReloadAutoConfigurationBeans {

		@Autowired
		private AbstractEnvironment environment;

		@Autowired
		private KubernetesClient kubernetesClient;

		@Autowired
		private ConfigMapPropertySourceLocator configMapPropertySourceLocator;

		@Autowired
		private SecretsPropertySourceLocator secretsPropertySourceLocator;

		/**
		 * Provides a bean that listen to configuration changes and fire a reload.
		 */
		@Bean
		@ConditionalOnMissingBean
		public ConfigurationChangeDetector propertyChangeWatcher(ConfigReloadProperties properties,
				ConfigurationUpdateStrategy strategy) {
			switch (properties.getMode()) {
			case POLLING:
				System.out.println("Polling reload is triggered event occured");
				return new PollingConfigurationChangeDetector(environment, properties, kubernetesClient, strategy,
						configMapPropertySourceLocator, secretsPropertySourceLocator);
			case EVENT:
				System.out.println("Any event occured");
				return new EventBasedConfigurationChangeDetector(environment, properties, kubernetesClient,
						strategy, configMapPropertySourceLocator, secretsPropertySourceLocator);
			}
			throw new IllegalStateException("Unsupported configuration reload mode: " + properties.getMode());
		}

		/**
		 * This is the fall back method if for any issue websocket connection is closed and not able to trigger the Event
		 * Provides a bean that listen to configuration changes and fire a reload.
		 */
		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "polling.fallback.enabled", matchIfMissing = false)
		public CustomChangeDetector propertyChangeWatcherExtForPollingStrategy(ConfigReloadProperties properties,
				ConfigurationUpdateStrategy strategy) {
			return new ExtPollingConfigurationChangeDetetor(environment, properties, kubernetesClient, strategy,
					configMapPropertySourceLocator, secretsPropertySourceLocator);

		}

		/**
		 * Provides the action to execute when the configuration changes.
		 */
		@Bean
		@ConditionalOnMissingBean
		public ConfigurationUpdateStrategy configurationUpdateStrategy(ConfigReloadProperties properties,
				ConfigurableApplicationContext ctx, RestartEndpoint restarter, ContextRefresher refresher) {
			switch (properties.getStrategy()) {
			case RESTART_CONTEXT:
				return new ConfigurationUpdateStrategy(properties.getStrategy().name(), restarter::restart);
			case REFRESH:
				System.out.println("Refresh starting");
				return new ConfigurationUpdateStrategy(properties.getStrategy().name(), refresher::refresh);
			case SHUTDOWN:
				return new ConfigurationUpdateStrategy(properties.getStrategy().name(), ctx::close);
			}
			throw new IllegalStateException("Unsupported configuration update strategy: " + properties.getStrategy());
		}

	}

}
