package uk.co.downthewire.jLTE.ea;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import uk.co.downthewire.jLTE.ea.chromosomes.Chromosome;
import uk.co.downthewire.jLTE.simulator.AbstractConfiguredRunnable;
import flanagan.math.PsRandom;

public class EAMain extends AbstractConfiguredRunnable<Integer> {

	static final Logger LOG = LoggerFactory.getLogger(EAMain.class);

	public static void main(String[] args) throws ConfigurationException, InterruptedException {
		Configuration configuration = new PropertiesConfiguration("ea.properties").interpolatedConfiguration();
		EAMain eaMain = new EAMain(configuration);
		eaMain.call();
	}

	public EAMain(Configuration configuration) {
		super(configuration);
		config.setProperty(EAFields.RANDOM, new PsRandom((long) 11111.11111));
	}

	@SuppressWarnings("hiding")
	private void printConfig(Configuration config) {
		Iterator<String> keyIterator = config.getKeys();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			LOG.error("{} - {}", key, config.getProperty(key));
		}
	}

	public void configureLogging() throws InterruptedException {
		MDC.put("userid", config.getString(EAFields.EXPERIMENT_PATH) + logName(config));
		Thread.sleep(5000);
	}

	private static String logName(Configuration config) {
		final SimpleDateFormat formatter = new SimpleDateFormat("hhmmss_ddMMyyyy");
		String timestamp = formatter.format(new Date());

		return "ea_" + config.getInt(EAFields.EXPERIMENT_ID) + //
		"_" + config.getString(EAFields.ALGORITHM) + //
		"_sp" + config.getInt(EAFields.SPEED) + //
		"_ues" + config.getInt(EAFields.NUM_UES) + //
		"_i" + config.getInt(EAFields.GENERATIONS) + //
		"_" + config.getString(EAFields.TRAFFIC) + //
		"_s" + config.getString(EAFields.SEED) + //
		"_" + timestamp + ".log";
	}

	@Override
	public String getId() {
		return config.getInt(EAFields.EXPERIMENT_ID) + "";
	}

	/**
	 * Runs the Multi-Objective Evolutionary Algorithm.
	 * 
	 * @throws InterruptedException
	 */
	@Override
	@SuppressWarnings("boxing")
	public Integer call() throws InterruptedException {
		Thread.currentThread().setName(String.format("EA_%d_%d", Thread.currentThread().getId(), config.getInt(EAFields.EXPERIMENT_ID)));
		configureLogging();

		long time = System.currentTimeMillis();
		printConfig(config);
		LOG.error("Starting MO-EA...");

		Population population = new Population(config);
		population.evaluate();

		ParetoFront paretoFront = new ParetoFront();
		paretoFront.addAll(population);

		for (int generation = 0; generation < config.getInt(EAFields.GENERATIONS); generation++) {
			LOG.error("--- Generation {} --- ", generation);
			population.log();
			paretoFront.log();

			// do mutation
			Chromosome selected = population.select();
			LOG.error("Mutation: selected chromosome {}: {}", selected.id, selected.log());
			Chromosome mutated = selected.mutate();
			mutated.evaluate();
			LOG.error("Mutation: mutant chromosome {}: {}", mutated.id, mutated.log());
			if (mutated.fitness.percentileTput >= selected.fitness.percentileTput) {
				population.remove(selected);
				population.add(mutated);
				paretoFront.add(mutated);
			}

			// do crossover
			Chromosome parent1 = population.select();
			Chromosome parent2 = population.select(parent1);
			List<Chromosome> children = population.crossover(parent1, parent2);

			Chromosome child1 = children.get(0);
			Chromosome child2 = children.get(1);

			child1.evaluate();
			child2.evaluate();

			paretoFront.add(child1);
			paretoFront.add(child2);

			LOG.error("Crossover: parent1 {}: {}", parent1.id, parent1.log());
			LOG.error("Crossover: parent2 {}: {}", parent2.id, parent2.log());
			LOG.error("Crossover: child1 {}: {}", child1.id, child1.log());
			LOG.error("Crossover: child2 {}: {}", child2.id, child2.log());

			for (Chromosome child: children) {
				population.add(child);
			}

			// select for next generation
			population.prune();

		}

		population.log();
		paretoFront.log();

		LOG.error("Time taken: " + Double.toString((System.currentTimeMillis() - time) / 1000.0));

		return 1;
	}
}
