package org.matsim.run;

import org.matsim.api.core.v01.Scenario;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.cadyts.car.CadytsCarModule;
import org.matsim.contrib.cadyts.car.CadytsContext;
import org.matsim.contrib.cadyts.general.CadytsConfigGroup;
import org.matsim.contrib.cadyts.general.CadytsScoring;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.*;
import org.matsim.roadpricing.RoadPricingModule;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RunMatsimWithCadyts {
    public static void main(String[] args) {
        List<Integer> weights = new ArrayList<Integer>() {{
            add(100);
            add(1000);
            add(10000);
            add(100000);
        }};
        for (Integer weight : weights) {
            CadytsConfigGroup cadytsConfigGroup = new CadytsConfigGroup();
            cadytsConfigGroup.setWriteAnalysisFile(true);

            Config config = ConfigUtils.loadConfig("input/config.xml", cadytsConfigGroup);
            Scenario scenario = ScenarioUtils.loadScenario(config);

            System.out.println("Total Population: " + scenario.getPopulation().getPersons().size());

            CadytsCarModule cadytsCarModule = new CadytsCarModule();

            Controler controler = new Controler(scenario);
            config.controler().setOutputDirectory("output/withCadyts_weight"+weight);

            controler.addOverridingModule(cadytsCarModule);

            config.controler().setLastIteration(500);
            config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

            controler.setScoringFunctionFactory(new ScoringFunctionFactory() {
                @Inject
                CadytsContext cadytsContext;
                @Inject
                ScoringParametersForPerson parameters;

                @Override
                public ScoringFunction createNewScoringFunction(Person person) {
                    final ScoringParameters params = parameters.getScoringParameters(person);
                    SumScoringFunction scoringFunctionAccumulator = new SumScoringFunction();
                    scoringFunctionAccumulator.addScoringFunction(new CharyparNagelLegScoring(params, controler.getScenario().getNetwork()));
                    scoringFunctionAccumulator.addScoringFunction(new CharyparNagelActivityScoring(params));
                    scoringFunctionAccumulator.addScoringFunction(new CharyparNagelAgentStuckScoring(params));

                    final CadytsScoring<Link> scoringFunction = new CadytsScoring<>(person.getSelectedPlan(), config, cadytsContext);
                    scoringFunction.setWeightOfCadytsCorrection(weight);
                    scoringFunctionAccumulator.addScoringFunction(scoringFunction);

                    return scoringFunctionAccumulator;
                }
            });

            controler.run();
        }
    }
}
