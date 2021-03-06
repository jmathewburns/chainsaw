/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zyxist.chainsaw.exec;

import com.zyxist.chainsaw.JavaModule;
import com.zyxist.chainsaw.TaskConfigurator;
import com.zyxist.chainsaw.algorithms.ModulePatcher;
import com.zyxist.chainsaw.jigsaw.JigsawCLI;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.JavaExec;

import static com.zyxist.chainsaw.ChainsawPlugin.PATCH_CONFIGURATION_NAME;

public class RunTaskConfigurator implements TaskConfigurator<JavaExec> {
	private final JavaModule moduleConfig;

	public RunTaskConfigurator(JavaModule moduleConfig) {
		this.moduleConfig = moduleConfig;
	}

	@Override
	public void updateConfiguration(Project project, JavaExec task) {
		task.getInputs().property("moduleName", moduleConfig.getName());
	}

	@Override
	public Action<Task> doFirst(final Project project, final JavaExec run) {
		return new Action<Task>() {
			@Override
			public void execute(Task task) {
				JigsawCLI cli = new JigsawCLI(run.getClasspath().getAsPath());
				cli.module(moduleConfig.getName(), run.getMain());

				ModulePatcher patcher = new ModulePatcher(moduleConfig.getPatchModules());
				patcher
					.patchFrom(project, PATCH_CONFIGURATION_NAME)
					.forEach((k, patchedModule) -> cli.patchList().patch(patchedModule));

				run.setJvmArgs(cli.generateArgs());
				run.setClasspath(project.files());
			}
		};
	}
}
