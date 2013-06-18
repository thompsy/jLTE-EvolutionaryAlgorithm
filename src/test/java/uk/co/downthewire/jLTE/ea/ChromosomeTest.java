package uk.co.downthewire.jLTE.ea;

import static org.junit.Assert.assertEquals;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;

import uk.co.downthewire.jLTE.ea.EAFields;
import uk.co.downthewire.jLTE.ea.chromosomes.Chromosome;
import uk.co.downthewire.jLTE.ea.chromosomes.ChromosomeFactory;

import flanagan.math.PsRandom;

public class ChromosomeTest {

	@Test
	public void test() throws ConfigurationException {
		Configuration config = new PropertiesConfiguration("ea-integ-test.properties").interpolatedConfiguration();
		config.setProperty(EAFields.RANDOM, new PsRandom((long) 11111.11111));

		int id = 0;

		Chromosome chromosome = ChromosomeFactory.createAdaptiveChromosome(config, id);
		chromosome.evaluate();

		assertEquals(1.861682, chromosome.fitness.avergeTput, 0.000001);
		assertEquals(0.198394, chromosome.fitness.percentileTput, 0.000001);
	}
}
