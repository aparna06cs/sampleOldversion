package com.sampleOldversion.demo;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.kubernetes.config.ConfigMapPropertySource;
import org.springframework.cloud.kubernetes.config.ConfigMapPropertySourceLocator;
import org.springframework.cloud.kubernetes.config.SecretsPropertySource;
import org.springframework.cloud.kubernetes.config.SecretsPropertySourceLocator;
import org.springframework.cloud.kubernetes.config.reload.ConfigReloadProperties;
import org.springframework.cloud.kubernetes.config.reload.ConfigurationChangeDetector;
import org.springframework.cloud.kubernetes.config.reload.ConfigurationUpdateStrategy;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.scheduling.annotation.Scheduled;

import io.fabric8.kubernetes.client.KubernetesClient;

public class CustomPollingConfigurationChangeDetector extends ConfigurationChangeDetector {

	protected Log log = LogFactory.getLog(getClass());

	private ConfigMapPropertySourceLocator configMapPropertySourceLocator;

	private SecretsPropertySourceLocator secretsPropertySourceLocator;

	public CustomPollingConfigurationChangeDetector(AbstractEnvironment environment,
			ConfigReloadProperties properties, KubernetesClient kubernetesClient,
			ConfigurationUpdateStrategy strategy,
			ConfigMapPropertySourceLocator configMapPropertySourceLocator,
			SecretsPropertySourceLocator secretsPropertySourceLocator) {
		super(environment, properties, kubernetesClient, strategy);

		this.configMapPropertySourceLocator = configMapPropertySourceLocator;
		this.secretsPropertySourceLocator = secretsPropertySourceLocator;
	}

	@PostConstruct
	public void init() {
		log.info("Kubernetes polling configuration change detector activated");
	}

	@Scheduled(initialDelayString = "3000", fixedDelayString = "3000")
	public void executeCycle() {
        log.info("Scheduled run initiated");
		boolean changedConfigMap = false;
		if (properties.isMonitoringConfigMaps()) {
			
			 log.info("Scheduled run initiated");
			List<? extends MapPropertySource> currentConfigMapSources = findPropertySources(
					ConfigMapPropertySource.class);

			if (!currentConfigMapSources.isEmpty()) {
				changedConfigMap = changed(
						locateMapPropertySources(configMapPropertySourceLocator,
								environment),
						currentConfigMapSources);
			}
		}

		boolean changedSecrets = false;
		if (properties.isMonitoringSecrets()) {
			MapPropertySource currentSecretSource = findPropertySource(
					SecretsPropertySource.class);
			if (currentSecretSource != null) {
				MapPropertySource newSecretSource = secretsPropertySourceLocator
						.locate(environment);
				changedSecrets = changed(currentSecretSource, newSecretSource);
			}
		}

		if (changedConfigMap || changedSecrets) {
			 log.info("Detected Changes in the configMap/Secrets for polling Mode");
			reloadProperties();
		}
	}
	

}
