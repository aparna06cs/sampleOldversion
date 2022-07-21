package com.sampleOldversion.demo;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.cloud.kubernetes.config.ConfigMapPropertySource;
import org.springframework.cloud.kubernetes.config.ConfigMapPropertySourceLocator;
import org.springframework.cloud.kubernetes.config.SecretsPropertySource;
import org.springframework.cloud.kubernetes.config.SecretsPropertySourceLocator;
import org.springframework.cloud.kubernetes.config.reload.ConfigReloadProperties;
import org.springframework.cloud.kubernetes.config.reload.ConfigurationChangeDetector;
import org.springframework.cloud.kubernetes.config.reload.ConfigurationUpdateStrategy;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MapPropertySource;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

public class CustomEventBasedConfigurationChangeDetector extends ConfigurationChangeDetector {
	private ConfigMapPropertySourceLocator configMapPropertySourceLocator;

	private SecretsPropertySourceLocator secretsPropertySourceLocator;

	private Map<String, Watch> watches;

	public CustomEventBasedConfigurationChangeDetector(AbstractEnvironment environment,
			ConfigReloadProperties properties, KubernetesClient kubernetesClient,
			ConfigurationUpdateStrategy strategy,
			ConfigMapPropertySourceLocator configMapPropertySourceLocator,
			SecretsPropertySourceLocator secretsPropertySourceLocator) {
		super(environment, properties, kubernetesClient, strategy);
		log.info("Constructor intiated");
		this.configMapPropertySourceLocator = configMapPropertySourceLocator;
		this.secretsPropertySourceLocator = secretsPropertySourceLocator;
		this.watches = new HashMap<>();
	}
	@PostConstruct
	public void watch() {
		boolean activated = false;
		log.info("Monitoring of the configMap is enabled by default 12345");
		if (properties.isMonitoringConfigMaps()) {
			log.info("Monitoring of the configMap is enabled by default");
			try {
				String name = "config-maps-watch";
				log.info("Monitoring of the configMap is enabled by default");
				watches.put(name,
						kubernetesClient.configMaps().watch(new Watcher<ConfigMap>() {
							
							@Override
							public void eventReceived(Action action,
									ConfigMap configMap) {
								log.info("Event Received :::"+action);
								onEvent(configMap);
							}

							@Override
							public void onClose(KubernetesClientException e) {
								log.info("closing event"+e.getStackTrace());
							}
						}));
				activated = true;
				log.info("Added new Kubernetes watch: " + name);
				log.info("watch map "+watches);
			}
			catch (Exception e) {
				log.info("Error establing the connection");
				log.error(
						"Error while establishing a connection to watch config maps: configuration may remain stale",
						e);
			}
		}

		if (properties.isMonitoringSecrets()) {
			try {
				activated = false;
				String name = "secrets-watch";
				watches.put(name, kubernetesClient.secrets().watch(new Watcher<Secret>() {
					@Override
					public void eventReceived(Action action, Secret secret) {
						onEvent(secret);
					}

					@Override
					public void onClose(KubernetesClientException e) {
					}
				}));
				activated = true;
				log.info("Added new Kubernetes watch: " + name);
			}
			catch (Exception e) {
				log.error(
						"Error while establishing a connection to watch secrets: configuration may remain stale",
						e);
			}
		}

		if (activated) {
			log.info("Kubernetes event-based configuration change detector activated");
		}
	}
	
	@PreDestroy
	public void unwatch() {
		if (this.watches != null) {
			for (Map.Entry<String, Watch> entry : this.watches.entrySet()) {
				try {
					log.info("Closing the watch " + entry.getKey());
					entry.getValue().close();

				}
				catch (Exception e) {
					log.error("Error while closing the watch connection", e);
				}
			}
		}
	}

	private void onEvent(ConfigMap configMap) {
		boolean changed = changed(
				locateMapPropertySources(configMapPropertySourceLocator, environment),
				findPropertySources(ConfigMapPropertySource.class));
		log.info("Detected the change or not::::"+changed);
		if (changed) {
			log.info("Detected change in config maps");
			reloadProperties();
		}
	}

	private void onEvent(Secret secret) {
		MapPropertySource currentSecretSource = findPropertySource(
				SecretsPropertySource.class);
		if (currentSecretSource != null) {
			MapPropertySource newSecretSource = secretsPropertySourceLocator
					.locate(environment);
			if (changed(currentSecretSource, newSecretSource)) {
				log.info("Detected change in secrets");
				reloadProperties();
			}
		}
	}


}
