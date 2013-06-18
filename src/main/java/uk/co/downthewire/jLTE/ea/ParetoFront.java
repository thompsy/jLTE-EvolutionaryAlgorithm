package uk.co.downthewire.jLTE.ea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.downthewire.jLTE.ea.chromosomes.Chromosome;


public class ParetoFront {

	static final Logger LOG = LoggerFactory.getLogger(ParetoFront.class);
	private final List<Chromosome> front;

	public ParetoFront() {
		this.front = new ArrayList<>();
	}

	@SuppressWarnings({ "boxing" })
	public void add(Chromosome chromosome) {

		if (front.contains(chromosome)) {
			LOG.error("Chromosome {} already exists in ParetoFront", chromosome.id);
			return;
		}

		for (Chromosome paretoChromosome: front) {
			if (paretoChromosome.dominates(chromosome)) {
				return;
			}
		}
		front.add(chromosome);
		clean();
	}

	public void addAll(Population population) {
		for (Chromosome chromosome: population.getChromosomes()) {
			add(chromosome);
		}
	}

	public void clean() {
		List<Chromosome> toRemove = new ArrayList<>();
		for (Chromosome c1: front) {
			for (Chromosome c2: front) {
				if (c1.id != c2.id && !c1.equals(c2)) {
					if (c1.dominates(c2)) {
						LOG.error("c[" + c2.id + "] dominated by c[" + c1.id + "]");
						toRemove.add(c2);
					}
				}
			}
		}
		front.removeAll(toRemove);
	}

	public void log() {

		Collections.sort(front, Chromosome.ID_ORDER);

		if (front.isEmpty()) {
			LOG.error("--- Pareto Front - Empty ---");
			return;
		}

		LOG.error("--- Pareto Front ---");
		LOG.error(front.get(0).logHeader());
		for (Chromosome chromosome: front) {
			LOG.error(chromosome.log());
		}
	}
}
