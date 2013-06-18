package uk.co.downthewire.jLTE.ea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.downthewire.jLTE.ea.chromosomes.Chromosome;
import uk.co.downthewire.jLTE.ea.chromosomes.ChromosomeFactory;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;
import flanagan.math.PsRandom;

public class Population {

	static final Logger LOG = LoggerFactory.getLogger(Population.class);

	private List<Chromosome> chromosomes;
	private final Configuration config;

	@SuppressWarnings("boxing")
	public Population(Configuration config) {
		this.config = config;
		chromosomes = new ArrayList<>();

		for (int i = 0; i < config.getInt(EAFields.POPULATION); i++) {
			Chromosome chromosome = null;
			if (config.getString(EAFields.ALGORITHM).equals(FieldNames.ADAPTIVE_SFR)) {
				if (config.getBoolean(EAFields.X2_ENABLED)) {
					chromosome = ChromosomeFactory.createAdaptiveX2Chromosome(config, i);
				} else {
					chromosome = ChromosomeFactory.createAdaptiveChromosome(config, i);
				}
			} else if (config.getBoolean(EAFields.X2_ENABLED)) {
				chromosome = ChromosomeFactory.createX2Chromosome(config, i);
			} else {
				throw new IllegalArgumentException("Unknown Chromosome type");
			}
			chromosomes.add(chromosome);
		}
		config.setProperty(EAFields.CHROMOSOME_ID, chromosomes.size());
	}

	public void log() {
		LOG.error("--- Population ---");
		LOG.error(chromosomes.get(0).logHeader());
		for (Chromosome ch: chromosomes) {
			LOG.error(ch.log());
		}
	}

	public Chromosome select() {
		return fitnessProportionateSelection(chromosomes);
	}

	public Chromosome select(Chromosome... other) {
		List<Chromosome> selection = new ArrayList<>(chromosomes);
		for (Chromosome c: other) {
			selection.remove(c);
		}
		return fitnessProportionateSelection(selection);
	}

	@SuppressWarnings({ "hiding", "boxing" })
	private Chromosome fitnessProportionateSelection(List<Chromosome> chromosomes) {

		double totalFitness = 0;
		for (Chromosome chromosome: chromosomes) {
			totalFitness += chromosome.fitness.percentileTput;
		}

		Collections.sort(chromosomes, Chromosome.FITNESS_ORDER);
		PsRandom random = (PsRandom) config.getProperty(EAFields.RANDOM);
		double randomDouble = random.nextDouble();

		double totalProportionate = 0;
		for (Chromosome chromosome: chromosomes) {
			double proportionalFitness = chromosome.fitness.percentileTput / totalFitness;
			totalProportionate += proportionalFitness;
			LOG.info("totalFitness = {}, randomDouble = {}, totalProportion = {}", totalFitness, randomDouble, totalProportionate);
			if (totalProportionate > randomDouble) {
				return chromosome;
			}
		}
		LOG.error("Something went very wrong during selection");
		throw new IllegalStateException("Something went very wrong during selection");
	}

	public void remove(Chromosome selected) {
		chromosomes.remove(selected);
	}

	public void add(Chromosome mutated) {
		chromosomes.add(mutated);
	}

	public void evaluate() {
		for (Chromosome ch: chromosomes) {
			ch.evaluate();
		}
	}

	public List<Chromosome> crossover(Chromosome parent1, Chromosome parent2) {
		return Chromosome.crossover(config, parent1, parent2);
	}

	public void prune() {
		List<Chromosome> newChromosomes = new ArrayList<>();
		int desiredPopulationSize = config.getInt(EAFields.POPULATION);
		while (newChromosomes.size() < desiredPopulationSize) {
			Chromosome selected = select();
			remove(selected);
			newChromosomes.add(selected);
		}
		chromosomes = newChromosomes;
	}

	public List<Chromosome> getChromosomes() {
		return chromosomes;
	}
}
