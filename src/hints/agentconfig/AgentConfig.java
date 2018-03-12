package net.amygdalum.tanteemmas.testrecorder;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;

import net.amygdalum.testrecorder.DefaultSerializationProfile;
import net.amygdalum.testrecorder.profile.Classes;
import net.amygdalum.testrecorder.profile.Methods;

public class AgentConfig extends DefaultSerializationProfile {

	@Override
	public List<Classes> getClasses() {
		return asList(Classes.byPackage("net.amygdalum.tanteemmas"), Classes.byName("java.io.Writer"));
	}

	@Override
	public List<Methods> getOutputs() {
		return emptyList();
	}

}
