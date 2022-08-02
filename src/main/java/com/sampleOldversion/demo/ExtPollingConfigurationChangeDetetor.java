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
import org.springframework.cloud.kubernetes.config.reload.ConfigurationUpdateStrategy;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.scheduling.annotation.Scheduled;

import io.fabric8.kubernetes.client.KubernetesClient;


public class ExtPollingConfigurationChangeDetetor  extends CustomChangeDetector{


	protected Log log = LogFactory.getLog(getClass());

	private ConfigMapPropertySourceLocator configMapPropertySourceLocator;

	private SecretsPropertySourceLocator secretsPropertySourceLocator;

	public ExtPollingConfigurationChangeDetetor(AbstractEnvironment environment,
			ConfigReloadProperties properties, KubernetesClient kubernetesClient,
			ConfigurationUpdateStrategy strategy,
			ConfigMapPropertySourceLocator configMapPropertySourceLocator,
			SecretsPropertySourceLocator secretsPropertySourceLocator) {
		super(environment, properties, kubernetesClient, strategy);
		log.info("Kubernetes polling configuration change detector activated initalized");
		this.configMapPropertySourceLocator = configMapPropertySourceLocator;
		this.secretsPropertySourceLocator = secretsPropertySourceLocator;
	}

	@PostConstruct
	public void init() {
		log.info("Kubernetes polling configuration change detector activated");
	}

	@Scheduled(initialDelayString = "${polling.reload.interval:15000}", fixedDelayString= "${polling.reload.interval:15000}")
	public void executeCycle() {
		boolean changedConfigMap = false;
		if (properties.isMonitoringConfigMaps()) {
			List<? extends MapPropertySource> currentConfigMapSources = findPropertySources(
					ConfigMapPropertySource.class);
			System.out.println("currentConfigMapSources"+currentConfigMapSources);
			if (!currentConfigMapSources.isEmpty()) {
				System.out.println("currentConfigMapSources is not empty");
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
