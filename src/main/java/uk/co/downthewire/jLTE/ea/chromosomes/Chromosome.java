package uk.co.downthewire.jLTE.ea.chromosomes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.downthewire.jLTE.ea.EAFields;
import uk.co.downthewire.jLTE.ea.genes.IGene;
import uk.co.downthewire.jLTE.simulator.ParallelSimulationRunner;
import uk.co.downthewire.jLTE.simulator.results.AggregatedSimulationResults;
import uk.co.downthewire.jLTE.simulator.results.SimulationResults;
import uk.co.downthewire.jLTE.simulator.results.SimulationResultsAggregator;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;
import flanagan.math.PsRandom;

public class Chromosome {

	static final Logger LOG = LoggerFactory.getLogger(Chromosome.class);

	@SuppressWarnings("rawtypes")
	private final Map<String, IGene> genes;
	private final Configuration eaConfig;
	public final int id;
	public AggregatedSimulationResults fitness;

	@SuppressWarnings("rawtypes")
	public Chromosome(Configuration eaConfig, Map<String, IGene> genes, int id) {
		this.genes = genes;
		this.eaConfig = eaConfig;
		this.id = id;
	}

	@SuppressWarnings("boxing")
	private List<Configuration> toSimuatorConfigurations() {
		List<Configuration> configs = new ArrayList<>();

		for (Object rawSeed: eaConfig.getList(EAFields.SIM_SEEDS)) {
			double seed = Double.parseDouble(((String) rawSeed));
			Configuration configuration;
			try {
				configuration = new PropertiesConfiguration("system.properties").interpolatedConfiguration();
				configuration.setProperty(FieldNames.SEED, seed);
				configuration.setProperty(FieldNames.ALGORITHM, eaConfig.getString(EAFields.ALGORITHM));
				configuration.setProperty(FieldNames.ITERATIONS, eaConfig.getInt(EAFields.ITERATIONS));
				configuration.setProperty(FieldNames.SCENARIO_PATH, eaConfig.getString(EAFields.EXPERIMENT_PATH));
				configuration.setProperty(FieldNames.TRAFFIC_TYPE, eaConfig.getString(EAFields.TRAFFIC));
				configuration.setProperty(FieldNames.NUM_UES, eaConfig.getInt(EAFields.NUM_UES));
				configuration.setProperty(FieldNames.SPEED, eaConfig.getInt(EAFields.SPEED));
				configuration.setProperty(FieldNames.X2_ENABLED, eaConfig.getBoolean(EAFields.X2_ENABLED));

				configuration.setProperty(FieldNames.EXPERIMENT_ID, eaConfig.getInt(EAFields.EXPERIMENT_ID));
				configuration.setProperty(FieldNames.CHROMOSOME_ID, id);

				ChromosomeFactory.addGenesToConfig(eaConfig, configuration, genes);
				configs.add(configuration);
			} catch (ConfigurationException e) {
				throw new IllegalStateException(e);
			}
		}
		return configs;
	}

	@SuppressWarnings({ "rawtypes", "null" })
	public Chromosome mutate() {
		Chromosome cloned = this.clone();
		PsRandom random = (PsRandom) eaConfig.getProperty(EAFields.RANDOM);

		int randomIndex = random.nextInteger(cloned.genes.keySet().size() - 1);
		int index = 0;
		IGene gene = null;
		String geneName = null;
		for (String key: cloned.genes.keySet()) {
			if (index++ == randomIndex) {
				geneName = key;
				gene = cloned.genes.get(key);
				break;
			}
		}

		LOG.error("Mutating gene: {} = {}", geneName, gene);
		gene.mutate();
		LOG.error("Mutated gene: {} = {}", geneName, gene);
		return cloned;
	}

	@SuppressWarnings({ "rawtypes", "boxing" })
	@Override
	public Chromosome clone() {
		SortedMap<String, IGene> clonedGenes = new TreeMap<>();
		for (String key: genes.keySet()) {
			clonedGenes.put(key, genes.get(key).clone());
		}
		int nextId = eaConfig.getInt(EAFields.CHROMOSOME_ID);
		eaConfig.setProperty(EAFields.CHROMOSOME_ID, nextId + 1);

		return new Chromosome(eaConfig, clonedGenes, nextId);
	}

	public void evaluate() {
		ParallelSimulationRunner runner = new ParallelSimulationRunner(eaConfig.getInt(EAFields.NUM_THREADS), LOG);

		for (Configuration config: toSimuatorConfigurations()) {
			runner.addConfigToRun(config);
		}

		List<SimulationResults> results = runner.run();
		SimulationResultsAggregator aggregator = new SimulationResultsAggregator();
		aggregator.aggregate(results);

		fitness = aggregator.getResult();
	}

	public String logHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("id,");
		for (String key: genes.keySet()) {
			sb.append(key).append(',');
		}
		sb.append(AggregatedSimulationResults.header());
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	public String log() {
		StringBuilder sb = new StringBuilder();
		sb.append(id).append(',');
		for (Entry<String, IGene> entry: genes.entrySet()) {
			sb.append(entry.getValue()).append(',');
		}
		sb.append(fitness);
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	public static List<Chromosome> crossover(Configuration eaConfig, Chromosome parent1, Chromosome parent2) {
		Chromosome child1 = parent1.clone();
		Chromosome child2 = parent2.clone();

		PsRandom random = (PsRandom) eaConfig.getProperty(EAFields.RANDOM);

		for (String key: parent1.genes.keySet()) {
			if (random.nextBit() == 1) {
				IGene gene1 = child1.genes.get(key);
				IGene gene2 = child2.genes.get(key);

				child1.genes.put(key, gene2);
				child2.genes.put(key, gene1);
			}
		}

		List<Chromosome> children = new ArrayList<>();
		children.add(child1);
		children.add(child2);
		return children;
	}

	public boolean dominates(Chromosome other) {
		return this.fitness.avergeTput > other.fitness.avergeTput && this.fitness.percentileTput > other.fitness.percentileTput;
	}

	public static final Comparator<Chromosome> FITNESS_ORDER = new Comparator<Chromosome>() {
		@SuppressWarnings("boxing")
		@Override
		public int compare(Chromosome c1, Chromosome c2) {
			Double f1 = c1.fitness.percentileTput;
			Double f2 = c2.fitness.percentileTput;
			return f1.compareTo(f2);
		}
	};

	public static final Comparator<Chromosome> ID_ORDER = new Comparator<Chromosome>() {
		@SuppressWarnings("boxing")
		@Override
		public int compare(Chromosome c1, Chromosome c2) {
			Integer id1 = c1.id;
			Integer id2 = c2.id;
			return id1.compareTo(id2);
		}

	};

}
