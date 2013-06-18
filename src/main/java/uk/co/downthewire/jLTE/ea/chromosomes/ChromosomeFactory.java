package uk.co.downthewire.jLTE.ea.chromosomes;

import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.ADAPTIVE_EDGE_THRESHOLD;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.ADAPTIVE_PROPORTION_OF_HIGH_POWER_RBS;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.ADAPTIVE_RANDOM_HIGH_POWER_RBS;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.ADAPTIVE_RANDOM_TRIGGER;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.ADAPTIVE_REDUCED_POWER_FACTOR;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.X2_MAX_RBS_PER_MSG;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.X2_MSG_ALL_NEIGHBOURS;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.X2_MSG_LIFE_TIME;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.X2_MSG_WAIT_TIME;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.configuration.Configuration;

import uk.co.downthewire.jLTE.ea.EAFields;
import uk.co.downthewire.jLTE.ea.genes.BooleanGene;
import uk.co.downthewire.jLTE.ea.genes.DoubleGene;
import uk.co.downthewire.jLTE.ea.genes.IGene;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;
import flanagan.math.PsRandom;

public class ChromosomeFactory {

	@SuppressWarnings("rawtypes")
	public static Chromosome createX2Chromosome(Configuration config, int id) {
		PsRandom random = (PsRandom) config.getProperty(EAFields.RANDOM);
		Map<String, IGene> genes = new TreeMap<>();

		genes.putAll(getX2Genes(random));

		return new Chromosome(config, genes, id);
	}

	@SuppressWarnings("rawtypes")
	public static Chromosome createAdaptiveChromosome(Configuration config, int id) {
		PsRandom random = (PsRandom) config.getProperty(EAFields.RANDOM);
		Map<String, IGene> genes = new TreeMap<>();

		genes.putAll(getAdaptiveGenes(random));

		return new Chromosome(config, genes, id);
	}

	@SuppressWarnings("rawtypes")
	public static Chromosome createAdaptiveX2Chromosome(Configuration config, int id) {
		PsRandom random = (PsRandom) config.getProperty(EAFields.RANDOM);
		Map<String, IGene> genes = new TreeMap<>();

		genes.putAll(getAdaptiveGenes(random));
		genes.putAll(getX2Genes(random));

		return new Chromosome(config, genes, id);
	}

	@SuppressWarnings("rawtypes")
	private static Map<String, IGene> getAdaptiveGenes(PsRandom random) {
		Map<String, IGene> genes = new TreeMap<>();

		genes.put(ADAPTIVE_EDGE_THRESHOLD, new DoubleGene(random, 0.0, 1.0));
		genes.put(ADAPTIVE_REDUCED_POWER_FACTOR, new DoubleGene(random, 0.0, 1.0));
		genes.put(ADAPTIVE_PROPORTION_OF_HIGH_POWER_RBS, new DoubleGene(random, 0.0, 1.0));
		genes.put(ADAPTIVE_RANDOM_TRIGGER, new DoubleGene(random, 0.0, 1.0));
		genes.put(ADAPTIVE_RANDOM_HIGH_POWER_RBS, new BooleanGene(random.nextInteger(0, 1) == 1));

		return genes;
	}

	@SuppressWarnings({ "rawtypes" })
	private static Map<String, IGene> getX2Genes(PsRandom random) {
		Map<String, IGene> genes = new TreeMap<>();
		genes.put(X2_MAX_RBS_PER_MSG, new DoubleGene(random, 0.0, 100.0));
		genes.put(X2_MSG_ALL_NEIGHBOURS, new BooleanGene(random.nextInteger(0, 1) == 1));
		genes.put(X2_MSG_LIFE_TIME, new DoubleGene(random, 0.0, 100.0));
		genes.put(X2_MSG_WAIT_TIME, new DoubleGene(random, 0.0, 100.0));

		return genes;
	}

	@SuppressWarnings("rawtypes")
	public static void addGenesToConfig(Configuration eaConfig, Configuration configuration, Map<String, IGene> genes) {
		if (eaConfig.getBoolean(EAFields.X2_ENABLED)) {
			addX2GenesToConfig(configuration, genes);
		}
		if (eaConfig.getString(EAFields.ALGORITHM).equals(FieldNames.ADAPTIVE_SFR)) {
			addAdaptiveGenesToConfig(configuration, genes);
		}

	}

	@SuppressWarnings("rawtypes")
	private static void addAdaptiveGenesToConfig(Configuration configuration, Map<String, IGene> genes) {
		configuration.setProperty(ADAPTIVE_EDGE_THRESHOLD, genes.get(ADAPTIVE_EDGE_THRESHOLD).getValue());
		configuration.setProperty(ADAPTIVE_REDUCED_POWER_FACTOR, genes.get(ADAPTIVE_REDUCED_POWER_FACTOR).getValue());
		configuration.setProperty(ADAPTIVE_PROPORTION_OF_HIGH_POWER_RBS, genes.get(ADAPTIVE_PROPORTION_OF_HIGH_POWER_RBS).getValue());
		configuration.setProperty(ADAPTIVE_RANDOM_TRIGGER, genes.get(ADAPTIVE_RANDOM_TRIGGER).getValue());
		configuration.setProperty(ADAPTIVE_RANDOM_HIGH_POWER_RBS, genes.get(ADAPTIVE_RANDOM_HIGH_POWER_RBS).getValue());

	}

	@SuppressWarnings("rawtypes")
	private static void addX2GenesToConfig(Configuration configuration, Map<String, IGene> genes) {
		configuration.setProperty(X2_MAX_RBS_PER_MSG, genes.get(X2_MAX_RBS_PER_MSG).getValue());
		configuration.setProperty(X2_MSG_ALL_NEIGHBOURS, genes.get(X2_MSG_ALL_NEIGHBOURS).getValue());
		configuration.setProperty(X2_MSG_LIFE_TIME, genes.get(X2_MSG_LIFE_TIME).getValue());
		configuration.setProperty(X2_MSG_WAIT_TIME, genes.get(X2_MSG_WAIT_TIME).getValue());

	}
}
