package com.sampleOldversion.demo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.cloud.kubernetes.config.reload.ConfigReloadProperties;
import org.springframework.cloud.kubernetes.config.reload.ConfigurationUpdateStrategy;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import io.fabric8.kubernetes.client.KubernetesClient;

public abstract class CustomChangeDetector {


	protected Log log = LogFactory.getLog(getClass());

	protected ConfigurableEnvironment environment;

	protected ConfigReloadProperties properties;

	protected KubernetesClient kubernetesClient;

	protected ConfigurationUpdateStrategy strategy;

	public CustomChangeDetector(ConfigurableEnvironment environment,
			ConfigReloadProperties properties, KubernetesClient kubernetesClient,
			ConfigurationUpdateStrategy strategy) {
		this.environment = environment;
		this.properties = properties;
		this.kubernetesClient = kubernetesClient;
		this.strategy = strategy;
	}

	@PreDestroy
	public void shutdown() {
		// Ensure the kubernetes client is cleaned up from spare threads when shutting
		// down
		kubernetesClient.close();
	}

	public void reloadProperties() {
		log.info("Reloading using strategy: " + strategy.getName());
		strategy.reload();
	}

	/**
	 * Determines if two property sources are different.
	 */
	protected boolean changed(MapPropertySource mp1, MapPropertySource mp2) {
		if (mp1 == mp2)
			return false;
		if (mp1 == null && mp2 != null || mp1 != null && mp2 == null)
			return true;

		Map<String, Object> s1 = mp1.getSource();
		Map<String, Object> s2 = mp2.getSource();

		return s1 == null ? s2 != null : !s1.equals(s2);
	}

	protected boolean changed(List<? extends MapPropertySource> l1,
			List<? extends MapPropertySource> l2) {

		if (l1.size() != l2.size()) {
			log.debug("The current number of Confimap PropertySources does not match "
					+ "the ones loaded from the Kubernetes - No reload will take place");
			return false;
		}

		for (int i = 0; i < l1.size(); i++) {
			if (changed(l1.get(i), l2.get(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Finds one registered property source of the given type, logging a warning if
	 * multiple property sources of that type are available.
	 */
	protected <S extends PropertySource<?>> S findPropertySource(Class<S> sourceClass) {
		List<S> sources = findPropertySources(sourceClass);
		if (sources.size() == 0) {
			return null;
		}
		if (sources.size() > 1) {
			log.warn("Found more than one property source of type " + sourceClass);
		}
		return sources.get(0);
	}

	/**
	 * Finds all registered property sources of the given type.
	 */
	protected <S extends PropertySource<?>> List<S> findPropertySources(
			Class<S> sourceClass) {
		List<S> managedSources = new LinkedList<>();

		LinkedList<PropertySource<?>> sources = toLinkedList(
				environment.getPropertySources());
		while (!sources.isEmpty()) {
			PropertySource<?> source = sources.pop();
			if (source instanceof CompositePropertySource) {
				CompositePropertySource comp = (CompositePropertySource) source;
				sources.addAll(comp.getPropertySources());
			}
			else if (sourceClass.isInstance(source)) {
				managedSources.add(sourceClass.cast(source));
			}
		}

		return managedSources;
	}

	private <E> LinkedList<E> toLinkedList(Iterable<E> it) {
		LinkedList<E> list = new LinkedList<E>();
		for (E e : it) {
			list.add(e);
		}
		return list;
	}

	/**
	 * Returns a list of MapPropertySource that correspond to the current state of the
	 * system This only handles the PropertySource objects that are returned
	 */
	protected List<MapPropertySource> locateMapPropertySources(
			PropertySourceLocator propertySourceLocator, Environment environment) {

		List<MapPropertySource> result = new ArrayList<>();
		PropertySource propertySource = propertySourceLocator.locate(environment);
		if (propertySource instanceof MapPropertySource) {
			result.add((MapPropertySource) propertySource);
		}
		else if (propertySource instanceof CompositePropertySource) {
			result.addAll(((CompositePropertySource) propertySource).getPropertySources()
					.stream().filter(p -> p instanceof MapPropertySource)
					.map(p -> (MapPropertySource) p).collect(Collectors.toList()));
		}
		else {
			log.debug("Found property source that cannot be handled: "
					+ propertySource.getClass());
		}

		return result;
	}


	

}
