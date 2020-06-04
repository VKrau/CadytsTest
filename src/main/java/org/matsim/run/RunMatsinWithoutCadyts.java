package org.matsim.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RunMatsinWithoutCadyts {
    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("input/config.xml");
        config.controler().setOutputDirectory("output/"+LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).split(":")[0] + "_" +
                "withoutCadyts");
        config.controler().setLastIteration(500);
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Controler controler = new Controler(scenario);
        controler.run();
    }
}
