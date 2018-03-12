package net.amygdalum.tanteemmas.testrecorder;

import java.nio.file.Paths;

import net.amygdalum.testrecorder.ScheduledTestGenerator;
import net.amygdalum.testrecorder.profile.AgentConfiguration;

public class TestGenerator extends ScheduledTestGenerator {

	public TestGenerator(AgentConfiguration config) {
		super(config);
		this.counterMaximum = 1000;
		this.counterInterval = 5;
		this.classNameTemplate = "${class}${counter}Test";
		this.generateTo = Paths.get("target/generated");
		this.dumpOnShutdown(true);
	}

}
